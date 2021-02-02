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

public class CraftItemPlow {
    public JavaPlugin plugin;
    public static String loreString = "PLOW!";

    ItemStack WoodPlow = new ItemStack(Material.WOODEN_HOE, 1);
    ItemStack StonePlow = new ItemStack(Material.STONE_HOE, 1);
    ItemStack IronPlow = new ItemStack(Material.IRON_HOE, 1);
    ItemStack GoldPlow = new ItemStack(Material.GOLDEN_HOE, 1);
    ItemStack DiamondPlow = new ItemStack(Material.DIAMOND_HOE, 1);
    ItemStack NetheritePlow = new ItemStack(Material.NETHERITE_HOE, 1);

    ShapedRecipe WoodPlowRecipe;
    ShapedRecipe WoodPlowBirchRecipe;
    ShapedRecipe WoodPlowAcaciaRecipe;
    ShapedRecipe WoodPlowJungleRecipe;
    ShapedRecipe WoodPlowDarkOakRecipe;
    ShapedRecipe WoodPlowSpurceRecipe;

    ShapedRecipe StonePlowRecipe;
    ShapedRecipe IronPlowRecipe;
    ShapedRecipe GoldPlowRecipe;
    ShapedRecipe DiamondPlowRecipe;
    ShapedRecipe NetheritePlowRecipe;

    public CraftItemPlow(JavaPlugin plugin) {
        this.plugin = plugin;

        modifyItemMeta();
        setRecipes();
        registerRecipes();
    }
    // Get metadata for all hammer types, add lore and change the names to identify them as hammers
    public void modifyItemMeta() {
        ItemMeta WoodPlowMeta = WoodPlow.getItemMeta();
        ItemMeta StonePlowMeta = StonePlow.getItemMeta();
        ItemMeta IronPlowMeta = IronPlow.getItemMeta();
        ItemMeta GoldPlowMeta = GoldPlow.getItemMeta();
        ItemMeta DiamondPlowMeta = DiamondPlow.getItemMeta();
        ItemMeta NetheritePlowMeta = NetheritePlow.getItemMeta();

        ArrayList<String> lore = new ArrayList<String>();
        lore.add(loreString);

        WoodPlowMeta.setDisplayName("Wooden Plow");
        StonePlowMeta.setDisplayName("Stone Plow");
        IronPlowMeta.setDisplayName("Iron Plow");
        GoldPlowMeta.setDisplayName("Golden Plow");
        DiamondPlowMeta.setDisplayName("Diamond Plow");
        NetheritePlowMeta.setDisplayName("Netherite Plow");

        NamespacedKey isPowerTool = new NamespacedKey(PowerMining.getInstance(), "isPowerTool");
        WoodPlowMeta.getPersistentDataContainer().set(isPowerTool, PersistentDataType.INTEGER, 1);
        StonePlowMeta.getPersistentDataContainer().set(isPowerTool, PersistentDataType.INTEGER, 1);
        IronPlowMeta.getPersistentDataContainer().set(isPowerTool, PersistentDataType.INTEGER, 1);
        GoldPlowMeta.getPersistentDataContainer().set(isPowerTool, PersistentDataType.INTEGER, 1);
        DiamondPlowMeta.getPersistentDataContainer().set(isPowerTool, PersistentDataType.INTEGER, 1);
        NetheritePlowMeta.getPersistentDataContainer().set(isPowerTool, PersistentDataType.INTEGER, 1);

        WoodPlowMeta.setLore(lore);
        WoodPlowMeta.addEnchant(new Glow(new NamespacedKey(plugin,"WoodPlow")),  1, false);

        StonePlowMeta.setLore(lore);
        StonePlowMeta.addEnchant(new Glow(new NamespacedKey(plugin,"StonePlow")),  1, false);

        IronPlowMeta.setLore(lore);
        IronPlowMeta.addEnchant(new Glow(new NamespacedKey(plugin,"IronPlow")),  1, false);

        GoldPlowMeta.setLore(lore);
        GoldPlowMeta.addEnchant(new Glow(new NamespacedKey(plugin,"GoldPlow")),  1, false);

        DiamondPlowMeta.setLore(lore);
        DiamondPlowMeta.addEnchant(new Glow(new NamespacedKey(plugin,"DiamondPlow")),  1, false);

        NetheritePlowMeta.setLore(lore);
        NetheritePlowMeta.addEnchant(new Glow(new NamespacedKey(plugin,"NetheritePlow")),  1, false);

        WoodPlow.setItemMeta(WoodPlowMeta);
        StonePlow.setItemMeta(StonePlowMeta);
        IronPlow.setItemMeta(IronPlowMeta);
        GoldPlow.setItemMeta(GoldPlowMeta);
        DiamondPlow.setItemMeta(DiamondPlowMeta);
        NetheritePlow.setItemMeta(NetheritePlowMeta);
    }

