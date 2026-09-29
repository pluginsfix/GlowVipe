package pluginsfix.glowvipe;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import pluginsfix.glowvipe.command.GlowVipeCommand;
import pluginsfix.glowvipe.config.GlowVipeConfig;
import pluginsfix.glowvipe.gui.GlowVipeMenu;
import pluginsfix.glowvipe.hook.CurrencyHook;
import pluginsfix.glowvipe.hook.PlaceholderHook;
import pluginsfix.glowvipe.hook.PlayerPointsHook;
import pluginsfix.glowvipe.hook.VaultEconomyHook;
import pluginsfix.glowvipe.listener.MenuListener;
import pluginsfix.glowvipe.listener.PlayerQuitListener;
import pluginsfix.glowvipe.service.VaultService;
import pluginsfix.glowvipe.storage.YamlVaultRepository;
import pluginsfix.glowvipe.text.Messages;

public final class GlowVipe extends JavaPlugin {

    private YamlVaultRepository repository;

    @Override
    public void onEnable() {
        GlowVipeConfig config = new GlowVipeConfig(this);
        Messages messages = new Messages(this);

        this.repository = new YamlVaultRepository(this);

        CurrencyHook currencyHook = selectCurrencyHook(config);
        VaultService vaultService = new VaultService(repository, config, messages, currencyHook);
        GlowVipeMenu menu = new GlowVipeMenu(this, config);

        MenuListener menuListener = new MenuListener(vaultService, menu, messages);
        PlayerQuitListener quitListener = new PlayerQuitListener(repository, menuListener);

        getServer().getPluginManager().registerEvents(menuListener, this);
        getServer().getPluginManager().registerEvents(quitListener, this);

        GlowVipeCommand commandExecutor = new GlowVipeCommand(config, messages, vaultService, menu);
        PluginCommand command = getCommand("glowvipe");
        if (command != null) {
            command.setExecutor(commandExecutor);
            command.setTabCompleter(commandExecutor);
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderHook(vaultService, config).register();
        }
    }

    @Override
    public void onDisable() {
        if (repository != null) {
            repository.close();
        }
    }

    private CurrencyHook selectCurrencyHook(GlowVipeConfig config) {
        if ("vault".equalsIgnoreCase(config.getEconomyType())) {
            return new VaultEconomyHook(this);
        }
        PlayerPointsHook playerPointsHook = new PlayerPointsHook();
        if (playerPointsHook.isAvailable()) {
            return playerPointsHook;
        }
        return new VaultEconomyHook(this);
    }
}
