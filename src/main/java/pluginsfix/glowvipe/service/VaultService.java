package pluginsfix.glowvipe.service;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import pluginsfix.glowvipe.config.GlowVipeConfig;
import pluginsfix.glowvipe.domain.VaultData;
import pluginsfix.glowvipe.gui.GlowVipeMenu;
import pluginsfix.glowvipe.hook.CurrencyHook;
import pluginsfix.glowvipe.storage.VaultRepository;
import pluginsfix.glowvipe.text.Messages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class VaultService {

    private final VaultRepository repository;
    private final GlowVipeConfig config;
    private final Messages messages;
    private final CurrencyHook currencyHook;

    public VaultService(VaultRepository repository, GlowVipeConfig config, Messages messages, CurrencyHook currencyHook) {
        this.repository = repository;
        this.config = config;
        this.messages = messages;
        this.currencyHook = currencyHook;
    }

    public VaultData getVaultData(UUID playerId) {
        return repository.load(playerId);
    }

    public boolean buySlot(Player player, VaultData data, int slotNumber) {
        if (!data.canBuyNextSlot(slotNumber)) {
            messages.send(player, "buy-order-error");
            return false;
        }

        if (!currencyHook.isAvailable()) {
            messages.send(player, "economy-unavailable");
            return false;
        }

        int price = config.getPrice(slotNumber);
        if (!currencyHook.has(player.getUniqueId(), price)) {
            messages.send(player, "not-enough-currency");
            return false;
        }

        boolean withdrawn = currencyHook.withdraw(player.getUniqueId(), price);
        if (!withdrawn) {
            messages.send(player, "not-enough-currency");
            return false;
        }

        data.incrementPurchasedSlots();
        repository.save(data);
        messages.send(player, "buy-success", "slot", String.valueOf(slotNumber));
        return true;
    }

    public void pack(Player player, VaultData data, Inventory inventory, GlowVipeMenu menu) {
        if (data.getPurchasedSlots() == 0) {
            messages.send(player, "no-slots-bought");
            return;
        }

        boolean hasItems = false;
        for (int i = 0; i < data.getPurchasedSlots(); i++) {
            int slot = GlowVipeMenu.STORAGE_SLOTS[i];
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType() != Material.AIR && !menu.isGuiElement(item)) {
                data.setItemData(i, item.serializeAsBytes());
                hasItems = true;
            } else {
                data.setItemData(i, null);
            }
        }

        if (!hasItems) {
            messages.send(player, "no-items-to-pack");
            return;
        }

        data.setPacked(true);
        repository.save(data);
        messages.send(player, "pack-success");
        player.closeInventory();
    }

    public void unpack(Player player, VaultData data) {
        if (!data.isPacked()) {
            return;
        }

        List<ItemStack> itemsToGive = new ArrayList<>();
        for (int i = 0; i < VaultData.MAX_SLOTS; i++) {
            byte[] bytes = data.getItemData(i);
            if (bytes != null && bytes.length > 0) {
                try {
                    ItemStack item = ItemStack.deserializeBytes(bytes);
                    if (item.getType() != Material.AIR) {
                        itemsToGive.add(item);
                    }
                } catch (Exception ignored) {
                }
            }
        }

        for (ItemStack item : itemsToGive) {
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);
            for (ItemStack drop : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
        }

        data.resetAfterUnpack();
        repository.save(data);
        messages.send(player, "unpack-success");
        player.closeInventory();
    }

    public void saveInventoryItems(VaultData data, Inventory inventory, GlowVipeMenu menu) {
        if (data.isPacked()) {
            return;
        }

        for (int i = 0; i < data.getPurchasedSlots(); i++) {
            int slot = GlowVipeMenu.STORAGE_SLOTS[i];
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType() != Material.AIR && !menu.isGuiElement(item)) {
                data.setItemData(i, item.serializeAsBytes());
            } else {
                data.setItemData(i, null);
            }
        }
        repository.save(data);
    }
}
