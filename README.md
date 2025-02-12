# ⚡ Jodelle Power Mining  
*A powerful tool enhancement plugin for Spigot servers!*  

![⛏️ Spigot](https://img.shields.io/badge/Platform-Spigot-blue)  

---

## 📌 Overview
**Jodelle Power Mining** is a **revived and enhanced** version of an old mining plugin originally created by **bloodyshade** (2013).

This plugin introduces **special PowerTools** that allow **efficient block breaking**, such as **3x3 mining with hammers and excavators**.  

🔹 **Mine faster.**  
🔹 **Dig smarter.**  
🔹 **Customize everything.**

💖 If you love this project, consider [donating](https://www.paypal.com/donate?hosted_button_id=QG8WUHMEEBXWW) to support further development!  

## Disclaimer
I did not originally create this plugin!  
I am just updating it because the **original creator** has abandoned it.  

I really **enjoyed** the plugin, so I decided to **update and improve it further**.

💡 **Original credits go to:**  
- **bloodyshade** – The original creator  

## Features
✅ **New Power Tools** – Includes **Hammer, Excavator, and Plow** for enhanced mining & digging.  
✅ **3x3 Mining & Digging** – Tools break multiple blocks at once (fully configurable).
✅ **Customizable Blocks** – Choose which blocks can be mined or dug using PowerTools.  
✅ **Configurable Recipes** – Fully customizable crafting recipes for PowerTools
✅ **Language support** – Full translation support
✅ **Permissions System** – Control **who** can use, craft, and enchant PowerTools.  
✅ **Multiple commands** – Check version, give PowerTools, reload config, set language, etc.
✅ **WorldGuard Support** – Respects **protected areas** (optional dependency).  
✅ **Enchantments Transfer** – Tools crafted with enchanted materials inherit enchantments.  

## Screenshots
### Hammer
![Hammer Usage](https://github.com/mgl23606/JodellePowerMining/blob/master/github/Hammer.gif?raw=true)

### Excavator
![Excavator Usage](https://github.com/mgl23606/JodellePowerMining/blob/master/github/Excavator.gif?raw=true)

### Plow
![Plow Usage](https://github.com/mgl23606/JodellePowerMining/blob/master/github/Plow.gif?raw=true)
> Thanks to Tom from [NoX](https://discord.gg/jymDumdFVU) for these Screenshots

## 📥 Installation  
1. **Download** the latest `.jar` from [Releases](https://github.com/your-repo/jodelle-power-mining/releases).  
2. **Place it** in the `plugins` folder of your **Spigot** server.  
3. **Restart** your server.  
4. **Configure** settings in the `config.yml` file if needed.  

## Configuration

- With the overhaul of the plugin to Version 1.0, it **automatically migrates outdated configs** to the latest version.
  - ⚠️ If possible use a freshly generated config file
- The **config file now includes versioning** for future updates.
- The `Deep` option has been **renamed to `Depth`** (this is migrated automatically).
- The **language system** allows full translation of all messages, item names, and lore.
- 🔧 [Config file (config.yml)](https://github.com/mgl23606/JodellePowerMining/blob/master/src/main/resources/config.yml)

## 📜 Commands
| **Command** | **Description** | **Permission** |
|------------|----------------|---------------|
| `/powermining` | Displays plugin information | `powermining.use.commands` |
| `/powermining help` | Shows the help menu | `powermining.use.commands` |
| `/powermining info` | Displays plugin info (same as `/powermining`) | `powermining.use.commands` |
| `/powermining version` | Shows the current plugin version | `powermining.version` |
| `/powermining give <tool>` | Gives a PowerTool to the player | `powermining.give` |
| `/powermining admin` | Shows the admin help menu | `powermining.admin` |
| `/powermining admin reload` | Reloads the plugin configuration and language files | `powermining.admin.reload` |
| `/powermining admin language <language>` | Changes the language file immediately | `powermining.admin.language` |
| `/powermining admin debug` | Toggles debug mode (Logs extra info, may spam console) | `powermining.admin.debug` |

---

## 🔑 Permissions
### General Permissions
| **Permission** | **Description** | **Default** |
|--------------|----------------|------------|
| `powermining.all` | Grants all permissions | `op` |
| `powermining.highdurability` | PowerTools use only 1 durability per use | `true` |
| `powermining.give` | Allows receiving a PowerTool via `/powermining give <tool>` | `op` |

---

### Crafting Permissions
| **Permission** | **Description** | **Default** |
|--------------|----------------|------------|
| `powermining.craft.all` | Allows crafting all PowerTools | `op` |
| `powermining.craft.hammer.all` | Allows crafting all Hammers | `op` |
| `powermining.craft.excavator.all` | Allows crafting all Excavators | `op` |
| `powermining.craft.plow.all` | Allows crafting all Plows | `op` |

#### Hammer Crafting
| **Permission** | **Description** | **Default** |
|--------------|----------------|------------|
| `powermining.craft.hammer.wooden` | Allows crafting the Wooden Hammer | `false` |
| `powermining.craft.hammer.stone` | Allows crafting the Stone Hammer | `false` |
| `powermining.craft.hammer.iron` | Allows crafting the Iron Hammer | `false` |
| `powermining.craft.hammer.golden` | Allows crafting the Golden Hammer | `false` |
| `powermining.craft.hammer.diamond` | Allows crafting the Diamond Hammer | `false` |
| `powermining.craft.hammer.netherite` | Allows crafting the Netherite Hammer | `false` |

#### Excavator Crafting
| **Permission** | **Description** | **Default** |
|--------------|----------------|------------|
| `powermining.craft.excavator.wooden` | Allows crafting the Wooden Excavator | `false` |
| `powermining.craft.excavator.stone` | Allows crafting the Stone Excavator | `false` |
| `powermining.craft.excavator.iron` | Allows crafting the Iron Excavator | `false` |
| `powermining.craft.excavator.golden` | Allows crafting the Golden Excavator | `false` |
| `powermining.craft.excavator.diamond` | Allows crafting the Diamond Excavator | `false` |
| `powermining.craft.excavator.netherite` | Allows crafting the Netherite Excavator | `false` |

#### Plow Crafting
| **Permission** | **Description** | **Default** |
|--------------|----------------|------------|
| `powermining.craft.plow.wooden` | Allows crafting the Wooden Plow | `false` |
| `powermining.craft.plow.stone` | Allows crafting the Stone Plow | `false` |
| `powermining.craft.plow.iron` | Allows crafting the Iron Plow | `false` |
| `powermining.craft.plow.golden` | Allows crafting the Golden Plow | `false` |
| `powermining.craft.plow.diamond` | Allows crafting the Diamond Plow | `false` |
| `powermining.craft.plow.netherite` | Allows crafting the Netherite Plow | `false` |

---

### Usage Permissions
| **Permission** | **Description** | **Default** |
|--------------|----------------|------------|
| `powermining.use.all` | Allows using all PowerTools | `op` |
| `powermining.use.commands` | Allows using relevant commands | `op` |
| `powermining.use.hammer.all` | Allows using all Hammers | `op` |
| `powermining.use.excavator.all` | Allows using all Excavators | `op` |
| `powermining.use.plow.all` | Allows using all Plows | `op` |

#### Hammer Usage
| **Permission** | **Description** | **Default** |
|--------------|----------------|------------|
| `powermining.use.hammer.wooden` | Allows using the Wooden Hammer | `false` |
| `powermining.use.hammer.stone` | Allows using the Stone Hammer | `false` |
| `powermining.use.hammer.iron` | Allows using the Iron Hammer | `false` |
| `powermining.use.hammer.golden` | Allows using the Golden Hammer | `false` |
| `powermining.use.hammer.diamond` | Allows using the Diamond Hammer | `false` |
| `powermining.use.hammer.netherite` | Allows using the Netherite Hammer | `false` |

#### Excavator Usage
| **Permission** | **Description** | **Default** |
|--------------|----------------|------------|
| `powermining.use.excavator.wooden` | Allows using the Wooden Excavator | `false` |
| `powermining.use.excavator.stone` | Allows using the Stone Excavator | `false` |
| `powermining.use.excavator.iron` | Allows using the Iron Excavator | `false` |
| `powermining.use.excavator.golden` | Allows using the Golden Excavator | `false` |
| `powermining.use.excavator.diamond` | Allows using the Diamond Excavator | `false` |
| `powermining.use.excavator.netherite` | Allows using the Netherite Excavator | `false` |

#### Plow Usage
| **Permission** | **Description** | **Default** |
|--------------|----------------|------------|
| `powermining.use.plow.wooden` | Allows using the Wooden Plow | `false` |
| `powermining.use.plow.stone` | Allows using the Stone Plow | `false` |
| `powermining.use.plow.iron` | Allows using the Iron Plow | `false` |
| `powermining.use.plow.golden` | Allows using the Golden Plow | `false` |
| `powermining.use.plow.diamond` | Allows using the Diamond Plow | `false` |
| `powermining.use.plow.netherite` | Allows using the Netherite Plow | `false` |

---

### Enchantment Permissions
| **Permission** | **Description** | **Default** |
|--------------|----------------|------------|
| `powermining.enchant.all` | Allows enchanting all PowerTools | `op` |
| `powermining.enchant.hammer.all` | Allows enchanting all Hammers | `op` |
| `powermining.enchant.excavator.all` | Allows enchanting all Excavators | `op` |
| `powermining.enchant.plow.all` | Allows enchanting all Plows | `op` |

---

## **📌 Change Log**
### 🆕 **Latest Updates - January 29, 2025**
- **🔄 Updated plugin to fully work in Minecraft 1.21.4** - Earlier versions are not supported (should work, but untested).
- **🔤 Added full Language system** – Everything is translatable (messages, items, commands).
- **🔨 Added Anvil support** Let's you repair and fix your PowerTool without loosing your precious ressources.
- **🛠️ Added admin commands** – `reload`, `language`, `debug`, etc.
- **🪨 Added missing blocks up to Minecraft 1.21.4**.
- **📁 Introduced Config Versioning** – For future automatic updates.
- **🔄 Automatic Config Migration** - Works, but if possible try to use the new config file
  - **Added:** `language` (default: `en_US`).
  - **Added:** `debug` (default: `false`).
  - **Renamed** `Deep` → `Depth` (value is kept the same).
- **🐞 Fixed a bug where XP orbs did not drop when using a PowerTool.**
- **🧹 Removed outdated dependencies**.

### **📌 Previous Updates**
#### 📅 **June 14, 2021**
- Fixed **Plow permissions**.
- Verified that all permissions work as expected.

#### 📅 **June 8, 2021**
- **Added WorldGuard support** (Optional dependency).
- Now **checks each block before breaking** to prevent griefing.
- Updated dependencies in `pom.xml`.

#### 📅 **June 6, 2021**
- **Implemented item durability logic** – PowerTools now break correctly when they reach zero durability.
- **Added breaking sound effects** when a tool breaks.

#### 📅 **June 3, 2021**
- **Added new commands:** `/jpm give`, `/jpm version`
- **Enchantment Transfer** – Enchantments from crafting materials now **transfer to the PowerTool**.
- **Max stack size validation for crafting** – Prevents unrealistic recipes (e.g., stacking multiple pickaxes in one slot).
- **Code optimizations** for better performance.

#### 📅 **May 26, 2021**
- **Refactored permission handling**
  - Uses **HashMaps** for permission storage instead of hardcoding them in each method.
  - Improves performance and maintainability.

#### 📅 **May 25, 2021**
- **Fixed durability loss bug** – PowerTools now lose durability correctly.
- **Implemented Unbreaking enchantment logic**.
- **Improved BlockBreakListener**:
  - `breakNaturally()` now properly drops items.
  - **Fortune enchantment now works correctly** with PowerTools.
  - **Plow logic cleaned up** for better performance.

---

## **🌎 Useful Links**
- 🔗 [BStats Page](https://bstats.org/plugin/bukkit/JodellePowerMining/24587)
- 🔗 [Spigot](https://img.shields.io/badge/Platform-Spigot-blue)  
---

## **🚀 Future Plans**
- Open to suggestions!  
- If you have feature requests, feel free to **open an issue**.

---
## **📢 Support & Contributions**
If you enjoy this plugin, please consider **contributing or donating** to help keep development active!  

🔗 [PayPal Donation](https://www.paypal.com/donate?hosted_button_id=QG8WUHMEEBXWW)

Thank you for using **Jodelle Power Mining**! 🚀  