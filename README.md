# TechnoBlocks Plugin

**Version:** 2.0  
**Developer:** TechnoAde#2952

## 📋 Overview

**TechnoBlocks** is a fully customizable **Spigot** plugin that allows players to **place blocks in your hub** without permanently altering it.  
Placed blocks will **automatically disappear after a set time**, making it perfect for hubs, lobbies, or temporary building areas!

### Main Features
- Fully configurable block selection GUI
- Automatic block removal after a configurable time
- Permission system for each selectable block
- 100% customizable through `config.yml`

---

## ⚙️ Quick Configuration

The `config.yml` lets you customize every aspect of the plugin:

- **Item Selector:**  
  Customize the item players receive to open the block selector (material, name, inventory slot).

- **GUI Blocks:**  
  Add blocks selectable through the GUI, each with its own material, name, lore, quantity, and permission node.

- **Placed Blocks:**  
  Configure where blocks appear in the player's inventory and how long they last before disappearing.

- **Messages:**  
  Personalize messages shown to players (for example, block selection confirmation or no permission alerts).

---

## 📜 Commands

| Command         | Description                   | Permission Required |
|:----------------|:-------------------------------|:--------------------|
| `/technoblocks` | Opens the block selection menu | None |

> ⚡ **Note:** The main command `/technoblocks` can be changed in the plugin's main configuration.

---

## 🔒 Permissions

Each block can have a specific permission requirement.  
If a player does not have the required permission, they won't be able to select that block.

Example:
```yaml
permission: "vip.example"
