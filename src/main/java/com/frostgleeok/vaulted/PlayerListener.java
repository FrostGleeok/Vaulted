package com.frostgleeok.vaulted;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

public class PlayerListener implements Listener {

    private final Vaulted plugin;
    private final SessionManager sessionManager;

    public PlayerListener(Vaulted plugin, SessionManager sessionManager) {
        this.plugin = plugin;
        this.sessionManager = sessionManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (sessionManager.hasActiveSession(player.getUniqueId())) {
            // Check if actually moved
            if (event.getFrom().getX() != event.getTo().getX() ||
                event.getFrom().getY() != event.getTo().getY() ||
                event.getFrom().getZ() != event.getTo().getZ() ||
                event.getFrom().getYaw() != event.getTo().getYaw() ||
                event.getFrom().getPitch() != event.getTo().getPitch()) {
                sessionManager.forceCloseOnEvent(player.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        if (sessionManager.hasActiveSession(player.getUniqueId())) {
            sessionManager.forceCloseOnEvent(player.getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (sessionManager.hasActiveSession(player.getUniqueId())) {
                sessionManager.forceCloseOnEvent(player.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (sessionManager.hasActiveSession(player.getUniqueId())) {
            sessionManager.forceCloseOnEvent(player.getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (sessionManager.hasActiveSession(player.getUniqueId())) {
            sessionManager.forceCloseOnEvent(player.getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        if (sessionManager.hasActiveSession(player.getUniqueId())) {
            sessionManager.forceCloseOnEvent(player.getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        sessionManager.closeSession(player.getUniqueId());
        plugin.getVaultManager().unloadPlayer(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        sessionManager.closeSession(player.getUniqueId());
        plugin.getVaultManager().unloadPlayer(player.getUniqueId());
    }

    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (event.getEntered() instanceof Player) {
            Player player = (Player) event.getEntered();
            if (sessionManager.hasActiveSession(player.getUniqueId())) {
                sessionManager.forceCloseOnEvent(player.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onVehicleExit(VehicleExitEvent event) {
        if (event.getExited() instanceof Player) {
            Player player = (Player) event.getExited();
            if (sessionManager.hasActiveSession(player.getUniqueId())) {
                sessionManager.forceCloseOnEvent(player.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (sessionManager.hasActiveSession(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (sessionManager.hasActiveSession(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (sessionManager.hasActiveSession(player.getUniqueId())) {
            String command = event.getMessage().toLowerCase();
            if (!command.startsWith("/pv") && !command.startsWith("/vault")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        if (sessionManager.hasActiveSession(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (sessionManager.hasActiveSession(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerArmorStandManipulate(PlayerArmorStandManipulateEvent event) {
        Player player = event.getPlayer();
        if (sessionManager.hasActiveSession(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }
}