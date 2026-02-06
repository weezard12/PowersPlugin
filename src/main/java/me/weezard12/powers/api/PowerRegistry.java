package me.weezard12.powers.api;

import java.util.Collection;

public interface PowerRegistry {
    boolean registerPower(Power power);

    boolean unregisterPower(String id);

    Power getPower(String id);

    Collection<Power> getPowers();
}
