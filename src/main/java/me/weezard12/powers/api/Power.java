package me.weezard12.powers.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class Power {
    private final String id;
    private final String name;
    private final PowerRarity rarity;
    private final PowerAlignment alignment;
    private final String iconKey;
    private final List<String> description;
    private final List<Ability> abilities;
    private final List<Ability> passiveAbilities;
    private final Map<AbilityActivation, ActiveAbility> activeAbilities;
    private final ActiveAbility ultimateAbility;

    private Power(Builder builder) {
        this.id = normalizeId(builder.id);
        this.name = requireNonEmpty(builder.name, "name");
        this.rarity = builder.rarity == null ? PowerRarity.getDefault() : builder.rarity;
        this.alignment = builder.alignment == null ? PowerAlignment.NEUTRAL : builder.alignment;
        this.iconKey = builder.iconKey;
        this.description = Collections.unmodifiableList(copyStrings(builder.description));

        List<Ability> passive = dedupe(builder.passiveAbilities);
        EnumMap<AbilityActivation, ActiveAbility> active = new EnumMap<AbilityActivation, ActiveAbility>(AbilityActivation.class);
        for (Map.Entry<AbilityActivation, ActiveAbility> entry : builder.activeAbilities.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                active.put(entry.getKey(), entry.getValue());
            }
        }

        this.activeAbilities = Collections.unmodifiableMap(active);
        this.ultimateAbility = builder.ultimateAbility;
        this.passiveAbilities = Collections.unmodifiableList(passive);

        Set<Ability> all = new LinkedHashSet<Ability>();
        addAll(all, builder.abilities);
        addAll(all, passive);
        addAll(all, active.values());
        if (ultimateAbility != null) {
            all.add(ultimateAbility);
        }
        this.abilities = Collections.unmodifiableList(new ArrayList<Ability>(all));
    }

    public static Power of(String id, String name, Collection<? extends Ability> abilities) {
        return builder(id, name).addAbilities(abilities).build();
    }

    public static Builder builder(String id, String name) {
        return new Builder(id, name);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public PowerRarity getRarity() {
        return rarity;
    }

    public PowerAlignment getAlignment() {
        return alignment;
    }

    public String getIconKey() {
        return iconKey;
    }

    public List<String> getDescription() {
        return description;
    }

    public List<Ability> getAbilities() {
        return abilities;
    }

    public List<Ability> getPassiveAbilities() {
        return passiveAbilities;
    }

    public Map<AbilityActivation, ActiveAbility> getActiveAbilities() {
        return activeAbilities;
    }

    public ActiveAbility getActiveAbility(AbilityActivation activation) {
        if (activation == null) {
            return null;
        }
        return activeAbilities.get(activation);
    }

    public ActiveAbility getUltimateAbility() {
        return ultimateAbility;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Power)) {
            return false;
        }
        Power that = (Power) other;
        return this.id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Power{id='" + id + "', name='" + name + "', rarity=" + rarity + ", alignment=" + alignment
                + ", abilities=" + abilities.size() + "}";
    }

    private static String normalizeId(String id) {
        String value = requireNonEmpty(id, "id");
        return value.toLowerCase(Locale.ROOT);
    }

    private static String requireNonEmpty(String value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " cannot be null");
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(field + " cannot be empty");
        }
        return trimmed;
    }

    private static void addAll(Set<Ability> set, Collection<? extends Ability> abilities) {
        if (abilities == null) {
            return;
        }
        for (Ability ability : abilities) {
            if (ability != null) {
                set.add(ability);
            }
        }
    }

    private static List<Ability> dedupe(Collection<? extends Ability> abilities) {
        if (abilities == null || abilities.isEmpty()) {
            return Collections.emptyList();
        }
        LinkedHashSet<Ability> set = new LinkedHashSet<Ability>(abilities);
        return new ArrayList<Ability>(set);
    }

    private static List<String> copyStrings(Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> copy = new ArrayList<String>();
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                copy.add(value);
            }
        }
        return copy;
    }

    public static final class Builder {
        private final String id;
        private final String name;
        private PowerRarity rarity = PowerRarity.getDefault();
        private PowerAlignment alignment = PowerAlignment.NEUTRAL;
        private String iconKey;
        private final List<String> description = new ArrayList<String>();
        private final List<Ability> abilities = new ArrayList<Ability>();
        private final List<Ability> passiveAbilities = new ArrayList<Ability>();
        private final EnumMap<AbilityActivation, ActiveAbility> activeAbilities =
                new EnumMap<AbilityActivation, ActiveAbility>(AbilityActivation.class);
        private ActiveAbility ultimateAbility;

        private Builder(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public Builder setRarity(PowerRarity rarity) {
            if (rarity != null) {
                this.rarity = rarity;
            }
            return this;
        }

        public Builder setAlignment(PowerAlignment alignment) {
            if (alignment != null) {
                this.alignment = alignment;
            }
            return this;
        }

        public Builder setIconKey(String iconKey) {
            this.iconKey = iconKey;
            return this;
        }

        public Builder setDescription(Collection<String> lines) {
            this.description.clear();
            if (lines != null) {
                this.description.addAll(lines);
            }
            return this;
        }

        public Builder addDescriptionLine(String line) {
            if (line != null) {
                this.description.add(line);
            }
            return this;
        }

        public Builder addAbility(Ability ability) {
            if (ability != null) {
                abilities.add(ability);
            }
            return this;
        }

        public Builder addAbilities(Collection<? extends Ability> abilities) {
            if (abilities != null) {
                this.abilities.addAll(abilities);
            }
            return this;
        }

        public Builder addPassive(Ability ability) {
            if (ability != null) {
                passiveAbilities.add(ability);
            }
            return this;
        }

        public Builder addPassives(Collection<? extends Ability> abilities) {
            if (abilities != null) {
                passiveAbilities.addAll(abilities);
            }
            return this;
        }

        public Builder setActive(AbilityActivation activation, ActiveAbility ability) {
            if (activation != null && ability != null) {
                activeAbilities.put(activation, ability);
            }
            return this;
        }

        public Builder setRightClick(ActiveAbility ability) {
            return setActive(AbilityActivation.RIGHT_CLICK, ability);
        }

        public Builder setShiftRightClick(ActiveAbility ability) {
            return setActive(AbilityActivation.SHIFT_RIGHT_CLICK, ability);
        }

        public Builder setLeftClick(ActiveAbility ability) {
            return setActive(AbilityActivation.LEFT_CLICK, ability);
        }

        public Builder setShiftLeftClick(ActiveAbility ability) {
            return setActive(AbilityActivation.SHIFT_LEFT_CLICK, ability);
        }

        public Builder setUltimate(ActiveAbility ability) {
            this.ultimateAbility = ability;
            if (ability != null) {
                activeAbilities.put(AbilityActivation.ULTIMATE, ability);
            }
            return this;
        }

        public Power build() {
            return new Power(this);
        }
    }
}
