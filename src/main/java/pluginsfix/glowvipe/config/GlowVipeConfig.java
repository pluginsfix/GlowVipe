package pluginsfix.glowvipe.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class GlowVipeConfig {

    private final Plugin plugin;
    private final File file;
    private String economyType;
    private String currencyName;
    private final Map<Integer, Integer> slotPrices = new HashMap<>();
    private GuiConfig guiConfig;

    public GlowVipeConfig(Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "config.yml");
        reload();
    }

    public void reload() {
        if (!file.exists()) {
            plugin.saveResource("config.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        InputStream defaultStream = plugin.getResource("config.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            config.setDefaults(defaultConfig);
        }

        this.economyType = config.getString("economy.type", "playerpoints").toLowerCase();
        this.currencyName = config.getString("economy.currency-name", "Points");

        this.slotPrices.clear();
        ConfigurationSection priceSection = config.getConfigurationSection("prices");
        if (priceSection != null) {
            for (String key : priceSection.getKeys(false)) {
                int price = priceSection.getInt(key);
                if (key.startsWith("slot-") || key.startsWith("slot_")) {
                    try {
                        int slotNum = Integer.parseInt(key.substring(5));
                        this.slotPrices.put(slotNum, price);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        this.guiConfig = GuiConfig.fromSection(config.getConfigurationSection("gui"));
    }

    public String getEconomyType() {
        return economyType;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public int getPrice(int slotNumber) {
        return slotPrices.getOrDefault(slotNumber, 100 * slotNumber);
    }

    public GuiConfig getGuiConfig() {
        return guiConfig;
    }
}
