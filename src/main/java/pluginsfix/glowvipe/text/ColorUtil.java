package pluginsfix.glowvipe.text;

import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    private ColorUtil() {
    }

    public static String colorize(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer(text.length() + 32);

        while (matcher.find()) {
            String hex = matcher.group(1);
            ChatColor color = ChatColor.of("#" + hex);
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(color.toString()));
        }
        matcher.appendTail(buffer);

        return org.bukkit.ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    public static List<String> colorizeList(List<String> list) {
        if (list == null) {
            return List.of();
        }
        List<String> result = new ArrayList<>(list.size());
        for (String line : list) {
            result.add(colorize("§r" + line));
        }
        return result;
    }
}
