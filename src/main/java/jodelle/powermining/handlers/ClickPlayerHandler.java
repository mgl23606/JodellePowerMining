package jodelle.powermining.handlers;

import jodelle.powermining.PowerMining;
import jodelle.powermining.listeners.ClickPlayerListener;

public class ClickPlayerHandler {

    public ClickPlayerHandler() {}
    public ClickPlayerListener listener;

    public void Init(PowerMining plugin) {
        listener = new ClickPlayerListener(plugin);
    }

    public ClickPlayerListener getListener() {
        return listener;
    }
}
