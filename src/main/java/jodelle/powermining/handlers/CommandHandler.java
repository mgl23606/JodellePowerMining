package jodelle.powermining.handlers;

import jodelle.powermining.PowerMining;
import jodelle.powermining.listeners.CommandListener;

public class CommandHandler {
    public CommandHandler() {}
    public CommandListener listener;

    public void Init(PowerMining plugin){
        listener = new CommandListener(plugin);
    }

    public CommandListener getListener() {
        return listener;
    }
}
