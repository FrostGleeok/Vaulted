package com.frostgleeok.vaulted;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private final Vaulted plugin;
    private final VaultManager vaultManager;
    private final Map<UUID, VaultSession> activeSessions = new ConcurrentHashMap<>();

    public SessionManager(Vaulted plugin, VaultManager vaultManager) {
        this.plugin = plugin;
        this.vaultManager = vaultManager;
    }

    public boolean openVault(Player player, int vaultNumber, boolean readOnly) {
        return openVault(player, player.getUniqueId(), vaultNumber, readOnly);
    }

    public boolean openVault(Player player, UUID ownerUUID, int vaultNumber, boolean readOnly) {
        UUID uuid = player.getUniqueId();

        // Check permission
        if (!readOnly && !player.hasPermission("playervaults.vault." + vaultNumber)) {
            return false;
        }

        // Close existing session
        closeSession(uuid);

        // Create new session
        VaultSession session = new VaultSession(uuid, ownerUUID, vaultNumber, readOnly);
        activeSessions.put(uuid, session);

        // Open inventory after 1 tick
        new BukkitRunnable() {
            @Override
            public void run() {
                Inventory inventory = vaultManager.createVaultInventory(ownerUUID, vaultNumber, readOnly);
                session.setInventory(inventory);
                player.openInventory(inventory);
            }
        }.runTaskLater(plugin, 1L);

        return true;
    }

    public boolean openMenu(Player player) {
        UUID uuid = player.getUniqueId();

        // Close existing session
        closeSession(uuid);

        // Create menu session
        VaultSession session = new VaultSession(uuid, uuid, -1, false); // -1 for menu
        activeSessions.put(uuid, session);

        Inventory inventory = vaultManager.createMainMenu(player);
        session.setInventory(inventory);
        player.openInventory(inventory);

        return true;
    }

    public void closeSession(UUID uuid) {
        VaultSession session = activeSessions.remove(uuid);
        if (session != null) {
            // Save vault contents
            if (session.getVaultNumber() > 0) {
                vaultManager.updateVaultContents(session.getOwnerUUID(), session.getVaultNumber(), session.getInventory().getContents());
                vaultManager.saveVault(session.getOwnerUUID(), session.getVaultNumber());
            }

            // Close inventory
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.getOpenInventory().getTopInventory().equals(session.getInventory())) {
                player.closeInventory();
            }
        }
    }

    public void closeAllSessions() {
        for (UUID uuid : activeSessions.keySet()) {
            closeSession(uuid);
        }
    }

    public VaultSession getSession(UUID uuid) {
        return activeSessions.get(uuid);
    }

    public boolean hasActiveSession(UUID uuid) {
        return activeSessions.containsKey(uuid);
    }

    public void forceCloseOnEvent(UUID uuid) {
        closeSession(uuid);
    }

    public static class VaultSession {
        private final UUID playerUUID;
        private final UUID ownerUUID;
        private final int vaultNumber;
        private final boolean readOnly;
        private final String sessionId;
        private Inventory inventory;

        public VaultSession(UUID playerUUID, UUID ownerUUID, int vaultNumber, boolean readOnly) {
            this.playerUUID = playerUUID;
            this.ownerUUID = ownerUUID;
            this.vaultNumber = vaultNumber;
            this.readOnly = readOnly;
            this.sessionId = UUID.randomUUID().toString();
        }

        public UUID getPlayerUUID() {
            return playerUUID;
        }

        public UUID getOwnerUUID() {
            return ownerUUID;
        }

        public int getVaultNumber() {
            return vaultNumber;
        }

        public boolean isReadOnly() {
            return readOnly;
        }

        public String getSessionId() {
            return sessionId;
        }

        public Inventory getInventory() {
            return inventory;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }
    }
}