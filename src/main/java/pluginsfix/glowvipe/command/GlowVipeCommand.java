package pluginsfix.glowvipe.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pluginsfix.glowvipe.config.GlowVipeConfig;
import pluginsfix.glowvipe.domain.VaultData;
import pluginsfix.glowvipe.gui.GlowVipeMenu;
import pluginsfix.glowvipe.service.VaultService;
import pluginsfix.glowvipe.text.Messages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GlowVipeCommand implements CommandExecutor, TabCompleter {

    private final GlowVipeConfig config;
    private final Messages messages;
    private final VaultService vaultService;
    private final GlowVipeMenu menu;

    public GlowVipeCommand(GlowVipeConfig config, Messages messages, VaultService vaultService, GlowVipeMenu menu) {
        this.config = config;
        this.messages = messages;
        this.vaultService = vaultService;
        this.menu = menu;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                messages.send(sender, "command-usage");
                return true;
            }

            if (!player.hasPermission("glowvipe.use")) {
                messages.send(player, "no-permission");
                return true;
            }

            VaultData data = vaultService.getVaultData(player.getUniqueId());
            menu.open(player, data);
            return true;
        }

        String sub = args[0].toLowerCase();
        if (sub.equals("open") || sub.equals("menu") || sub.equals("меню")) {
            if (!(sender instanceof Player player)) {
                messages.send(sender, "player-only");
                return true;
            }

            if (!player.hasPermission("glowvipe.use")) {
                messages.send(player, "no-permission");
                return true;
            }

            VaultData data = vaultService.getVaultData(player.getUniqueId());
            menu.open(player, data);
            return true;
        }

        if (sub.equals("reload")) {
            if (!sender.hasPermission("glowvipe.admin.reload")) {
                messages.send(sender, "no-permission");
                return true;
            }

            config.reload();
            messages.reload();
            messages.send(sender, "reloaded");
            return true;
        }

        messages.send(sender, "command-usage");
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            if (sender.hasPermission("glowvipe.use")) {
                completions.add("open");
                completions.add("menu");
            }
            if (sender.hasPermission("glowvipe.admin.reload")) {
                completions.add("reload");
            }

            String current = args[0].toLowerCase();
            return completions.stream()
                    .filter(c -> c.toLowerCase().startsWith(current))
                    .toList();
        }
        return Collections.emptyList();
    }
}
