package pluginsfix.glowvipe.text;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Messages {

    private final Plugin plugin;
    private final File file;
    private final Map<String, List<String>> messageMap = new HashMap<>();

    public Messages(Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "messages.yml");
        reload();
    }

    public void reload() {
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        InputStream defaultStream = plugin.getResource("messages.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            config.setDefaults(defaultConfig);
        }

        messageMap.clear();
        for (String key : config.getKeys(true)) {
            if (config.isList(key)) {
                messageMap.put(key, config.getStringList(key));
            } else if (config.isString(key)) {
                messageMap.put(key, List.of(config.getString(key, "")));
            }
        }
    }

    public void send(CommandSender sender, String key) {
        send(sender, key, Collections.emptyMap());
    }

    public void send(CommandSender sender, String key, String placeholderKey, String placeholderValue) {
        send(sender, key, Map.of(placeholderKey, placeholderValue));
    }

    public void send(CommandSender sender, String key, Map<String, String> placeholders) {
        List<String> lines = messageMap.get(key);
        if (lines == null || lines.isEmpty()) {
            return;
        }

        for (String line : lines) {
            String processed = applyPlaceholders(line, placeholders);
            dispatchLine(sender, processed);
        }
    }

    public List<String> getLines(String key, Map<String, String> placeholders) {
        List<String> lines = messageMap.get(key);
        if (lines == null) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>(lines.size());
        for (String line : lines) {
            result.add(applyPlaceholders(line, placeholders));
        }
        return result;
    }

    private void dispatchLine(CommandSender sender, String line) {
        if (line.startsWith("[sound] ")) {
            if (sender instanceof Player player) {
                playSound(player, line.substring(8).trim());
            }
            return;
        }

        if (line.startsWith("[actionbar] ")) {
            if (sender instanceof Player player) {
                String text = ColorUtil.colorize(line.substring(12));
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(text));
            }
            return;
        }

        String text = line.startsWith("[message] ") ? line.substring(10) : line;
        sender.sendMessage(ColorUtil.colorize(text));
    }

    private void playSound(Player player, String soundToken) {
        String[] parts = soundToken.split(":");
        String soundName = parts[0].trim();
        float volume = 1.0f;
        float pitch = 1.0f;

        if (parts.length > 1) {
            try {
                volume = Float.parseFloat(parts[1].trim());
            } catch (NumberFormatException ignored) {
            }
        }
        if (parts.length > 2) {
            try {
                pitch = Float.parseFloat(parts[2].trim());
            } catch (NumberFormatException ignored) {
            }
        }

        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException ignored) {
        }
    }

    private String applyPlaceholders(String text, Map<String, String> placeholders) {
        if (placeholders.isEmpty()) {
            return text;
        }
        String result = text;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue())
                           .replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return result;
    }
}
