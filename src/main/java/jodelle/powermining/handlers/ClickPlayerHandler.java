package jodelle.powermining.handlers;

import jodelle.powermining.PowerMining;
import jodelle.powermining.listeners.ClickPlayerListener;

import javax.annotation.Nonnull;

public class ClickPlayerHandler {

    public ClickPlayerHandler() {}
    public ClickPlayerListener listener;

    public void Init(@Nonnull PowerMining plugin) {
        listener = new ClickPlayerListener(plugin);
    }

    @Nonnull
    public ClickPlayerListener getListener() {
        return listener;
    }
}
