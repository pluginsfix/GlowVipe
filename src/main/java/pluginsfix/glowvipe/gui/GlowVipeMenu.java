package pluginsfix.glowvipe.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import pluginsfix.glowvipe.config.GlowVipeConfig;
import pluginsfix.glowvipe.config.GuiConfig;
import pluginsfix.glowvipe.domain.VaultData;
import pluginsfix.glowvipe.text.ColorUtil;

import java.util.ArrayList;
import java.util.List;

public final class GlowVipeMenu {

    public static final int[] STORAGE_SLOTS = {11, 12, 13};
    public static final int ACTION_BUTTON_SLOT = 15;
    public static final int INVENTORY_SIZE = 27;

    private final Plugin plugin;
    private final GlowVipeConfig config;
    private final NamespacedKey elementKey;

    public GlowVipeMenu(Plugin plugin, GlowVipeConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.elementKey = new NamespacedKey(plugin, "gui_element");
    }

    public NamespacedKey getElementKey() {
        return elementKey;
    }

    public void open(Player player, VaultData data) {
        GuiConfig guiConfig = config.getGuiConfig();
        Component titleComponent = ColorUtil.toComponent(guiConfig.title());

        GlowVipeHolder holder = new GlowVipeHolder(player.getUniqueId(), data);
        Inventory inventory = Bukkit.createInventory(holder, INVENTORY_SIZE, titleComponent);
        holder.setInventory(inventory);

        ItemStack filler = createFillerItem();
        for (int i = 0; i < INVENTORY_SIZE; i++) {
            inventory.setItem(i, filler);
        }

        renderStorageSlots(inventory, data);
        renderActionButton(inventory, data);

        player.openInventory(inventory);
    }

    public void renderStorageSlots(Inventory inventory, VaultData data) {
        for (int i = 0; i < VaultData.MAX_SLOTS; i++) {
            int slot = STORAGE_SLOTS[i];
            if (i < data.getPurchasedSlots()) {
                byte[] itemBytes = data.getItemData(i);
                if (itemBytes != null && itemBytes.length > 0) {
                    ItemStack realItem = ItemStack.deserializeBytes(itemBytes);
                    inventory.setItem(slot, realItem);
                } else {
                    if (data.isPacked()) {
                        inventory.setItem(slot, new ItemStack(Material.AIR));
                    } else {
                        inventory.setItem(slot, createSlotFreeItem());
                    }
                }
            } else {
                int price = config.getPrice(i + 1);
                inventory.setItem(slot, createBuySlotItem(i + 1, price));
            }
        }
    }

    public void renderActionButton(Inventory inventory, VaultData data) {
        if (!data.isPacked()) {
            inventory.setItem(ACTION_BUTTON_SLOT, createPackButtonItem());
        } else {
            inventory.setItem(ACTION_BUTTON_SLOT, createUnpackButtonItem());
        }
    }

    public ItemStack createFillerItem() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(" ").decoration(TextDecoration.ITALIC, false));
            meta.getPersistentDataContainer().set(elementKey, PersistentDataType.STRING, "filler");
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createSlotFreeItem() {
        GuiConfig guiConfig = config.getGuiConfig();
        ItemStack item = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ColorUtil.toComponent(guiConfig.slotFreeName()));
            List<Component> lore = ColorUtil.toComponentList(guiConfig.slotFreeLore());
            meta.lore(lore);
            meta.getPersistentDataContainer().set(elementKey, PersistentDataType.STRING, "slot_free");
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createBuySlotItem(int slotNumber, int price) {
        GuiConfig guiConfig = config.getGuiConfig();
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String nameText = guiConfig.buySlotName()
                    .replace("{slot}", String.valueOf(slotNumber))
                    .replace("%slot%", String.valueOf(slotNumber));
            meta.displayName(ColorUtil.toComponent(nameText));

            List<String> rawLore = guiConfig.buySlotLore();
            List<Component> lore = new ArrayList<>(rawLore.size());
            for (String line : rawLore) {
                String processed = line.replace("{slot}", String.valueOf(slotNumber))
                        .replace("%slot%", String.valueOf(slotNumber))
                        .replace("{price}", String.valueOf(price))
                        .replace("%price%", String.valueOf(price))
                        .replace("{currency}", config.getCurrencyName())
                        .replace("%currency%", config.getCurrencyName());
                lore.add(ColorUtil.toComponent(processed));
            }
            meta.lore(lore);
            meta.getPersistentDataContainer().set(elementKey, PersistentDataType.STRING, "buy_slot_" + slotNumber);
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createPackButtonItem() {
        GuiConfig guiConfig = config.getGuiConfig();
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ColorUtil.toComponent(guiConfig.buttonPackName()));
            meta.lore(ColorUtil.toComponentList(guiConfig.buttonPackLore()));
            meta.getPersistentDataContainer().set(elementKey, PersistentDataType.STRING, "button_pack");
            item.setItemMeta(meta);
        }
        return item;
    }

    public ItemStack createUnpackButtonItem() {
        GuiConfig guiConfig = config.getGuiConfig();
        ItemStack item = new ItemStack(Material.TRAPPED_CHEST);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(ColorUtil.toComponent(guiConfig.buttonUnpackName()));
            meta.lore(ColorUtil.toComponentList(guiConfig.buttonUnpackLore()));
            meta.getPersistentDataContainer().set(elementKey, PersistentDataType.STRING, "button_unpack");
            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isGuiElement(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(elementKey, PersistentDataType.STRING);
    }

    public boolean isFiller(ItemStack item) {
        return isElementType(item, "filler");
    }

    public boolean isSlotFree(ItemStack item) {
        return isElementType(item, "slot_free");
    }

    public boolean isBuySlot(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        String value = meta.getPersistentDataContainer().get(elementKey, PersistentDataType.STRING);
        return value != null && value.startsWith("buy_slot_");
    }

    private boolean isElementType(ItemStack item, String expectedType) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        String value = meta.getPersistentDataContainer().get(elementKey, PersistentDataType.STRING);
        return expectedType.equals(value);
    }
}
