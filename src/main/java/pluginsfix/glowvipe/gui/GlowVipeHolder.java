package pluginsfix.glowvipe.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import pluginsfix.glowvipe.domain.VaultData;

import java.util.UUID;

public final class GlowVipeHolder implements InventoryHolder {

    private final UUID playerId;
    private final VaultData data;
    private Inventory inventory;

    public GlowVipeHolder(UUID playerId, VaultData data) {
        this.playerId = playerId;
        this.data = data;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public VaultData getData() {
        return data;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
