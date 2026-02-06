package me.weezard12.powers.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class Power {
    private final String id;
    private final String name;
    private final List<Ability> abilities;

    private Power(String id, String name, Collection<? extends Ability> abilities) {
        this.id = normalizeId(id);
        this.name = requireNonEmpty(name, "name");
        if (abilities == null) {
            this.abilities = Collections.emptyList();
        } else {
            this.abilities = Collections.unmodifiableList(new ArrayList<Ability>(abilities));
        }
    }

    public static Power of(String id, String name, Collection<? extends Ability> abilities) {
        return new Power(id, name, abilities);
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

    public List<Ability> getAbilities() {
        return abilities;
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
        return "Power{id='" + id + "', name='" + name + "', abilities=" + abilities.size() + "}";
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

    public static final class Builder {
        private final String id;
        private final String name;
        private final List<Ability> abilities = new ArrayList<Ability>();

        private Builder(String id, String name) {
            this.id = id;
            this.name = name;
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

        public Power build() {
            return new Power(id, name, abilities);
        }
    }
}
