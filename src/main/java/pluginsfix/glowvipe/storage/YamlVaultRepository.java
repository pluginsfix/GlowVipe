package pluginsfix.glowvipe.storage;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import pluginsfix.glowvipe.domain.VaultData;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public final class YamlVaultRepository implements VaultRepository {

    private final Plugin plugin;
    private final File file;
    private final YamlConfiguration config;
    private final Map<UUID, VaultData> cache = new ConcurrentHashMap<>();
    private final AtomicBoolean dirty = new AtomicBoolean(false);

    public YamlVaultRepository(Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException ignored) {
            }
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    @Override
    public VaultData load(UUID playerId) {
        VaultData cached = cache.get(playerId);
        if (cached != null) {
            return cached;
        }

        String key = playerId.toString();
        if (!config.contains(key)) {
            VaultData fresh = new VaultData(playerId);
            cache.put(playerId, fresh);
            return fresh;
        }

        int purchasedSlots = config.getInt(key + ".purchased-slots", 0);
        boolean packed = config.getBoolean(key + ".packed", false);
        Map<Integer, Map<String, Object>> items = new HashMap<>();

        for (int i = 0; i < VaultData.MAX_SLOTS; i++) {
            String slotKey = key + ".slots." + i;
            if (config.isConfigurationSection(slotKey)) {
                ConfigurationSection section = config.getConfigurationSection(slotKey);
                if (section != null) {
                    items.put(i, section.getValues(true));
                }
            } else if (config.isItemStack(slotKey)) {
                ItemStack item = config.getItemStack(slotKey);
                if (item != null) {
                    items.put(i, item.serialize());
                }
            }
        }

        VaultData data = new VaultData(playerId, purchasedSlots, packed, items);
        cache.put(playerId, data);
        return data;
    }

    @Override
    public void save(VaultData data) {
        cache.put(data.getPlayerId(), data);
        String key = data.getPlayerId().toString();

        if (data.getPurchasedSlots() == 0 && !data.isPacked() && !data.hasAnyItems()) {
            config.set(key, null);
        } else {
            config.set(key + ".purchased-slots", data.getPurchasedSlots());
            config.set(key + ".packed", data.isPacked());
            for (int i = 0; i < VaultData.MAX_SLOTS; i++) {
                Map<String, Object> itemData = data.getItemData(i);
                if (itemData != null && !itemData.isEmpty()) {
                    config.set(key + ".slots." + i, itemData);
                } else {
                    config.set(key + ".slots." + i, null);
                }
            }
        }

        dirty.set(true);
        flushAsync();
    }

    @Override
    public void saveAll() {
        for (VaultData data : cache.values()) {
            String key = data.getPlayerId().toString();
            if (data.getPurchasedSlots() == 0 && !data.isPacked() && !data.hasAnyItems()) {
                config.set(key, null);
            } else {
                config.set(key + ".purchased-slots", data.getPurchasedSlots());
                config.set(key + ".packed", data.isPacked());
                for (int i = 0; i < VaultData.MAX_SLOTS; i++) {
                    Map<String, Object> itemData = data.getItemData(i);
                    if (itemData != null && !itemData.isEmpty()) {
                        config.set(key + ".slots." + i, itemData);
                    } else {
                        config.set(key + ".slots." + i, null);
                    }
                }
            }
        }
        flushSync();
    }

    public void unload(UUID playerId) {
        VaultData data = cache.remove(playerId);
        if (data != null) {
            save(data);
        }
    }

    private void flushAsync() {
        if (!plugin.isEnabled()) {
            flushSync();
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::flushSync);
    }

    private synchronized void flushSync() {
        if (!dirty.getAndSet(false)) {
            return;
        }

        try {
            config.save(file);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void close() {
        saveAll();
    }
}
