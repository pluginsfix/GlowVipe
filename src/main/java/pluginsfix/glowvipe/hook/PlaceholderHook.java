package pluginsfix.glowvipe.hook;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pluginsfix.glowvipe.config.GlowVipeConfig;
import pluginsfix.glowvipe.domain.VaultData;
import pluginsfix.glowvipe.service.VaultService;

public final class PlaceholderHook extends PlaceholderExpansion {

    private final VaultService vaultService;
    private final GlowVipeConfig config;

    public PlaceholderHook(VaultService vaultService, GlowVipeConfig config) {
        this.vaultService = vaultService;
        this.config = config;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "glowvipe";
    }

    @Override
    public @NotNull String getAuthor() {
        return "pluginsfix";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        VaultData data = vaultService.getVaultData(player.getUniqueId());

        if (params.equalsIgnoreCase("slots")) {
            return String.valueOf(data.getPurchasedSlots());
        }

        if (params.equalsIgnoreCase("max_slots")) {
            return String.valueOf(VaultData.MAX_SLOTS);
        }

        if (params.equalsIgnoreCase("status")) {
            return data.isPacked() ? "Запаковано" : "Не запаковано";
        }

        if (params.toLowerCase().startsWith("price_")) {
            try {
                int slotNum = Integer.parseInt(params.substring(6));
                return String.valueOf(config.getPrice(slotNum));
            } catch (NumberFormatException ignored) {
            }
        }

        return null;
    }
}
