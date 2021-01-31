package jodelle.powermining.crafting;

import jodelle.powermining.PowerMining;
import jodelle.powermining.enchantment.Glow;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class CraftItemHoe {
    public JavaPlugin plugin;
    public static String loreString = "HOE!";

    ItemStack NetheriteHoe = new ItemStack(Material.NETHERITE_HOE, 1);

    ShapedRecipe NetheriteHoeRecipe;

    public CraftItemHoe(JavaPlugin plugin) {
        this.plugin = plugin;

        modifyItemMeta();
        setRecipes();
        registerRecipes();
    }
    // Get metadata for all hammer types, add lore and change the names to identify them as hammers
    public void modifyItemMeta() {
        ItemMeta NetheriteHoeMeta = NetheriteHoe.getItemMeta();

        NamespacedKey isPowerTool = new NamespacedKey(PowerMining.getInstance(), "isPowerTool");
        NetheriteHoeMeta.getPersistentDataContainer().set(isPowerTool, PersistentDataType.INTEGER, 1);


        ArrayList<String> lore = new ArrayList<String>();
        lore.add(loreString);

        NetheriteHoeMeta.setDisplayName("Netherite Hoe");
        NetheriteHoeMeta.setLore(lore);
        NetheriteHoeMeta.addEnchant(new Glow(new NamespacedKey(plugin,"NetheriteHoe")),  1, false);

        NetheriteHoe.setItemMeta(NetheriteHoeMeta);
    }

    // Creates the ShapedRecipe patterns for all hammer types
    public void setRecipes() {
        NetheriteHoeRecipe = new ShapedRecipe(new NamespacedKey(plugin, "NetheriteHoe"), NetheriteHoe);

        NetheriteHoeRecipe.shape(" m ", "mim", " m ");
        NetheriteHoeRecipe.setIngredient('m', Material.NETHERITE_INGOT);
        NetheriteHoeRecipe.setIngredient('i', Material.NETHERITE_HOE);
    }

    // Registers all created recipes into the game
    public void registerRecipes() {
        plugin.getServer().addRecipe(NetheriteHoeRecipe);
    }
}
