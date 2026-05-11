package com.frostgleeok.vaulted;

import org.bukkit.plugin.java.JavaPlugin;

public class Vaulted extends JavaPlugin {

    private DatabaseManager databaseManager;
    private VaultManager vaultManager;
    private SessionManager sessionManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Initialize managers
        databaseManager = new DatabaseManager(this);
        vaultManager = new VaultManager(this, databaseManager);
        sessionManager = new SessionManager(this, vaultManager);

        // Register commands
        getCommand("pv").setExecutor(new PVCommand(this, vaultManager, sessionManager));
        getCommand("viewvault").setExecutor(new ViewVaultCommand(this, vaultManager, sessionManager));

        // Register listeners
        getServer().getPluginManager().registerEvents(new InventoryListener(this, sessionManager), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this, sessionManager), this);

        // Start autosave task
        getServer().getScheduler().runTaskTimerAsynchronously(this, vaultManager::saveAllActiveVaults, 1200L, 1200L); // Every minute

        getLogger().info("Vaulted enabled!");
    }

    @Override
    public void onDisable() {
        // Save all vaults
        if (vaultManager != null) {
            vaultManager.saveAllActiveVaults();
        }
        // Close all sessions
        if (sessionManager != null) {
            sessionManager.closeAllSessions();
        }
        // Close database
        if (databaseManager != null) {
            databaseManager.close();
        }

        getLogger().info("Vaulted disabled!");
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public VaultManager getVaultManager() {
        return vaultManager;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }
}