    // Creates the ShapedRecipe patterns for all hammer types
    public void setRecipes() {
        WoodPlowRecipe = new ShapedRecipe(new NamespacedKey(plugin, "WoodPlow"),WoodPlow);
        WoodPlowBirchRecipe = new ShapedRecipe(new NamespacedKey(plugin, "WoodPlowBirchRecipe"),WoodPlow);
        WoodPlowAcaciaRecipe = new ShapedRecipe(new NamespacedKey(plugin, "WoodPlowAcaciaRecipe"),WoodPlow);
        WoodPlowJungleRecipe = new ShapedRecipe(new NamespacedKey(plugin, "WoodPlowJungleRecipe"),WoodPlow);
        WoodPlowDarkOakRecipe = new ShapedRecipe(new NamespacedKey(plugin, "WoodPlowDarkOakRecipe"),WoodPlow);
        WoodPlowSpurceRecipe = new ShapedRecipe(new NamespacedKey(plugin, "WoodPlowSpurceRecipe"),WoodPlow);
        StonePlowRecipe = new ShapedRecipe(new NamespacedKey(plugin,"StonePlow"), StonePlow);
        IronPlowRecipe = new ShapedRecipe(new NamespacedKey(plugin,"IronPlow"),IronPlow);
        GoldPlowRecipe = new ShapedRecipe(new NamespacedKey(plugin,"GoldPlow"),GoldPlow);
        DiamondPlowRecipe = new ShapedRecipe(new NamespacedKey(plugin,"DiamondPlow"),DiamondPlow);
        NetheritePlowRecipe = new ShapedRecipe(new NamespacedKey(plugin,"NetheritePlow"),NetheritePlow);

        WoodPlowRecipe.shape(" m ", "mim", " m ");
        WoodPlowRecipe.setIngredient('m', Material.OAK_LOG);
        WoodPlowRecipe.setIngredient('i', Material.WOODEN_HOE);

        WoodPlowBirchRecipe.shape(" m ", "mim", " m ");
        WoodPlowBirchRecipe.setIngredient('m', Material.BIRCH_LOG);
        WoodPlowBirchRecipe.setIngredient('i', Material.WOODEN_HOE);

        WoodPlowAcaciaRecipe.shape(" m ", "mim", " m ");
        WoodPlowAcaciaRecipe.setIngredient('m', Material.ACACIA_LOG);
        WoodPlowAcaciaRecipe.setIngredient('i', Material.WOODEN_HOE);

        WoodPlowJungleRecipe.shape(" m ", "mim", " m ");
        WoodPlowJungleRecipe.setIngredient('m', Material.JUNGLE_LOG);
        WoodPlowJungleRecipe.setIngredient('i', Material.WOODEN_HOE);

        WoodPlowDarkOakRecipe.shape(" m ", "mim", " m ");
        WoodPlowDarkOakRecipe.setIngredient('m', Material.DARK_OAK_LOG);
        WoodPlowDarkOakRecipe.setIngredient('i', Material.WOODEN_HOE);

        WoodPlowSpurceRecipe.shape(" m ", "mim", " m ");
        WoodPlowSpurceRecipe.setIngredient('m', Material.SPRUCE_LOG);
        WoodPlowSpurceRecipe.setIngredient('i', Material.WOODEN_HOE);

        StonePlowRecipe.shape(" m ", "mim", " m ");
        StonePlowRecipe.setIngredient('m', Material.STONE);
        StonePlowRecipe.setIngredient('i', Material.STONE_HOE);

        IronPlowRecipe.shape(" m ", "mim", " m ");
        IronPlowRecipe.setIngredient('m', Material.IRON_INGOT);
        IronPlowRecipe.setIngredient('i', Material.IRON_HOE);

        GoldPlowRecipe.shape(" m ", "mim", " m ");
        GoldPlowRecipe.setIngredient('m', Material.GOLD_INGOT);
        GoldPlowRecipe.setIngredient('i', Material.GOLDEN_HOE);

        DiamondPlowRecipe.shape(" m ", "mim", " m ");
        DiamondPlowRecipe.setIngredient('m', Material.DIAMOND);
        DiamondPlowRecipe.setIngredient('i', Material.DIAMOND_HOE);

        NetheritePlowRecipe.shape(" m ", "mim", " m ");
        NetheritePlowRecipe.setIngredient('m', Material.NETHERITE_INGOT);
        NetheritePlowRecipe.setIngredient('i', Material.NETHERITE_HOE);
    }

    // Registers all created recipes into the game
    public void registerRecipes() {
        plugin.getServer().addRecipe(WoodPlowRecipe);
        plugin.getServer().addRecipe(WoodPlowBirchRecipe);
        plugin.getServer().addRecipe(WoodPlowAcaciaRecipe);
        plugin.getServer().addRecipe(WoodPlowJungleRecipe);
        plugin.getServer().addRecipe(WoodPlowDarkOakRecipe);
        plugin.getServer().addRecipe(WoodPlowSpurceRecipe);
        plugin.getServer().addRecipe(StonePlowRecipe);
        plugin.getServer().addRecipe(IronPlowRecipe);
        plugin.getServer().addRecipe(GoldPlowRecipe);
        plugin.getServer().addRecipe(DiamondPlowRecipe);
        plugin.getServer().addRecipe(NetheritePlowRecipe);
    }
}
