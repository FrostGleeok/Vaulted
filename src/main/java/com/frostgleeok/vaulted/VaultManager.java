package com.frostgleeok.vaulted;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VaultManager {

    private final Vaulted plugin;
    private final DatabaseManager databaseManager;
    private final Map<UUID, Map<Integer, ItemStack[]>> vaultCache = new ConcurrentHashMap<>();
    private final Map<UUID, Map<Integer, Long>> lastAccess = new ConcurrentHashMap<>();

    public VaultManager(Vaulted plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    public void loadPlayerVaults(UUID uuid) {
        if (!vaultCache.containsKey(uuid)) {
            databaseManager.loadVaults(uuid).thenAccept(vaults -> {
                vaultCache.put(uuid, vaults);
                lastAccess.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());
            });
        }
    }

    public ItemStack[] getVaultContents(UUID uuid, int vaultNumber) {
        loadPlayerVaults(uuid);
        Map<Integer, ItemStack[]> playerVaults = vaultCache.get(uuid);
        if (playerVaults == null) return new ItemStack[54];
        return playerVaults.computeIfAbsent(vaultNumber, k -> new ItemStack[54]);
    }

    public void saveVault(UUID uuid, int vaultNumber) {
        Map<Integer, ItemStack[]> playerVaults = vaultCache.get(uuid);
        if (playerVaults != null) {
            ItemStack[] contents = playerVaults.get(vaultNumber);
            if (contents != null) {
                databaseManager.saveVault(uuid, vaultNumber, contents);
            }
        }
    }

    public void saveAllActiveVaults() {
        for (Map.Entry<UUID, Map<Integer, ItemStack[]>> entry : vaultCache.entrySet()) {
            UUID uuid = entry.getKey();
            for (Map.Entry<Integer, ItemStack[]> vaultEntry : entry.getValue().entrySet()) {
                databaseManager.saveVault(uuid, vaultEntry.getKey(), vaultEntry.getValue());
            }
        }
    }

    public void updateVaultContents(UUID uuid, int vaultNumber, ItemStack[] contents) {
        vaultCache.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).put(vaultNumber, contents);
        lastAccess.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).put(vaultNumber, System.currentTimeMillis());
    }

    public Inventory createVaultInventory(UUID uuid, int vaultNumber, boolean readOnly) {
        ItemStack[] contents = getVaultContents(uuid, vaultNumber);
        Inventory inventory = Bukkit.createInventory(new VaultHolder(uuid, vaultNumber, readOnly), 54, "Vault #" + vaultNumber);
        inventory.setContents(contents);
        return inventory;
    }

    public Inventory createMainMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(new MenuHolder(), 45, "Player Vaults");

        // Border with light gray stained glass panes
        ItemStack border = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = border.getItemMeta();
        meta.setDisplayName(" ");
        meta.setHideTooltip(true);
        border.setItemMeta(meta);

        for (int i = 0; i < 45; i++) {
            if (i < 9 || i >= 36 || i % 9 == 0 || i % 9 == 8) {
                inventory.setItem(i, border);
            }
        }

        // Vault buttons
        for (int vault = 1; vault <= 27; vault++) {
            int slot = 9 + (vault - 1) / 7 * 9 + (vault - 1) % 7 + 1;
            if (slot < 36) {
                ItemStack button = new ItemStack(Material.CHEST);
                ItemMeta buttonMeta = button.getItemMeta();
                buttonMeta.setDisplayName("Vault #" + vault);
                button.setItemMeta(buttonMeta);
                inventory.setItem(slot, button);
            }
        }

        return inventory;
    }

    public void unloadPlayer(UUID uuid) {
        Map<Integer, ItemStack[]> vaults = vaultCache.remove(uuid);
        if (vaults != null) {
            for (Map.Entry<Integer, ItemStack[]> entry : vaults.entrySet()) {
                databaseManager.saveVault(uuid, entry.getKey(), entry.getValue());
            }
        }
        lastAccess.remove(uuid);
    }
}