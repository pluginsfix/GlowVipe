package pluginsfix.glowvipe.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class VaultData {

    public static final int MAX_SLOTS = 3;

    private final UUID playerId;
    private int purchasedSlots;
    private boolean packed;
    private final Map<Integer, byte[]> items = new HashMap<>();

    public VaultData(UUID playerId) {
        this(playerId, 0, false, Collections.emptyMap());
    }

    public VaultData(UUID playerId, int purchasedSlots, boolean packed, Map<Integer, byte[]> items) {
        this.playerId = Objects.requireNonNull(playerId, "playerId");
        this.purchasedSlots = Math.max(0, Math.min(MAX_SLOTS, purchasedSlots));
        this.packed = packed;
        if (items != null) {
            this.items.putAll(items);
        }
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public int getPurchasedSlots() {
        return purchasedSlots;
    }

    public void setPurchasedSlots(int purchasedSlots) {
        this.purchasedSlots = Math.max(0, Math.min(MAX_SLOTS, purchasedSlots));
    }

    public boolean isPacked() {
        return packed;
    }

    public void setPacked(boolean packed) {
        this.packed = packed;
    }

    public boolean canBuyNextSlot(int targetSlotNumber) {
        int targetIndex = targetSlotNumber - 1;
        return targetIndex == purchasedSlots && purchasedSlots < MAX_SLOTS;
    }

    public boolean incrementPurchasedSlots() {
        if (purchasedSlots < MAX_SLOTS) {
            purchasedSlots++;
            return true;
        }
        return false;
    }

    public byte[] getItemData(int slotIndex) {
        return items.get(slotIndex);
    }

    public void setItemData(int slotIndex, byte[] data) {
        if (data == null || data.length == 0) {
            items.remove(slotIndex);
        } else {
            items.put(slotIndex, data);
        }
    }

    public boolean hasItem(int slotIndex) {
        byte[] data = items.get(slotIndex);
        return data != null && data.length > 0;
    }

    public boolean hasAnyItems() {
        return !items.isEmpty();
    }

    public Map<Integer, byte[]> getItems() {
        return Collections.unmodifiableMap(items);
    }

    public void resetAfterUnpack() {
        this.purchasedSlots = 0;
        this.packed = false;
        this.items.clear();
    }
}
