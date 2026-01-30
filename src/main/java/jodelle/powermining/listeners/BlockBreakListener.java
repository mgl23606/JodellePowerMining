/**
 * Listener that applies PowerTool “area mining” mechanics to block breaking.
 *
 * <p>This listener reacts to {@link org.bukkit.event.block.BlockBreakEvent} and extends the normal
 * one-block break behavior into an area-of-effect (AoE) break when the player uses a valid
 * PowerTool (e.g., Hammer or Excavator). The AoE shape is determined by:</p>
 * <ul>
 *   <li>The player-facing {@link org.bukkit.block.BlockFace} tracked by the plugin’s {@code PlayerInteractListener}.</li>
 *   <li>Configured radius/depth values (defaults from {@link jodelle.powermining.lib.Reference}).</li>
 *   <li>{@link jodelle.powermining.utils.PowerUtils#getSurroundingBlocks(BlockFace, Block, int, int)}.</li>
 * </ul>
 *
 * <h2>Main responsibilities</h2>
 * <ul>
 *   <li><b>Validation:</b> Ensures the player is eligible (not sneaking, holding a PowerTool, has permission).</li>
 *   <li><b>AoE block selection:</b> Computes surrounding blocks and excludes the center block (already handled by Bukkit).</li>
 *   <li><b>Protection checks:</b> Verifies each candidate block may be modified via
 *       {@link jodelle.powermining.utils.PowerUtils#canBreak(PowerMining, Player, Block)}.</li>
 *   <li><b>Tool compatibility:</b> Determines whether the tool functions as a Hammer or Excavator for a given block type
 *       using {@link jodelle.powermining.utils.PowerUtils#validateHammer(Material, Material)} and
 *       {@link jodelle.powermining.utils.PowerUtils#validateExcavator(Material, Material)}.</li>
 *   <li><b>Durability handling:</b> Optionally reduces durability per broken block depending on
 *       {@code useDurabilityPerBlock} configuration; includes an inventory update safeguard when the tool breaks.</li>
 *   <li><b>Experience handling:</b> Computes XP to drop from configuration, fires a {@link org.bukkit.event.block.BlockExpEvent}
 *       for each broken AoE block, and spawns {@link org.bukkit.entity.ExperienceOrb} accordingly (unless Silk Touch applies).</li>
 *   <li><b>Plugin integration:</b> Notifies Jobs integration (if present) prior to breaking each block.</li>
 *   <li><b>Diagnostics:</b> Emits debug output through {@link jodelle.powermining.lib.DebuggingMessages} for troubleshooting.</li>
 * </ul>
 *
 * <h2>Threading</h2>
 * <p>Runs on the Bukkit main thread. A one-tick delayed task is scheduled in survival mode to ensure the
 * client inventory view is updated correctly if the tool breaks.</p>
 */
package jodelle.powermining.listeners;

import jodelle.powermining.PowerMining;
import jodelle.powermining.lib.DebuggingMessages;
import jodelle.powermining.lib.Reference;
import jodelle.powermining.utils.PowerUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExpEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;
import java.util.Random;

import javax.annotation.Nonnull;

/**
 * Creates a new {@code BlockBreakListener}, initializes dependencies, registers
 * events, and logs configuration.
 *
 * <p>
 * The listener registers itself with the server
 * {@link org.bukkit.plugin.PluginManager} so it can receive
 * {@link org.bukkit.event.block.BlockBreakEvent} callbacks. It also obtains the
 * plugin’s
 * {@link jodelle.powermining.lib.DebuggingMessages} instance and logs the
 * current value of
 * {@code useDurabilityPerBlock} to the console for diagnostics.
 * </p>
 */
public class BlockBreakListener implements Listener {

    /** Plugin instance (non-null). */
    private final @Nonnull PowerMining plugin;

    /** Debug message helper (non-null). */
    private final @Nonnull DebuggingMessages debuggingMessages;

