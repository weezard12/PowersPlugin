package me.weezard12.powers.internal;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.weezard12.powers.api.Ability;
import me.weezard12.powers.api.AbilityActivation;
import me.weezard12.powers.api.ActiveAbility;
import me.weezard12.powers.api.PassiveAbility;
import me.weezard12.powers.api.PlayerPowerManager;
import me.weezard12.powers.api.Power;
import me.weezard12.powers.api.conditions.AbilityCondition;
import me.weezard12.powers.api.conditions.AbilityConditionContext;
import me.weezard12.powers.api.conditions.AbilityConditionManager;
import me.weezard12.powers.api.conditions.ConditionAction;
import me.weezard12.powers.api.conditions.ConditionRule;
import me.weezard12.powers.api.conditions.ConditionTrigger;
import me.weezard12.powers.api.conditions.ConditionalAbility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/**
 * Runtime condition manager for active/passive ability state.
 */
public final class SimpleAbilityConditionManager implements AbilityConditionManager, Listener, Runnable {
    private static final EnumSet<ConditionTrigger> HOLD_TRIGGERS = EnumSet.of(
            ConditionTrigger.START_HOLD_HAND,
            ConditionTrigger.START_HOLD_OFF_HAND,
            ConditionTrigger.START_HOLD_ANY_HAND,
            ConditionTrigger.FINISH_HOLD_HAND,
            ConditionTrigger.FINISH_HOLD_OFF_HAND,
            ConditionTrigger.FINISH_HOLD_ANY_HAND,
            ConditionTrigger.HOLD_INVENTORY
    );

    private final Plugin plugin;
    private final PlayerPowerManager playerPowerManager;
    private final Logger logger;
    private final ConcurrentHashMap<UUID, Map<AbilityKey, AbilityState>> statesByPlayer =
            new ConcurrentHashMap<UUID, Map<AbilityKey, AbilityState>>();
    private final ConcurrentHashMap<UUID, HandSnapshot> handSnapshots =
            new ConcurrentHashMap<UUID, HandSnapshot>();
    private final boolean modernPickupListenerRegistered;
    private volatile long tickCounter = 0L;

    public SimpleAbilityConditionManager(Plugin plugin, PlayerPowerManager playerPowerManager, Logger logger) {
        this.plugin = plugin;
        this.playerPowerManager = playerPowerManager;
        this.logger = logger;
        this.modernPickupListenerRegistered = registerModernPickupListener();
    }

    @Override
    public boolean isAbilityEnabled(Player player, Power power, Ability ability) {
        if (player == null || power == null || ability == null) {
            return false;
        }
        if (!(ability instanceof ConditionalAbility)) {
            return true;
        }
        ConditionalAbility conditional = (ConditionalAbility) ability;
        List<ConditionRule> rules = conditional.getConditionRules();
        if (!hasStateRules(rules)) {
            return true;
        }
        AbilityState state = getOrCreateState(player, power, ability, conditional, rules);
        return state.enabled;
    }

    @Override
    public void onPowerAdded(Player player, Power power) {
        if (player == null || power == null) {
            return;
        }
        handSnapshots.put(player.getUniqueId(), HandSnapshot.capture(player));
    }

    @Override
    public void onPowerRemoved(Player player, Power power) {
        if (player == null || power == null) {
            return;
        }
        removeStateForPower(player.getUniqueId(), power.getId());
        handSnapshots.put(player.getUniqueId(), HandSnapshot.capture(player));
    }

    @Override
    public void onPowersCleared(Player player) {
        clearPlayerState(player);
    }

    @Override
    public boolean dispatchCustomTrigger(Player player, String triggerKey) {
        return dispatchCustomTrigger(player, triggerKey, null, null);
    }

