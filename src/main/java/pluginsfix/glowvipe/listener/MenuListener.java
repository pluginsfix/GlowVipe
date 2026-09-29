package pluginsfix.glowvipe.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import pluginsfix.glowvipe.domain.VaultData;
import pluginsfix.glowvipe.gui.GlowVipeHolder;
import pluginsfix.glowvipe.gui.GlowVipeMenu;
import pluginsfix.glowvipe.service.VaultService;
import pluginsfix.glowvipe.text.Messages;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class MenuListener implements Listener {

    private final VaultService vaultService;
    private final GlowVipeMenu menu;
    private final Messages messages;
    private final Map<UUID, Long> clickDebounce = new ConcurrentHashMap<>();

    public MenuListener(VaultService vaultService, GlowVipeMenu menu, Messages messages) {
        this.vaultService = vaultService;
        this.menu = menu;
        this.messages = messages;
    }

    public void removePlayer(UUID playerId) {
        clickDebounce.remove(playerId);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof GlowVipeHolder holder)) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int rawSlot = event.getRawSlot();
        if (rawSlot < 0) {
            return;
        }

        long now = System.currentTimeMillis();
        long lastClick = clickDebounce.getOrDefault(player.getUniqueId(), 0L);
        if (now - lastClick < 150) {
            event.setCancelled(true);
            return;
        }
        clickDebounce.put(player.getUniqueId(), now);

        VaultData data = holder.getData();
        Inventory topInventory = event.getInventory();

        if (rawSlot < GlowVipeMenu.INVENTORY_SIZE) {
            event.setCancelled(true);

            if (rawSlot == GlowVipeMenu.ACTION_BUTTON_SLOT) {
                if (!data.isPacked()) {
                    vaultService.pack(player, data, topInventory, menu);
                } else {
                    vaultService.unpack(player, data);
                }
                return;
            }

            int storageIndex = getStorageSlotIndex(rawSlot);
            if (storageIndex == -1) {
                return;
            }

            if (storageIndex >= data.getPurchasedSlots()) {
                boolean bought = vaultService.buySlot(player, data, storageIndex + 1);
                if (bought) {
                    menu.renderStorageSlots(topInventory, data);
                    menu.renderActionButton(topInventory, data);
                    player.updateInventory();
                }
                return;
            }

            if (data.isPacked()) {
                messages.send(player, "already-packed");
                return;
            }

            handleStorageSlotInteraction(event, player, topInventory, rawSlot, data);
            return;
        }

        if (event.isShiftClick()) {
            event.setCancelled(true);
            handleBottomInventoryShiftClick(event, player, topInventory, data);
            return;
        }

        event.setCancelled(false);
    }

    private void handleStorageSlotInteraction(InventoryClickEvent event, Player player, Inventory topInventory, int rawSlot, VaultData data) {
        if (event.getClick().isKeyboardClick()) {
            int hotbarButton = event.getHotbarButton();
            if (hotbarButton >= 0 && hotbarButton <= 8) {
                ItemStack hotbarItem = player.getInventory().getItem(hotbarButton);
                ItemStack current = topInventory.getItem(rawSlot);

                if (menu.isSlotFree(current)) {
                    if (hotbarItem != null && hotbarItem.getType() != Material.AIR) {
                        topInventory.setItem(rawSlot, hotbarItem.clone());
                        player.getInventory().setItem(hotbarButton, null);
                    }
                } else if (current != null && !menu.isGuiElement(current)) {
                    player.getInventory().setItem(hotbarButton, current.clone());
                    if (hotbarItem != null && hotbarItem.getType() != Material.AIR) {
                        topInventory.setItem(rawSlot, hotbarItem.clone());
                    } else {
                        topInventory.setItem(rawSlot, menu.createSlotFreeItem());
                    }
                }
                vaultService.saveInventoryItems(data, topInventory, menu);
                player.updateInventory();
            }
            return;
        }

        ItemStack cursor = event.getCursor();
        ItemStack current = topInventory.getItem(rawSlot);

        if (menu.isSlotFree(current)) {
            if (cursor != null && cursor.getType() != Material.AIR) {
                if (event.isLeftClick()) {
                    topInventory.setItem(rawSlot, cursor.clone());
                    event.getView().setCursor(null);
                } else if (event.isRightClick()) {
                    ItemStack single = cursor.clone();
                    single.setAmount(1);
                    cursor.setAmount(cursor.getAmount() - 1);
                    topInventory.setItem(rawSlot, single);
                    event.getView().setCursor(cursor.getAmount() > 0 ? cursor : null);
                }
            }
        } else if (current != null && !menu.isGuiElement(current)) {
            if (cursor == null || cursor.getType() == Material.AIR) {
                if (event.isLeftClick() || event.isShiftClick()) {
                    event.getView().setCursor(current.clone());
                    topInventory.setItem(rawSlot, menu.createSlotFreeItem());
                } else if (event.isRightClick()) {
                    int half = (current.getAmount() + 1) / 2;
                    ItemStack halfItem = current.clone();
                    halfItem.setAmount(half);
                    current.setAmount(current.getAmount() - half);
                    event.getView().setCursor(halfItem);
                    topInventory.setItem(rawSlot, current.getAmount() > 0 ? current : menu.createSlotFreeItem());
                }
            } else {
                if (cursor.isSimilar(current)) {
                    int max = current.getMaxStackSize();
                    int space = max - current.getAmount();
                    if (space > 0) {
                        int toAdd = event.isRightClick() ? 1 : Math.min(space, cursor.getAmount());
                        toAdd = Math.min(toAdd, cursor.getAmount());
                        current.setAmount(current.getAmount() + toAdd);
                        cursor.setAmount(cursor.getAmount() - toAdd);
                        topInventory.setItem(rawSlot, current);
                        event.getView().setCursor(cursor.getAmount() > 0 ? cursor : null);
                    }
                } else if (event.isLeftClick()) {
                    ItemStack temp = current.clone();
                    topInventory.setItem(rawSlot, cursor.clone());
                    event.getView().setCursor(temp);
                }
            }
        }

        vaultService.saveInventoryItems(data, topInventory, menu);
        player.updateInventory();
    }

    private void handleBottomInventoryShiftClick(InventoryClickEvent event, Player player, Inventory topInventory, VaultData data) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        if (data.isPacked()) {
            messages.send(player, "already-packed");
            return;
        }

        for (int i = 0; i < data.getPurchasedSlots(); i++) {
            int topSlot = GlowVipeMenu.STORAGE_SLOTS[i];
            ItemStack topItem = topInventory.getItem(topSlot);
            if (topItem != null && !menu.isGuiElement(topItem) && topItem.isSimilar(clicked)) {
                int space = topItem.getMaxStackSize() - topItem.getAmount();
                if (space > 0) {
                    int toMove = Math.min(space, clicked.getAmount());
                    topItem.setAmount(topItem.getAmount() + toMove);
                    clicked.setAmount(clicked.getAmount() - toMove);
                    topInventory.setItem(topSlot, topItem);
                    event.setCurrentItem(clicked.getAmount() > 0 ? clicked : null);
                    vaultService.saveInventoryItems(data, topInventory, menu);
                    player.updateInventory();
                    if (clicked.getAmount() == 0) {
                        return;
                    }
                }
            }
        }

        for (int i = 0; i < data.getPurchasedSlots(); i++) {
            int topSlot = GlowVipeMenu.STORAGE_SLOTS[i];
            ItemStack topItem = topInventory.getItem(topSlot);
            if (menu.isSlotFree(topItem) || topItem == null || topItem.getType() == Material.AIR) {
                topInventory.setItem(topSlot, clicked.clone());
                event.setCurrentItem(null);
                vaultService.saveInventoryItems(data, topInventory, menu);
                player.updateInventory();
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getInventory().getHolder() instanceof GlowVipeHolder)) {
            return;
        }

        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot < GlowVipeMenu.INVENTORY_SIZE) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof GlowVipeHolder holder)) {
            return;
        }

        vaultService.saveInventoryItems(holder.getData(), event.getInventory(), menu);
    }

    private int getStorageSlotIndex(int rawSlot) {
        for (int i = 0; i < GlowVipeMenu.STORAGE_SLOTS.length; i++) {
            if (GlowVipeMenu.STORAGE_SLOTS[i] == rawSlot) {
                return i;
            }
        }
        return -1;
    }
}