    /**
     * Creates and registers the listener.
     *
     * @param plugin The {@link PowerMining} plugin instance.
     */
    public BlockBreakListener(@Nonnull PowerMining plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);

        this.debuggingMessages = Objects.requireNonNull(this.plugin.getDebuggingMessages(), "debuggingMessages");
        debuggingMessages.sendConsoleMessage(
                ChatColor.YELLOW + "Config (BlockBreakListener) useDurabilityPerBlock="
                        + this.plugin.getConfig().getBoolean("useDurabilityPerBlock"));
    }

    /**
     * Handles the center block break event and applies PowerTool AoE effects to
     * additional blocks.
     *
     * <p>
     * This handler is invoked when a player breaks a block. It performs early
     * checks (holding an item,
     * using a PowerTool, permissions, not sneaking), determines the player-facing
     * direction via the
     * plugin’s {@code PlayerInteractListener}, computes surrounding blocks
     * (radius/depth), and then
     * attempts to break each additional block that is compatible with the tool.
     * </p>
     *
     * <h4>Processing steps</h4>
     * <ol>
     * <li><b>Early exit:</b> Returns immediately if the player is not holding an
     * item or if the interaction fails
     * {@link #basicVerifications(Player, ItemStack)}.</li>
     * <li><b>Direction lookup:</b> Retrieves the player’s last known facing
     * {@link org.bukkit.block.BlockFace}. If unavailable,
     * the AoE operation is aborted to avoid incorrect block selection.</li>
     * <li><b>AoE computation:</b> Reads {@code Radius} and {@code Depth} from
     * configuration (falling back to
     * {@link jodelle.powermining.lib.Reference}), converts them into internal
     * values (non-negative, minus one),
     * and requests the block list from
     * {@link jodelle.powermining.utils.PowerUtils#getSurroundingBlocks(org.bukkit.block.BlockFace, org.bukkit.block.Block, java.lang.Integer, java.lang.Integer)}.</li>
     * <li><b>Center exclusion:</b> Removes the original (center) block from the
     * list because Bukkit already processes it
     * as part of the original {@link org.bukkit.event.block.BlockBreakEvent}.</li>
     * <li><b>Tool break safety:</b> In {@link org.bukkit.GameMode#SURVIVAL},
     * schedules a 1-tick delayed inventory update to
     * ensure proper client sync if the tool breaks and becomes
     * {@link org.bukkit.Material#AIR}.</li>
     * <li><b>Breaking loop:</b> For each surrounding block:
     * <ul>
     * <li>Calls {@link #checkAndBreakBlock(Player, ItemStack, Block)} to validate
     * and break the block.</li>
     * <li>If a block was actually broken and {@code useDurabilityPerBlock} is
     * enabled, reduces durability once per block
     * (survival mode only).</li>
     * <li>If XP is returned, fires {@link org.bukkit.event.block.BlockExpEvent} and
     * spawns an {@link org.bukkit.entity.ExperienceOrb}
     * with the event-adjusted XP value.</li>
     * </ul>
     * </li>
     * </ol>
     *
     * <p>
     * <b>Important:</b> This method does not attempt to apply additional durability
     * loss when per-block durability is disabled.
     * The center block is expected to follow vanilla durability rules, while AoE
     * blocks apply durability only in per-block mode.
     * </p>
     *
     * @param event the {@link org.bukkit.event.block.BlockBreakEvent} fired by
     *              Bukkit; ignored if cancelled due to
     *              {@code ignoreCancelled=true}
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        final @Nonnull Player player = Objects.requireNonNull(event.getPlayer(), "player");
        final @Nonnull ItemStack handItem = Objects.requireNonNull(player.getInventory().getItemInMainHand(),
                "handItem");

        // Ensure the player is holding an item
        if (handItem.getType() == Material.AIR) {
            return;
        }

        // Debug message if tool has a custom name
        if (handItem.getItemMeta() != null && handItem.getItemMeta().hasDisplayName()) {
            debuggingMessages.sendConsoleMessage(
                    ChatColor.RED + "Broke a block with item: " + handItem.getItemMeta().getDisplayName());
        }

        // Perform basic verifications (permissions, tool type, sneaking)
        if (basicVerifications(player, handItem)) {
            return;
        }

        final @Nonnull Block centerBlock = Objects.requireNonNull(event.getBlock(), "centerBlock");
        final @Nonnull String playerName = Objects.requireNonNull(player.getName(), "playerName");

        final PlayerInteractListener pil = (plugin.getPlayerInteractHandler() != null)
                ? plugin.getPlayerInteractHandler().getListener()
                : null;

        if (pil == null) {
            debuggingMessages.sendConsoleMessage(ChatColor.RED + "PlayerInteractListener is null.");
            return;
        }

        // getBlockFaceByPlayerName may return null depending on your
        // implementation/state
        final BlockFace rawFace = pil.getBlockFaceByPlayerName(playerName);
        if (rawFace == null) {
            debuggingMessages.sendConsoleMessage(ChatColor.RED + "BlockFace is null for player: " + playerName);
            return;
        }
        final @Nonnull BlockFace blockFace = Objects.requireNonNull(rawFace, "blockFace");

        final int radius = Math.max(0, plugin.getConfig().getInt("Radius", Reference.RADIUS) - 1);
        final int depth = Math.max(0, plugin.getConfig().getInt("Depth", Reference.DEPTH) - 1);

        final @Nonnull List<Block> surroundingBlocks = Objects.requireNonNull(
                PowerUtils.getSurroundingBlocks(blockFace, centerBlock, radius, depth),
                "surroundingBlocks");

        // Remove the center block because it is already handled by the original
        // BlockBreakEvent
        surroundingBlocks.removeIf(b -> b.getLocation().equals(centerBlock.getLocation()));

        if (surroundingBlocks.isEmpty()) {
            debuggingMessages.sendConsoleMessage(ChatColor.RED + "No surrounding blocks found.");
            return;
        }

        // Handle durability reduction for the main block first
        if (player.getGameMode() == GameMode.SURVIVAL) {
            // Schedule a delayed inventory update to ensure proper tool breaking
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
                    player.updateInventory();
                }
            }, 1L);
        }
        final boolean useDurabilityPerBlockNow = plugin.getConfig().getBoolean("useDurabilityPerBlock");
        for (Block block : surroundingBlocks) {
            final BreakResult result = checkAndBreakBlock(player, handItem, Objects.requireNonNull(block, "block"));

            // Track broken blocks and apply durability according to config
            if (result.broken()) {
                // Per-block durability loss only when enabled
                if (player.getGameMode() == GameMode.SURVIVAL && useDurabilityPerBlockNow) {
                    PowerUtils.reduceDurability(player, handItem);
                }
            }

            // Handle XP drops
            if (result.exp() > 0) {
                BlockExpEvent expEvent = new BlockExpEvent(block, result.exp());
                plugin.getServer().getPluginManager().callEvent(expEvent);

                ExperienceOrb orb = block.getWorld().spawn(block.getLocation(), ExperienceOrb.class);
                orb.setExperience(expEvent.getExpToDrop());
            }
        }
    }

    /**
     * Immutable result for a single AoE block-processing attempt.
     *
     * <p>
     * This record communicates two outcomes back to the caller:
     * </p>
     * <ul>
     * <li>{@code broken}: whether the block was actually broken by the plugin
     * logic.</li>
     * <li>{@code exp}: the amount of experience that should be dropped for that
     * break (before event adjustments).</li>
     * </ul>
     *
     * <p>
     * Experience may be {@code 0} for many reasons, including:
     * unsupported blocks/tools, protection restrictions, break failure, missing XP
     * configuration,
     * or Silk Touch being present on the tool.
     * </p>
     *
     * @param broken {@code true} if the block was successfully broken;
     *               {@code false} otherwise
     * @param exp    the computed XP to drop (may be {@code 0})
     */
    private record BreakResult(boolean broken, int exp) {
    }

    /**
     * Validates whether the given tool should break the given block as part of AoE
     * mining, performs the break,
     * and computes the XP drop result.
     *
     * <p>
     * This helper centralizes the AoE “can I break it?” logic for a single block.
     * It determines whether
     * the player’s tool qualifies as a Hammer or Excavator for the target block
     * type, checks protection rules,
     * optionally notifies Jobs integration, executes
     * {@link org.bukkit.block.Block#breakNaturally(ItemStack)},
     * and calculates how much XP to drop (respecting Silk Touch).
     * </p>
     *
     * <h2>Rules enforced</h2>
     * <ul>
     * <li><b>Tool presence:</b> If the tool is {@link org.bukkit.Material#AIR}, no
     * action is taken.</li>
     * <li><b>Tool/block compatibility:</b> Uses
     * {@link jodelle.powermining.utils.PowerUtils#validateHammer(Material, Material)}
     * first; if not a hammer, tries
     * {@link jodelle.powermining.utils.PowerUtils#validateExcavator(Material, Material)}.</li>
     * <li><b>Protection:</b> Aborts if
     * {@link jodelle.powermining.utils.PowerUtils#canBreak(PowerMining, Player, Block)}
     * denies the break.</li>
     * <li><b>Jobs integration:</b> If a jobs hook exists, calls
     * {@code notifyJobs(player, block)} before breaking to ensure
     * job tracking receives credit for the block.</li>
     * <li><b>XP configuration:</b> Reads XP ranges from the {@code xp-drops} config
     * section keyed by block material name.
     * If the section or block entry is missing, XP defaults to {@code 0}. If
     * {@code max < min}, XP defaults to {@code 0}.</li>
     * <li><b>Break execution:</b> Uses
     * {@link org.bukkit.block.Block#breakNaturally(ItemStack)}; if it returns
     * {@code false},
     * the operation is treated as not broken and XP is {@code 0}.</li>
     * <li><b>Silk Touch:</b> If the tool has
     * {@link org.bukkit.enchantments.Enchantment#SILK_TOUCH}, XP is forced to
     * {@code 0}
     * even when the block is broken.</li>
     * </ul>
     *
     * <h2>Random XP selection</h2>
     * <p>
     * When both {@code min} and {@code max} are valid, the XP value is selected
     * uniformly from the inclusive range
     * {@code [min, max]}.
     * </p>
     *
     * @param player   the player attempting to break the block; must not be
     *                 {@code null}
     * @param handItem the tool used to break; must not be {@code null}
     * @param block    the target block; must not be {@code null}
     * @return a {@link BreakResult} indicating whether the block was broken and the
     *         XP to drop (possibly {@code 0})
     * @throws NullPointerException if {@code player}, {@code handItem}, or
     *                              {@code block} is {@code null}
     */
    private BreakResult checkAndBreakBlock(@Nonnull Player player, @Nonnull ItemStack handItem, @Nonnull Block block) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(handItem, "handItem");
        Objects.requireNonNull(block, "block");

        if (handItem.getType() == Material.AIR) {
            return new BreakResult(false, 0);
        }

        final @Nonnull Material blockMat = Objects.requireNonNull(block.getType(), "blockMat");
        final @Nonnull Material toolMat = Objects.requireNonNull(handItem.getType(), "toolMat");

        final boolean useHammer = PowerUtils.validateHammer(toolMat, blockMat);
        final boolean useExcavator = !useHammer && PowerUtils.validateExcavator(toolMat, blockMat);

        if (!(useHammer || useExcavator)) {
            return new BreakResult(false, 0);
        }

        if (!PowerUtils.canBreak(plugin, player, block)) {
            return new BreakResult(false, 0);
        }

        // Notify Jobs Reborn BEFORE breaking the block
        if (plugin.getJobsHook() != null) {
            plugin.getJobsHook().notifyJobs(player, block);
            debuggingMessages
                    .sendConsoleMessage(ChatColor.GREEN + "✅ Jobs Reborn notified for block: " + block.getType());
        }

        // Retrieve XP drop configuration for this block
        int expToDrop = 0;
        try {
            ConfigurationSection xpDropsSection = plugin.getConfig().getConfigurationSection("xp-drops");
            if (xpDropsSection == null) {
                plugin.getLogger().warning(
                        "XP drops configuration section 'xp-drops' not found. Using default XP of 0 for " + blockMat);
            } else {
                ConfigurationSection blockSection = xpDropsSection.getConfigurationSection(blockMat.toString());
                if (blockSection == null) {
                    debuggingMessages.sendConsoleMessage(
                            "XP drop configuration for block " + blockMat + " not found. Using default XP of 0.");
                } else {
                    int minXp = blockSection.getInt("min", 0);
                    int maxXp = blockSection.getInt("max", minXp);
                    if (maxXp < minXp) {
                        plugin.getLogger().warning("Invalid XP configuration for block " + blockMat
                                + ": max (" + maxXp + ") is less than min (" + minXp + "). Using default XP of 0.");
                    } else {
                        // Calculate a random XP value within the range (inclusive)
                        expToDrop = minXp + new Random().nextInt(maxXp - minXp + 1);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe(
                    "Error while retrieving XP drop configuration for block " + blockMat + ": " + e.getMessage());
        }

        // Break the block naturally if conditions are met
        final boolean broken = block.breakNaturally(handItem);

        if (!broken) {
            return new BreakResult(false, 0);
        }

        // If Silk Touch is present, do not drop XP
        if (handItem.getItemMeta() != null && handItem.getItemMeta().hasEnchant(Enchantment.SILK_TOUCH)) {
            debuggingMessages.sendConsoleMessage(
                    "Silk Touch detected on tool; no XP will be dropped for block " + blockMat);
            return new BreakResult(true, 0);
        }

        return new BreakResult(true, expToDrop);
    }

    /**
     * Performs fast, conservative checks to decide whether PowerTool AoE mechanics
     * should be applied.
     *
     * <p>
     * This method is intentionally strict: if any requirement is not met, it
     * returns {@code true} to indicate
     * the caller should stop and allow normal (vanilla) behavior to proceed without
     * AoE processing.
     * </p>
     *
     * <h2>Checks performed</h2>
     * <ul>
     * <li><b>Tool present:</b> Returns {@code true} if the player holds
     * {@link org.bukkit.Material#AIR}.</li>
     * <li><b>Sneaking bypass:</b> Returns {@code true} if the player is sneaking,
     * allowing players to opt out of AoE behavior.</li>
     * <li><b>PowerTool detection:</b> Returns {@code true} if the item is not
     * recognized as a PowerTool via
     * {@link jodelle.powermining.utils.PowerUtils#isPowerTool(ItemStack)}.</li>
     * <li><b>Permission enforcement:</b> Returns {@code true} if
     * {@link jodelle.powermining.utils.PowerUtils#checkUsePermission(PowerMining, Player, Material)}
     * fails for this tool type.</li>
     * </ul>
     *
     * @param player   the player breaking blocks; must not be {@code null}
     * @param handItem the item in the player’s main hand; must not be {@code null}
     * @return {@code true} if AoE processing should be skipped; {@code false} if
     *         PowerTool mechanics may be applied
     * @throws NullPointerException if {@code player} or {@code handItem} is
     *                              {@code null}
     */
    private boolean basicVerifications(@Nonnull Player player, @Nonnull ItemStack handItem) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(handItem, "handItem");

        if (handItem.getType() == Material.AIR) {
            return true;
        }

        if (player.isSneaking()) {
            return true;
        }

        if (!PowerUtils.isPowerTool(handItem)) {
            return true;
        }

        final @Nonnull Material handItemType = Objects.requireNonNull(handItem.getType(), "handItemType");
        return !PowerUtils.checkUsePermission(plugin, player, handItemType);
    }
}