    @Override
    public boolean dispatchCustomTrigger(Player player, String triggerKey, Object sourceEvent, ItemStack eventItem) {
        if (player == null) {
            return false;
        }
        if (!Bukkit.isPrimaryThread()) {
            if (plugin == null || !plugin.isEnabled()) {
                return false;
            }
            try {
                Future<Boolean> future = Bukkit.getScheduler().callSyncMethod(plugin,
                        () -> dispatchCustomTrigger(player, triggerKey, sourceEvent, eventItem));
                return future.get(10L, TimeUnit.SECONDS);
            } catch (TimeoutException ex) {
                logger.log(Level.SEVERE, "Timed out while dispatching custom trigger: " + triggerKey, ex);
                return false;
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Failed to dispatch custom trigger: " + triggerKey, ex);
                return false;
            }
        }

        String normalizedTriggerKey = normalizeTriggerKey(triggerKey);
        if (normalizedTriggerKey == null) {
            return false;
        }
        Set<Power> powers = playerPowerManager.getPowers(player);
        if (powers.isEmpty()) {
            return false;
        }

        HandSnapshot before = snapshotBefore(player);
        HandSnapshot after = snapshotAfter(player);
        TriggerRef trigger = TriggerRef.custom(normalizedTriggerKey);

        boolean handled = false;
        for (Power power : powers) {
            if (power == null) {
                continue;
            }
            for (Ability ability : power.getAbilities()) {
                if (!(ability instanceof ConditionalAbility)) {
                    continue;
                }
                ConditionalAbility conditional = (ConditionalAbility) ability;
                List<ConditionRule> rules = conditional.getConditionRules();
                if (rules == null || rules.isEmpty()) {
                    continue;
                }
                if (!hasAnySupportedRule(rules, Collections.singletonList(trigger), ability)) {
                    continue;
                }

                ConditionResult result = applyRules(
                        player,
                        power,
                        ability,
                        conditional,
                        rules,
                        Collections.singletonList(trigger),
                        AbilityActivation.UNKNOWN,
                        sourceEvent,
                        PlayerItemAccess.cloneOrNull(eventItem),
                        before,
                        after,
                        tickCounter
                );

                if (ability instanceof ActiveAbility && result.enabled && result.triggered) {
                    handled |= ((ActiveAbility) ability).tryActivate(player, power, AbilityActivation.UNKNOWN);
                }
            }
        }

        handSnapshots.put(player.getUniqueId(), after);
        return handled;
    }

    @Override
    public void clearPlayerState(Player player) {
        if (player == null) {
            return;
        }
        clearPlayerState(player.getUniqueId());
    }

