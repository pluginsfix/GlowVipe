package pluginsfix.glowvipe.hook;

import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;

import java.util.UUID;

public final class PlayerPointsHook implements CurrencyHook {

    private PlayerPointsAPI api;

    public PlayerPointsHook() {
        tryHook();
    }

    private void tryHook() {
        if (api != null) {
            return;
        }
        if (Bukkit.getPluginManager().isPluginEnabled("PlayerPoints")) {
            PlayerPoints plugin = PlayerPoints.getInstance();
            if (plugin != null) {
                this.api = plugin.getAPI();
            }
        }
    }

    @Override
    public boolean isAvailable() {
        if (api == null) {
            tryHook();
        }
        return api != null;
    }

    @Override
    public boolean has(UUID playerId, int amount) {
        if (!isAvailable()) {
            return false;
        }
        return api.look(playerId) >= amount;
    }

    @Override
    public boolean withdraw(UUID playerId, int amount) {
        if (!isAvailable()) {
            return false;
        }
        return api.take(playerId, amount);
    }
}
