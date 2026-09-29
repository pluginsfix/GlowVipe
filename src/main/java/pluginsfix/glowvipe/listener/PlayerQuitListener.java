package pluginsfix.glowvipe.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import pluginsfix.glowvipe.storage.YamlVaultRepository;

public final class PlayerQuitListener implements Listener {

    private final YamlVaultRepository repository;
    private final MenuListener menuListener;

    public PlayerQuitListener(YamlVaultRepository repository, MenuListener menuListener) {
        this.repository = repository;
        this.menuListener = menuListener;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        menuListener.removePlayer(player.getUniqueId());
        repository.unload(player.getUniqueId());
    }
}
