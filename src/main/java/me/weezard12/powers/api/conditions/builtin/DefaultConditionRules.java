package me.weezard12.powers.api.conditions.builtin;

import me.weezard12.powers.api.conditions.ConditionAction;
import me.weezard12.powers.api.conditions.ConditionRule;
import me.weezard12.powers.api.conditions.ConditionTrigger;

/**
 * Factory methods for default condition rules.
 */
public final class DefaultConditionRules {
    private DefaultConditionRules() {
    }

    public static ConditionRule onRightClick() {
        return trigger(ConditionTrigger.RIGHT_CLICK);
    }

    public static ConditionRule onShiftRightClick() {
        return trigger(ConditionTrigger.SHIFT_RIGHT_CLICK);
    }

    public static ConditionRule onAnyRightClick() {
        return trigger(ConditionTrigger.ANY_RIGHT_CLICK);
    }

    public static ConditionRule onLeftClick() {
        return trigger(ConditionTrigger.LEFT_CLICK);
    }

    public static ConditionRule onShiftLeftClick() {
        return trigger(ConditionTrigger.SHIFT_LEFT_CLICK);
    }

    public static ConditionRule onAnyLeftClick() {
        return trigger(ConditionTrigger.ANY_LEFT_CLICK);
    }

    public static ConditionRule onStartHoldHand(ItemMatcher matcher) {
        return onStartHoldHand(matcher, ConditionAction.ENABLE);
    }

    public static ConditionRule onStartHoldOffHand(ItemMatcher matcher) {
        return onStartHoldOffHand(matcher, ConditionAction.ENABLE);
    }

    public static ConditionRule onStartHoldAnyHand(ItemMatcher matcher) {
        return onStartHoldAnyHand(matcher, ConditionAction.ENABLE);
    }

    public static ConditionRule onFinishHoldHand(ItemMatcher matcher) {
        return onFinishHoldHand(matcher, ConditionAction.DISABLE);
    }

    public static ConditionRule onFinishHoldOffHand(ItemMatcher matcher) {
        return onFinishHoldOffHand(matcher, ConditionAction.DISABLE);
    }

    public static ConditionRule onFinishHoldAnyHand(ItemMatcher matcher) {
        return onFinishHoldAnyHand(matcher, ConditionAction.DISABLE);
    }

    public static ConditionRule onHoldInventory(ItemMatcher matcher) {
        return onHoldInventory(matcher, ConditionAction.SYNC);
    }

    public static ConditionRule onDrop(ItemMatcher matcher) {
        return onDrop(matcher, ConditionAction.DISABLE);
    }

    public static ConditionRule onPickUp(ItemMatcher matcher) {
        return onPickUp(matcher, ConditionAction.ENABLE);
    }

    public static ConditionRule onStartHoldHand(ItemMatcher matcher, ConditionAction action) {
        return rule(new ItemHoldTransitionCondition(ConditionTrigger.START_HOLD_HAND, matcher), action);
    }

    public static ConditionRule onStartHoldOffHand(ItemMatcher matcher, ConditionAction action) {
        return rule(new ItemHoldTransitionCondition(ConditionTrigger.START_HOLD_OFF_HAND, matcher), action);
    }

    public static ConditionRule onStartHoldAnyHand(ItemMatcher matcher, ConditionAction action) {
        return rule(new ItemHoldTransitionCondition(ConditionTrigger.START_HOLD_ANY_HAND, matcher), action);
    }

    public static ConditionRule onFinishHoldHand(ItemMatcher matcher, ConditionAction action) {
        return rule(new ItemHoldTransitionCondition(ConditionTrigger.FINISH_HOLD_HAND, matcher), action);
    }

    public static ConditionRule onFinishHoldOffHand(ItemMatcher matcher, ConditionAction action) {
        return rule(new ItemHoldTransitionCondition(ConditionTrigger.FINISH_HOLD_OFF_HAND, matcher), action);
    }

    public static ConditionRule onFinishHoldAnyHand(ItemMatcher matcher, ConditionAction action) {
        return rule(new ItemHoldTransitionCondition(ConditionTrigger.FINISH_HOLD_ANY_HAND, matcher), action);
    }

    public static ConditionRule onHoldInventory(ItemMatcher matcher, ConditionAction action) {
        return rule(new InventoryContainsItemCondition(matcher), action);
    }

    public static ConditionRule onDrop(ItemMatcher matcher, ConditionAction action) {
        return rule(new EventItemCondition(ConditionTrigger.DROP, matcher), action);
    }

    public static ConditionRule onPickUp(ItemMatcher matcher, ConditionAction action) {
        return rule(new EventItemCondition(ConditionTrigger.PICK_UP, matcher), action);
    }

    public static ConditionRule trigger(ConditionTrigger trigger) {
        return rule(new TriggerCondition(trigger), ConditionAction.TRIGGER);
    }

    public static ConditionRule trigger(ConditionTrigger trigger, ConditionAction action) {
        return rule(new TriggerCondition(trigger), action);
    }

    public static ConditionRule onCustomTrigger(String triggerKey) {
        return onCustomTrigger(triggerKey, ConditionAction.TRIGGER);
    }

    public static ConditionRule onCustomTrigger(String triggerKey, ConditionAction action) {
        return rule(new CustomTriggerCondition(triggerKey), action);
    }

    private static ConditionRule rule(me.weezard12.powers.api.conditions.AbilityCondition condition, ConditionAction action) {
        return ConditionRule.of(condition, action);
    }
}
