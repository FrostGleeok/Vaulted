package com.frostgleeok.vaulted;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public class VaultHolder implements InventoryHolder {

    private final UUID ownerUUID;
    private final int vaultNumber;
    private final boolean readOnly;

    public VaultHolder(UUID ownerUUID, int vaultNumber, boolean readOnly) {
        this.ownerUUID = ownerUUID;
        this.vaultNumber = vaultNumber;
        this.readOnly = readOnly;
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

    @Override
    public Inventory getInventory() {
        return null; // Not used
    }
}

class MenuHolder implements InventoryHolder {

    @Override
    public Inventory getInventory() {
        return null; // Not used
    }
}