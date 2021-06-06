# Jodelle Power Mining
---
## Overview
Attention! I did not create this plugin! I am just updating it because the creator droped it in 2013 and I like this plugin very much. All the credits go to bloodyshade.

JodellePowerMining is a plugin for spigot that adds a new set of tools to the game. These tools have a different behavior from the original version. An example is instead of a pickaxe mining just one block, it can mine 3x3.

## Features
- Adds 2 different tools to the game, Hammer and Shovel.
- Mining one block makes the other blocks around to be broken, a 3x3 hole.
- All mineble/diggable blocks can be configured in the confile file.
- Configurable recipes for the powertools. (Finished, maybe...)
- Commands to check the version and give a power tool

## Todo List
- Test the recipes...
- Implement the durability per block thing.
- Improve, fix and test permissions.


## Change log
##### 6 june 2021
    - Implemented the logic that removes the item from the player inventory as soons as the 
      durability reaches a value below zero. Also plays a breaking sound.
##### 3 june 2021
    - Added the command "jpm give" and " jpm version"
    - Enchantments of items from the recipe on the crafting table are now passed onto
      the crafted PowerTool.
    - Max stack size is now verified when loading the custom recipes. This prevents, for example, more
      setting more than one pickaxe per slot, which is impossible to make in survival.
    - Little optimizations on the code.

##### 26 may 2021
    - Changed the way permissions are checked. Instead of having all the permissions hardcoded on each method 
      they are now created while the plugin is loaded. HashMaps are used to store each type of permission which 
      are later used to check if the player has the specific permissions.
##### 25 may 2021
    - When using a powertool the durability wasn't updated. That's due to a deprecated method being used.
      It has been updated to use the new way. Basically now we need to get the item meta and change the durability
      there. If we use the old method the changes are not applied.
      Created the method, on the PowerUtils class, reduceDurability(ItemStack) to be used by all powertools.
      Also implemented the unbreaking enchantment logic.    

    - Reviewed the BlockBreakListener code and removed useless code. 
      Before the method breakNaturally() didn't drop anything, so the dropping logic needed to be implemented 
      manually. This includes the enchants like the various fortune levels.
      Also removed some code referring to the Plow present on the this class.
    
    
    
    
