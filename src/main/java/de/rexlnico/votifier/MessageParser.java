package de.rexlnico.votifier;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MessageParser {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder().tags(StandardTags.defaults()).build();

    public static Component parse(String string) {
        if (string == null || string.isBlank()) {
            return Component.empty();
        }
        return MINI_MESSAGE.deserialize(replaceColors(string));
    }

    public static Component parse(String string, TagResolver... tagResolver) {
        if (string == null || string.isBlank()) {
            return Component.empty();
        }
        return MINI_MESSAGE.deserialize(replaceColors(string), tagResolver);
    }

    private static String replaceColors(String input) {
        input = input.replace("&0", "<#000000>").replace("§0", "<#000000>");
        input = input.replace("&1", "<#0000aa>").replace("§1", "<#0000aa>");
        input = input.replace("&2", "<#00aa00>").replace("§2", "<#00aa00>");
        input = input.replace("&3", "<#00aaaa>").replace("§3", "<#00aaaa>");
        input = input.replace("&4", "<#aa0000>").replace("§4", "<#aa0000>");
        input = input.replace("&5", "<#aa00aa>").replace("§5", "<#aa00aa>");
        input = input.replace("&6", "<#ffaa00>").replace("§6", "<#ffaa00>");
        input = input.replace("&7", "<#aaaaaa>").replace("§7", "<#aaaaaa>");
        input = input.replace("&8", "<#555555>").replace("§8", "<#555555>");
        input = input.replace("&9", "<#5555ff>").replace("§9", "<#5555ff>");
        input = input.replace("&a", "<#55ff55>").replace("§a", "<#55ff55>");
        input = input.replace("&b", "<#55ffff>").replace("§b", "<#55ffff>");
        input = input.replace("&c", "<#ff5555>").replace("§c", "<#ff5555>");
        input = input.replace("&d", "<#ff55ff>").replace("§d", "<#ff55ff>");
        input = input.replace("&e", "<#ffff55>").replace("§e", "<#ffff55>");
        input = input.replace("&f", "<#ffffff>").replace("§f", "<#ffffff>");

        input = input.replace("&k", "<obfuscated>").replace("§k", "<obfuscated>");
        input = input.replace("&l", "<bold>").replace("§l", "<bold>");
        input = input.replace("&m", "<strikethrough>").replace("§m", "<strikethrough>");
        input = input.replace("&n", "<underlined>").replace("§n", "<underlined>");
        input = input.replace("&o", "<italic>").replace("§o", "<italic>");

        input = input.replace("&r", "<reset>").replace("§r", "<reset>");
        return input;
    }

}