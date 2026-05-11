package com.frostgleeok.vaulted;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ViewVaultCommand implements CommandExecutor {

    private final Vaulted plugin;
    private final VaultManager vaultManager;
    private final SessionManager sessionManager;

    public ViewVaultCommand(Vaulted plugin, VaultManager vaultManager, SessionManager sessionManager) {
        this.plugin = plugin;
        this.vaultManager = vaultManager;
        this.sessionManager = sessionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("playervaults.admin.view")) {
            player.sendMessage("You do not have permission to view other players' vaults.");
            return true;
        }

        if (args.length != 2) {
            player.sendMessage("Usage: /viewvault <player> <number>");
            return true;
        }

        String playerName = args[0];
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        if (target == null || !target.hasPlayedBefore()) {
            player.sendMessage("Player not found.");
            return true;
        }

        try {
            int vaultNumber = Integer.parseInt(args[1]);
            if (vaultNumber < 1 || vaultNumber > 27) {
                player.sendMessage("Vault number must be between 1 and 27.");
                return true;
            }

            boolean readOnly = !player.hasPermission("playervaults.admin.edit");

            // Load target's vaults if not loaded
            vaultManager.loadPlayerVaults(target.getUniqueId());

            if (!sessionManager.openVault(player, target.getUniqueId(), vaultNumber, readOnly)) {
                player.sendMessage("Failed to open vault.");
            } else {
                if (readOnly) {
                    player.sendMessage("Viewing " + playerName + "'s vault " + vaultNumber + " (read-only).");
                } else {
                    player.sendMessage("Editing " + playerName + "'s vault " + vaultNumber + ".");
                }
            }
        } catch (NumberFormatException e) {
            player.sendMessage("Invalid vault number.");
        }

        return true;
    }
}