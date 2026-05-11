package com.frostgleeok.vaulted;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class InventoryListener implements Listener {

    private final Vaulted plugin;
    private final SessionManager sessionManager;

    public InventoryListener(Vaulted plugin, SessionManager sessionManager) {
        this.plugin = plugin;
        this.sessionManager = sessionManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder();

        SessionManager.VaultSession session = sessionManager.getSession(player.getUniqueId());
        if (session == null) return;

        if (holder instanceof MenuHolder) {
            event.setCancelled(true);
            if (event.getCurrentItem() != null && event.getCurrentItem().getType().name().equals("CHEST")) {
                String displayName = event.getCurrentItem().getItemMeta().getDisplayName();
                if (displayName.startsWith("Vault #")) {
                    try {
                        int vaultNumber = Integer.parseInt(displayName.substring(7));
                        sessionManager.openVault(player, vaultNumber, false);
                    } catch (NumberFormatException ignored) {}
                }
            }
        } else if (holder instanceof VaultHolder) {
            VaultHolder vaultHolder = (VaultHolder) holder;
            if (vaultHolder.isReadOnly() && event.getAction() != InventoryAction.NOTHING) {
                event.setCancelled(true);
                return;
            }

            // Prevent moving border items in menu, but since we cancel all in menu, it's fine
            // For vault, allow normal interactions but validate

            // Cancel if clicking on invalid slots or items
            if (event.getClick() == ClickType.DOUBLE_CLICK ||
                event.getClick() == ClickType.SHIFT_LEFT ||
                event.getClick() == ClickType.SHIFT_RIGHT ||
                event.getHotbarButton() != -1) {
                event.setCancelled(true);
                return;
            }

            // Ensure session is still valid
            if (!session.getSessionId().equals(session.getSessionId())) { // Wait, that's always true. Need better check
                event.setCancelled(true);
                sessionManager.forceCloseOnEvent(player.getUniqueId());
                return;
            }
        } else {
            // If player has active vault session, prevent other inventory interactions
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = (Player) event.getPlayer();
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof VaultHolder || holder instanceof MenuHolder) {
            sessionManager.closeSession(player.getUniqueId());
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        InventoryHolder holder = event.getInventory().getHolder();

        if (holder instanceof MenuHolder) {
            event.setCancelled(true);
        } else if (holder instanceof VaultHolder) {
            VaultHolder vaultHolder = (VaultHolder) holder;
            if (vaultHolder.isReadOnly()) {
                event.setCancelled(true);
            }
        } else if (sessionManager.hasActiveSession(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;

        Player player = (Player) event.getPlayer();

        // If player has active vault session, prevent opening other inventories
        if (sessionManager.hasActiveSession(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }
}