    @Override
    public void clearPlayerState(UUID playerId) {
        if (playerId == null) {
            return;
        }
        statesByPlayer.remove(playerId);
        handSnapshots.remove(playerId);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        ItemStack eventItem = extractItemStack(event.getItemDrop());
        handleDropTrigger(player, event, eventItem);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickUp(PlayerPickupItemEvent event) {
        if (modernPickupListenerRegistered) {
            return;
        }
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        ItemStack eventItem = extractItemStack(event.getItem());
        handlePickUpTrigger(player, event, eventItem);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        clearPlayerState(event.getPlayer());
    }

    @Override
    public void run() {
        tickCounter++;
        for (Player player : getOnlinePlayers()) {
            if (player == null) {
                continue;
            }
            Set<Power> powers = playerPowerManager.getPowers(player);
            if (powers.isEmpty()) {
                handSnapshots.put(player.getUniqueId(), HandSnapshot.capture(player));
                pruneState(player.getUniqueId(), powers);
                continue;
            }

            HandSnapshot before = snapshotBefore(player);
            HandSnapshot after = HandSnapshot.capture(player);
            applyTriggers(player, powers, toTriggerRefs(HOLD_TRIGGERS), null, null, null, before, after, false);
            handSnapshots.put(player.getUniqueId(), after);
            pruneState(player.getUniqueId(), powers);
        }
    }

    void handleActivationForPassives(Player player, Set<Power> powers, AbilityActivation activation) {
        if (player == null || powers == null || powers.isEmpty() || activation == null) {
            return;
        }
        List<TriggerRef> triggers = triggersForActivation(activation);
        if (triggers.isEmpty()) {
            return;
        }
        HandSnapshot snapshot = snapshotAfter(player);
        applyTriggers(player, powers, triggers, activation, null, null, snapshot, snapshot, true);
        handSnapshots.put(player.getUniqueId(), snapshot);
    }

    boolean tryActivateConditional(Player player, Power power, ActiveAbility ability, AbilityActivation activation) {
        if (player == null || power == null || ability == null || activation == null) {
            return false;
        }
        if (!(ability instanceof ConditionalAbility)) {
            return ability.tryActivate(player, power, activation);
        }
        ConditionalAbility conditional = (ConditionalAbility) ability;
        List<ConditionRule> rules = conditional.getConditionRules();
        if (rules == null || rules.isEmpty()) {
            return ability.tryActivate(player, power, activation);
        }

        List<TriggerRef> triggers = triggersForActivation(activation);
        if (triggers.isEmpty()) {
            return false;
        }
        HandSnapshot snapshot = snapshotAfter(player);
        ConditionResult result = applyRules(player, power, ability, conditional, rules, triggers, activation,
                null, null, snapshot, snapshot, tickCounter);
        handSnapshots.put(player.getUniqueId(), snapshot);
        if (!result.triggered || !result.enabled) {
            return false;
        }
        return ability.tryActivate(player, power, activation);
    }

    boolean isPassiveEnabled(Player player, Power power, PassiveAbility ability) {
        return isAbilityEnabled(player, power, ability);
    }

    private void applyTriggers(Player player, Set<Power> powers, Collection<TriggerRef> triggers,
                               AbilityActivation activation, Object sourceEvent, ItemStack eventItem,
                               HandSnapshot before, HandSnapshot after, boolean passiveOnly) {
        if (player == null || triggers == null || triggers.isEmpty() || powers == null || powers.isEmpty()) {
            return;
        }
        for (Power power : powers) {
            if (power == null) {
                continue;
            }
            Collection<Ability> abilities = passiveOnly ? power.getPassiveAbilities() : power.getAbilities();
            for (Ability ability : abilities) {
                if (!(ability instanceof ConditionalAbility)) {
                    continue;
                }
                if (passiveOnly && !(ability instanceof PassiveAbility)) {
                    continue;
                }
                ConditionalAbility conditional = (ConditionalAbility) ability;
                List<ConditionRule> rules = conditional.getConditionRules();
                if (rules == null || rules.isEmpty()) {
                    continue;
                }
                if (!hasAnySupportedRule(rules, triggers, ability)) {
                    continue;
                }
                applyRules(player, power, ability, conditional, rules, triggers, activation, sourceEvent, eventItem,
                        before, after, tickCounter);
            }
        }
    }

    private ConditionResult applyRules(Player player, Power power, Ability ability, ConditionalAbility conditional,
                                       List<ConditionRule> rules, Collection<TriggerRef> triggers,
                                       AbilityActivation activation, Object sourceEvent, ItemStack eventItem,
                                       HandSnapshot before, HandSnapshot after, long tick) {
        boolean hasStateRules = hasStateRules(rules);
        boolean enabled = true;
        if (hasStateRules) {
            AbilityState state = getOrCreateState(player, power, ability, conditional, rules);
            enabled = state.enabled;
        }

        boolean triggered = false;
        for (TriggerRef trigger : triggers) {
            if (!hasSupportedRuleForTrigger(rules, trigger, ability)) {
                continue;
            }
            AbilityConditionContext.Builder contextBuilder = trigger.trigger != null
                    ? AbilityConditionContext.builder(player, power, ability, trigger.trigger)
                    : AbilityConditionContext.builder(player, power, ability, trigger.triggerKey);
            AbilityConditionContext context = contextBuilder
                    .setActivation(activation)
                    .setSourceEvent(sourceEvent)
                    .setEventItem(eventItem)
                    .setMainHandBefore(before.mainHand)
                    .setOffHandBefore(before.offHand)
                    .setMainHandAfter(after.mainHand)
                    .setOffHandAfter(after.offHand)
                    .setTick(tick)
                    .build();

            for (ConditionRule rule : rules) {
                if (rule == null || rule.getCondition() == null || rule.getAction() == null) {
                    continue;
                }
                AbilityCondition condition = rule.getCondition();
                if (!supports(condition, trigger.triggerKey, ability)) {
                    continue;
                }
                boolean matches = matches(condition, context, ability);
                switch (rule.getAction()) {
                    case TRIGGER:
                        if (matches) {
                            triggered = true;
                        }
                        break;
                    case ENABLE:
                        if (matches) {
                            enabled = true;
                        }
                        break;
                    case DISABLE:
                        if (matches) {
                            enabled = false;
                        }
                        break;
                    case SYNC:
                        enabled = matches;
                        break;
                    default:
                        break;
                }
            }
        }
        if (hasStateRules) {
            AbilityState state = getOrCreateState(player, power, ability, conditional, rules);
            state.enabled = enabled;
        }
        return new ConditionResult(enabled, triggered);
    }

    private AbilityState getOrCreateState(Player player, Power power, Ability ability, ConditionalAbility conditional,
                                          List<ConditionRule> rules) {
        UUID playerId = player.getUniqueId();
        Map<AbilityKey, AbilityState> map = statesByPlayer.get(playerId);
        if (map == null) {
            Map<AbilityKey, AbilityState> created = new ConcurrentHashMap<AbilityKey, AbilityState>();
            Map<AbilityKey, AbilityState> existing = statesByPlayer.putIfAbsent(playerId, created);
            map = existing == null ? created : existing;
        }
        AbilityKey key = AbilityKey.of(power, ability);
        AbilityState state = map.get(key);
        if (state != null) {
            return state;
        }

        boolean defaultEnabled = !hasStateRules(rules) || conditional.isConditionStateEnabledByDefault(player, power);
        AbilityState created = new AbilityState(defaultEnabled);
        AbilityState existing = map.putIfAbsent(key, created);
        return existing == null ? created : existing;
    }

    private static boolean hasStateRules(List<ConditionRule> rules) {
        if (rules == null || rules.isEmpty()) {
            return false;
        }
        for (ConditionRule rule : rules) {
            if (rule != null && rule.getAction() != null && rule.getAction() != ConditionAction.TRIGGER) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAnySupportedRule(List<ConditionRule> rules, Collection<TriggerRef> triggers, Ability ability) {
        if (rules == null || rules.isEmpty() || triggers == null || triggers.isEmpty()) {
            return false;
        }
        for (TriggerRef trigger : triggers) {
            if (hasSupportedRuleForTrigger(rules, trigger, ability)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasSupportedRuleForTrigger(List<ConditionRule> rules, TriggerRef trigger, Ability ability) {
        if (rules == null || rules.isEmpty() || trigger == null) {
            return false;
        }
        for (ConditionRule rule : rules) {
            if (rule == null || rule.getCondition() == null || rule.getAction() == null) {
                continue;
            }
            if (supports(rule.getCondition(), trigger.triggerKey, ability)) {
                return true;
            }
        }
        return false;
    }

    private boolean supports(AbilityCondition condition, String triggerKey, Ability ability) {
        if (condition == null || triggerKey == null || triggerKey.trim().isEmpty()) {
            return false;
        }
        try {
            return condition.supports(triggerKey);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Condition supports() failed for ability: " + ability.getId(), ex);
            return false;
        }
    }

    private boolean matches(AbilityCondition condition, AbilityConditionContext context, Ability ability) {
        try {
            return condition.matches(context);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Condition evaluation failed for ability: " + ability.getId(), ex);
            return false;
        }
    }

    private void pruneState(UUID playerId, Set<Power> powers) {
        if (playerId == null) {
            return;
        }
        Map<AbilityKey, AbilityState> states = statesByPlayer.get(playerId);
        if (states == null || states.isEmpty()) {
            return;
        }
        if (powers == null || powers.isEmpty()) {
            statesByPlayer.remove(playerId);
            return;
        }

        Set<AbilityKey> valid = new HashSet<AbilityKey>();
        for (Power power : powers) {
            if (power == null) {
                continue;
            }
            for (Ability ability : power.getAbilities()) {
                if (!(ability instanceof ConditionalAbility)) {
                    continue;
                }
                List<ConditionRule> rules = ((ConditionalAbility) ability).getConditionRules();
                if (!hasStateRules(rules)) {
                    continue;
                }
                valid.add(AbilityKey.of(power, ability));
            }
        }

        for (AbilityKey key : new ArrayList<AbilityKey>(states.keySet())) {
            if (!valid.contains(key)) {
                states.remove(key);
            }
        }
        if (states.isEmpty()) {
            statesByPlayer.remove(playerId);
        }
    }

    private void removeStateForPower(UUID playerId, String powerId) {
        if (playerId == null || powerId == null || powerId.trim().isEmpty()) {
            return;
        }
        Map<AbilityKey, AbilityState> states = statesByPlayer.get(playerId);
        if (states == null || states.isEmpty()) {
            return;
        }
        for (AbilityKey key : new ArrayList<AbilityKey>(states.keySet())) {
            if (powerId.equals(key.powerId)) {
                states.remove(key);
            }
        }
        if (states.isEmpty()) {
            statesByPlayer.remove(playerId);
        }
    }

    private HandSnapshot snapshotBefore(Player player) {
        HandSnapshot snapshot = handSnapshots.get(player.getUniqueId());
        return snapshot == null ? HandSnapshot.empty() : snapshot;
    }

    private HandSnapshot snapshotAfter(Player player) {
        return HandSnapshot.capture(player);
    }

    private static List<TriggerRef> triggersForActivation(AbilityActivation activation) {
        switch (activation) {
            case RIGHT_CLICK:
                return toTriggerRefs(Arrays.asList(ConditionTrigger.RIGHT_CLICK, ConditionTrigger.ANY_RIGHT_CLICK));
            case SHIFT_RIGHT_CLICK:
                return toTriggerRefs(Arrays.asList(ConditionTrigger.SHIFT_RIGHT_CLICK, ConditionTrigger.ANY_RIGHT_CLICK));
            case LEFT_CLICK:
                return toTriggerRefs(Arrays.asList(ConditionTrigger.LEFT_CLICK, ConditionTrigger.ANY_LEFT_CLICK));
            case SHIFT_LEFT_CLICK:
                return toTriggerRefs(Arrays.asList(ConditionTrigger.SHIFT_LEFT_CLICK, ConditionTrigger.ANY_LEFT_CLICK));
            default:
                return Collections.emptyList();
        }
    }

    private static List<TriggerRef> toTriggerRefs(Collection<ConditionTrigger> triggers) {
        if (triggers == null || triggers.isEmpty()) {
            return Collections.emptyList();
        }
        List<TriggerRef> refs = new ArrayList<TriggerRef>();
        for (ConditionTrigger trigger : triggers) {
            if (trigger != null) {
                refs.add(TriggerRef.of(trigger));
            }
        }
        if (refs.isEmpty()) {
            return Collections.emptyList();
        }
        return refs;
    }

    private static String normalizeTriggerKey(String triggerKey) {
        if (triggerKey == null) {
            return null;
        }
        String normalized = triggerKey.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        return normalized.replace('-', '_').replace(' ', '_').toUpperCase(Locale.ROOT);
    }

    private void handleDropTrigger(Player player, Object sourceEvent, ItemStack eventItem) {
        HandSnapshot before = snapshotBefore(player);
        HandSnapshot after = snapshotAfter(player);
        applyTriggers(player, playerPowerManager.getPowers(player),
                toTriggerRefs(Collections.singletonList(ConditionTrigger.DROP)),
                null, sourceEvent, eventItem, before, after, false);
        handSnapshots.put(player.getUniqueId(), after);
    }

    private void handlePickUpTrigger(Player player, Object sourceEvent, ItemStack eventItem) {
        HandSnapshot before = snapshotBefore(player);
        HandSnapshot after = snapshotAfter(player);
        applyTriggers(player, playerPowerManager.getPowers(player),
                toTriggerRefs(Collections.singletonList(ConditionTrigger.PICK_UP)),
                null, sourceEvent, eventItem, before, after, false);
        handSnapshots.put(player.getUniqueId(), after);
    }

    private ItemStack extractItemStack(Object itemEntity) {
        if (itemEntity == null) {
            return null;
        }
        try {
            Method getItemStack = itemEntity.getClass().getMethod("getItemStack");
            Object stack = getItemStack.invoke(itemEntity);
            if (stack instanceof ItemStack) {
                return PlayerItemAccess.cloneOrNull((ItemStack) stack);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Failed to read event item stack for condition trigger.", ex);
        }
        return null;
    }

    private boolean registerModernPickupListener() {
        if (plugin == null) {
            return false;
        }
        try {
            final Class<?> eventClass = Class.forName("org.bukkit.event.entity.EntityPickupItemEvent");
            final Method getEntity = eventClass.getMethod("getEntity");
            final Method getItem = eventClass.getMethod("getItem");
            @SuppressWarnings("unchecked")
            Class<? extends Event> typed = (Class<? extends Event>) eventClass.asSubclass(Event.class);
            Bukkit.getPluginManager().registerEvent(typed, this, EventPriority.MONITOR, (listener, event) -> {
                if (!eventClass.isInstance(event)) {
                    return;
                }
                if (event instanceof Cancellable && ((Cancellable) event).isCancelled()) {
                    return;
                }
                try {
                    Object entityObj = getEntity.invoke(event);
                    if (!(entityObj instanceof Player)) {
                        return;
                    }
                    Player player = (Player) entityObj;
                    Object itemObj = getItem.invoke(event);
                    ItemStack itemStack = extractItemStack(itemObj);
                    handlePickUpTrigger(player, event, itemStack);
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "Failed to handle EntityPickupItemEvent condition trigger.", ex);
                }
            }, plugin, true);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Failed to register EntityPickupItemEvent listener.", ex);
            return false;
        }
    }

    private Iterable<Player> getOnlinePlayers() {
        try {
            Method method = Bukkit.class.getMethod("getOnlinePlayers");
            Object result = method.invoke(null);
            if (result instanceof Player[]) {
                return Arrays.asList((Player[]) result);
            }
            if (result instanceof Collection) {
                @SuppressWarnings("unchecked")
                Collection<Player> players = (Collection<Player>) result;
                return players;
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to enumerate online players for condition ticks.", ex);
        }
        return Collections.emptyList();
    }

    private static final class ConditionResult {
        private final boolean enabled;
        private final boolean triggered;

        private ConditionResult(boolean enabled, boolean triggered) {
            this.enabled = enabled;
            this.triggered = triggered;
        }
    }

    private static final class AbilityState {
        private volatile boolean enabled;

        private AbilityState(boolean enabled) {
            this.enabled = enabled;
        }
    }

    private static final class TriggerRef {
        private final ConditionTrigger trigger;
        private final String triggerKey;

        private TriggerRef(ConditionTrigger trigger, String triggerKey) {
            this.trigger = trigger;
            this.triggerKey = triggerKey;
        }

        private static TriggerRef of(ConditionTrigger trigger) {
            return new TriggerRef(trigger, trigger == null ? null : trigger.name());
        }

        private static TriggerRef custom(String triggerKey) {
            return new TriggerRef(null, triggerKey);
        }
    }

    private static final class AbilityKey {
        private final String powerId;
        private final String abilityId;

        private AbilityKey(String powerId, String abilityId) {
            this.powerId = powerId;
            this.abilityId = abilityId;
        }

        private static AbilityKey of(Power power, Ability ability) {
            String powerId = power == null ? "" : power.getId();
            String abilityId = ability == null ? "" : ability.getId();
            return new AbilityKey(powerId, abilityId);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof AbilityKey)) {
                return false;
            }
            AbilityKey that = (AbilityKey) other;
            return this.powerId.equals(that.powerId) && this.abilityId.equals(that.abilityId);
        }

        @Override
        public int hashCode() {
            return 31 * powerId.hashCode() + abilityId.hashCode();
        }
    }

    private static final class HandSnapshot {
        private final ItemStack mainHand;
        private final ItemStack offHand;

        private HandSnapshot(ItemStack mainHand, ItemStack offHand) {
            this.mainHand = PlayerItemAccess.cloneOrNull(mainHand);
            this.offHand = PlayerItemAccess.cloneOrNull(offHand);
        }

        private static HandSnapshot empty() {
            return new HandSnapshot(null, null);
        }

        private static HandSnapshot capture(Player player) {
            return new HandSnapshot(PlayerItemAccess.getMainHand(player), PlayerItemAccess.getOffHand(player));
        }
    }
}
