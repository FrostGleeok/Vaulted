# Vaulted

A lightweight, secure, high-performance PlayerVaults plugin for Paper 1.21.x.

## Features

- Secure vault storage with SQLite database
- 27 numbered vaults per player
- GUI menu for easy vault selection
- Admin commands for viewing/editing other players' vaults
- Comprehensive security measures to prevent inventory exploits
- Single active session per player to prevent duplication
- Automatic saving and caching

## Commands

- `/pv` - Opens the vault selection menu
- `/pv <number>` - Opens the specified vault directly
- `/viewvault <player> <number>` - View/edit another player's vault (admin only)

## Permissions

- `playervaults.vault.<number>` - Access to specific vault (1-27)
- `playervaults.admin.view` - View other players' vaults
- `playervaults.admin.edit` - Edit other players' vaults

## Installation

1. Place `vaulted-1.0.0.jar` in your `plugins` folder
2. Restart your server
3. The plugin will create `plugins/Vaulted/vaults.db` for storage

## Security Features

- Prevents all common inventory exploits (shift-click, double-click, drag, etc.)
- Forces vault closure on player movement, damage, teleport, etc.
- Single session enforcement
- Read-only mode for admin viewing
- UUID-based storage, never usernames

## Configuration

No configuration file needed. All settings are hardcoded for security and simplicity.
