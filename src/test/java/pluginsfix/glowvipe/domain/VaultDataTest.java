package pluginsfix.glowvipe.domain;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VaultDataTest {

    @Test
    void defaultVaultHasZeroSlotsAndNotPacked() {
        UUID playerId = UUID.randomUUID();
        VaultData data = new VaultData(playerId);

        assertThat(data.getPlayerId()).isEqualTo(playerId);
        assertThat(data.getPurchasedSlots()).isZero();
        assertThat(data.isPacked()).isFalse();
        assertThat(data.hasAnyItems()).isFalse();
    }

    @Test
    void canOnlyBuySlotsInSequentialOrder() {
        VaultData data = new VaultData(UUID.randomUUID());

        assertThat(data.canBuyNextSlot(1)).isTrue();
        assertThat(data.canBuyNextSlot(2)).isFalse();
        assertThat(data.canBuyNextSlot(3)).isFalse();

        data.incrementPurchasedSlots();
        assertThat(data.getPurchasedSlots()).isEqualTo(1);

        assertThat(data.canBuyNextSlot(1)).isFalse();
        assertThat(data.canBuyNextSlot(2)).isTrue();
        assertThat(data.canBuyNextSlot(3)).isFalse();

        data.incrementPurchasedSlots();
        assertThat(data.getPurchasedSlots()).isEqualTo(2);

        assertThat(data.canBuyNextSlot(3)).isTrue();

        data.incrementPurchasedSlots();
        assertThat(data.getPurchasedSlots()).isEqualTo(3);
        assertThat(data.canBuyNextSlot(4)).isFalse();
    }

    @Test
    void resetAfterUnpackClearsAllData() {
        VaultData data = new VaultData(UUID.randomUUID());
        data.setPurchasedSlots(3);
        data.setPacked(true);
        data.setItemData(0, Map.of("type", "DIAMOND_SWORD", "amount", 1));
        data.setItemData(1, Map.of("type", "GOLDEN_APPLE", "amount", 16));

        assertThat(data.hasAnyItems()).isTrue();
        assertThat(data.isPacked()).isTrue();

        data.resetAfterUnpack();

        assertThat(data.getPurchasedSlots()).isZero();
        assertThat(data.isPacked()).isFalse();
        assertThat(data.hasAnyItems()).isFalse();
        assertThat(data.getItemData(0)).isNull();
    }
}
