# Powers API (Aura SMP Foundation)

This project provides a cross-version (Java 8, Spigot 1.8.8+) API for building powers and abilities.
It includes:

- Active abilities with automatic right-click, shift-right-click, left-click, shift-left-click dispatch.
- Passive abilities with built-in event routing (join, quit, damage, death, kill, tick).
- Cooldowns with long-duration support.
- Power metadata (rarity, alignment, description, icon key).
- Built-in passive and active abilities (potion effects, healing, dashes, shockwaves, fireballs, damage modifiers).

## Build

```bash
./gradlew build
```

Output JARs are in `build/libs/`.

## JitPack

Gradle example:

```gradle
repositories {
    maven { url "https://jitpack.io" }
}

dependencies {
    implementation "com.github.<YourUser>:<YourRepo>:<Tag>"
}
```

Maven example:

```xml
<repositories>
  <repository>
    <id>jitpack</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependencies>
  <dependency>
    <groupId>com.github.&lt;YourUser&gt;</groupId>
    <artifactId>&lt;YourRepo&gt;</artifactId>
    <version>&lt;Tag&gt;</version>
  </dependency>
</dependencies>
```

## Quick Start

Create a power with active and passive abilities:

```java
Power fireAura = Power.builder("aura:fire", "Fire Aura")
    .setRarity(api.getPowerRarityRegistry().get("Rare"))
    .setAlignment(PowerAlignment.DARK)
    .addPassive(new PotionEffectPassiveAbility(
        "aura:fire_resist", "Fire Resistance", "fire_resistance", 0
    ))
    .setRightClick(new FlameDashAbility())
    .setShiftRightClick(new InfernoBurstAbility())
    .setUltimate(new SupernovaAbility())
    .build();
```

Assign it to a player:

```java
api.getPowerRegistry().registerPower(fireAura);
api.getPlayerPowerManager().addPower(player, fireAura);
```

## Built-in Abilities

Passive abilities:
- PotionEffectPassiveAbility
- DamageMultiplierPassiveAbility
- DamageShieldPassiveAbility
- PeriodicHealPassiveAbility
- KillHealPassiveAbility
- HungerRegenPassiveAbility

Active abilities:
- HealActiveAbility
- DashActiveAbility
- ShockwaveActiveAbility
- FireballActiveAbility
- PotionEffectActiveAbility

## Passive Potion Effects

Create a passive potion ability by name, alias, or key:

```java
PassiveAbility jumpBoost = new PotionEffectPassiveAbility(
    "aura:jump", "Jump Boost", "jump_boost", 1
);
```

Supported inputs include:

- `jump_boost` (alias for `JUMP` on legacy)
- `minecraft:speed` (namespaced key on modern)
- `SPEED` (enum name)

## Notes

- The API is built against `spigot-api:1.8.8` for broad compatibility.
- Active abilities are dispatched automatically by the plugin.
- Ultimates can be triggered manually via `AbilityActivationDispatcher`.
