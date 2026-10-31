package jodelle.powermining.lib;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class ChatUtils {

    private static final String PREFIX = "<dark_gray>[<gold>JodelleLoreSetter<dark_gray>] ";
    private static final MiniMessage MM = MiniMessage.miniMessage();

    public enum Type {
        SUCCESS("<green>"),
        WARNING("<yellow>"),
        ERROR("<red>");

        private final String color;
        Type(String color) { this.color = color; }
    }

    public static Component format(String content, Type type) {
        return MM.deserialize(PREFIX + type.color + content);
    }

    public static String formatRaw(String content) {
        Component comp = MM.deserialize(PREFIX + content);
        return PlainTextComponentSerializer.plainText().serialize(comp);
    }
}
