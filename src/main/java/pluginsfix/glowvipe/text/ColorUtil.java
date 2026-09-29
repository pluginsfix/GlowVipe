package pluginsfix.glowvipe.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexCharacter('#')
            .hexColors()
            .build();

    private ColorUtil() {
    }

    public static String colorize(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder buffer = new StringBuilder(text.length() + 32);

        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char ch : hex.toCharArray()) {
                replacement.append('§').append(ch);
            }
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement.toString()));
        }
        matcher.appendTail(buffer);

        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    public static List<String> colorizeList(List<String> list) {
        if (list == null) {
            return List.of();
        }
        List<String> result = new ArrayList<>(list.size());
        for (String line : list) {
            result.add(colorize(line));
        }
        return result;
    }

    public static Component toComponent(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty().decoration(TextDecoration.ITALIC, false);
        }
        return SERIALIZER.deserialize(text).decoration(TextDecoration.ITALIC, false);
    }

    public static List<Component> toComponentList(List<String> list) {
        if (list == null) {
            return List.of();
        }
        List<Component> components = new ArrayList<>(list.size());
        for (String line : list) {
            components.add(toComponent(line));
        }
        return components;
    }
}
