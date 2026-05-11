package com.frostgleeok.vaulted;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseManager {

    private final Vaulted plugin;
    private Connection connection;

    public DatabaseManager(Vaulted plugin) {
        this.plugin = plugin;
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder() + "/vaults.db");

            try (Statement stmt = connection.createStatement()) {
                stmt.execute("CREATE TABLE IF NOT EXISTS vaults (" +
                        "uuid TEXT NOT NULL, " +
                        "vault_number INTEGER NOT NULL, " +
                        "contents BLOB, " +
                        "PRIMARY KEY(uuid, vault_number))");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
        }
    }

    public CompletableFuture<Map<Integer, ItemStack[]>> loadVaults(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            Map<Integer, ItemStack[]> vaults = new ConcurrentHashMap<>();
            String sql = "SELECT vault_number, contents FROM vaults WHERE uuid = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    int number = rs.getInt("vault_number");
                    byte[] data = rs.getBytes("contents");
                    if (data != null) {
                        vaults.put(number, deserializeContents(data));
                    } else {
                        vaults.put(number, new ItemStack[54]); // Default size
                    }
                }
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to load vaults for " + uuid + ": " + e.getMessage());
            }
            return vaults;
        });
    }

    public void saveVault(UUID uuid, int vaultNumber, ItemStack[] contents) {
        CompletableFuture.runAsync(() -> {
            String sql = "INSERT OR REPLACE INTO vaults (uuid, vault_number, contents) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, uuid.toString());
                stmt.setInt(2, vaultNumber);
                stmt.setBytes(3, serializeContents(contents));
                stmt.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to save vault " + vaultNumber + " for " + uuid + ": " + e.getMessage());
            }
        });
    }

    private byte[] serializeContents(ItemStack[] contents) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             BukkitObjectOutputStream boos = new BukkitObjectOutputStream(baos)) {
            boos.writeObject(contents);
            return baos.toByteArray();
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to serialize vault contents: " + e.getMessage());
            return null;
        }
    }

    private ItemStack[] deserializeContents(byte[] data) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             BukkitObjectInputStream bois = new BukkitObjectInputStream(bais)) {
            return (ItemStack[]) bois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            plugin.getLogger().severe("Failed to deserialize vault contents: " + e.getMessage());
            return new ItemStack[54];
        }
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to close database: " + e.getMessage());
            }
        }
    }
}