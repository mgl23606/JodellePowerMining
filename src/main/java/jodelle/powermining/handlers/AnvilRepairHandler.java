package jodelle.powermining.handlers;

import jodelle.powermining.PowerMining;
import jodelle.powermining.listeners.AnvilRepairListener;

import javax.annotation.Nonnull;

public class AnvilRepairHandler {
    public AnvilRepairHandler() {}
    private AnvilRepairListener listener;

    public void Init(@Nonnull PowerMining plugin) {
        listener = new AnvilRepairListener(plugin);
    }

    @Nonnull
    public AnvilRepairListener getListener() {
        return listener;
    }
}
