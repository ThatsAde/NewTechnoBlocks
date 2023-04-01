package it.technoade.technoblocks.utils;

import it.technoade.technoblocks.Main;

import java.util.Objects;

public enum Messages {
    SELECTORTITLE(Utils.color(Objects.requireNonNull(Main.plugin.getConfig().getString("itemselector.displayname")))),
    SELECTEDITEM(Utils.color(Objects.requireNonNull(Main.plugin.getConfig().getString("messages.blockselected")))),
    NO_PERMS(Utils.color(Objects.requireNonNull(Main.plugin.getConfig().getString("messages.noperms"))));

    private final String string;

    Messages(String string) {
        this.string = string;
    }

    public String getString() {
        return this.string;
    }
}
