package jodelle.powermining.handlers;

import jodelle.powermining.PowerMining;
import jodelle.powermining.listeners.CommandListener;

import javax.annotation.Nonnull;

public class CommandHandler {
    public CommandHandler() {}
    public CommandListener listener;

    public void Init(@Nonnull PowerMining plugin){
        listener = new CommandListener(plugin);
    }

    @Nonnull
    public CommandListener getListener() {
        return listener;
    }
}
