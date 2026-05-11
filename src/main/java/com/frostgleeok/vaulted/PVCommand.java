package com.frostgleeok.vaulted;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PVCommand implements CommandExecutor {

    private final Vaulted plugin;
    private final VaultManager vaultManager;
    private final SessionManager sessionManager;

    public PVCommand(Vaulted plugin, VaultManager vaultManager, SessionManager sessionManager) {
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

        if (args.length == 0) {
            // Open main menu
            sessionManager.openMenu(player);
        } else if (args.length == 1) {
            try {
                int vaultNumber = Integer.parseInt(args[0]);
                if (vaultNumber < 1 || vaultNumber > 27) {
                    player.sendMessage("Vault number must be between 1 and 27.");
                    return true;
                }
                if (!sessionManager.openVault(player, vaultNumber, false)) {
                    player.sendMessage("You do not have permission to access vault " + vaultNumber + ".");
                }
            } catch (NumberFormatException e) {
                player.sendMessage("Invalid vault number.");
            }
        } else {
            player.sendMessage("Usage: /pv [number]");
        }

        return true;
    }
}