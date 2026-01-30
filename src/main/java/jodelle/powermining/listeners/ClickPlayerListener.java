/**
 * Listener that applies PowerTool “right-click” mechanics to blocks.
 *
 * <p>This listener intercepts {@link org.bukkit.event.player.PlayerInteractEvent} to provide
 * custom behavior for PowerTools (e.g., hoes/shovels configured as PowerTools). When a player
 * right-clicks a valid block with a valid PowerTool, the listener can transform a set of
 * surrounding blocks (defined by {@link jodelle.powermining.lib.Reference#RADIUS} and the
 * player-facing {@link org.bukkit.block.BlockFace}) into either {@link org.bukkit.Material#FARMLAND}
 * (tilling) or {@link org.bukkit.Material#DIRT_PATH} (path creation).</p>
 *
 * <h3>Key responsibilities</h3>
 * <ul>
 *   <li><b>Validation:</b> Ensures the interaction is eligible (right-clicking a block, not sneaking,
 *       holding a non-air item, clicked block present, block is tillable, item is a PowerTool, and
 *       the player has permission to use it).</li>
 *   <li><b>Permission-aware block changes:</b> For each candidate surrounding block, verifies the
 *       player may modify/break it (e.g., region protection integrations) before changing the block.</li>
 *   <li><b>Controlled durability handling:</b> Supports two durability modes:
 *     <ul>
 *       <li><i>Per-block durability</i> (config {@code useDurabilityPerBlock=true}): durability is reduced
 *           once for each block that actually changed.</li>
 *       <li><i>Per-use durability</i> (config {@code useDurabilityPerBlock=false}): vanilla interaction is
 *           cancelled and a single durability loss is applied if at least one block changed.</li>
 *     </ul>
 *   </li>
 *   <li><b>Diagnostics:</b> Emits debug output through {@link jodelle.powermining.lib.DebuggingMessages}
 *       to help track configuration state and the number of blocks changed.</li>
 * </ul>
 *
 * <h3>Threading</h3>
 * <p>All actions are executed on the Bukkit main thread as part of the event callback.</p>
 *
 * <h3>Notes</h3>
 * <ul>
 *   <li>This listener registers itself during construction.</li>
 *   <li>Actual surrounding-block selection and tool validation rules are delegated to {@link jodelle.powermining.utils.PowerUtils}.</li>
 * </ul>
 */
package jodelle.powermining.listeners;

import jodelle.powermining.PowerMining;
import jodelle.powermining.lib.DebuggingMessages;
import jodelle.powermining.lib.Reference;
import jodelle.powermining.utils.PowerUtils;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Creates a new listener instance, initializes dependencies, registers events,
 * and logs configuration.
 *
 * <p>
 * The listener is immediately registered with the server's
 * {@link org.bukkit.plugin.PluginManager},
 * enabling it to receive {@link org.bukkit.event.player.PlayerInteractEvent}
 * callbacks.
 * </p>
 *
 * <p>
 * The constructor also acquires the
 * {@link jodelle.powermining.lib.DebuggingMessages} instance from
 * the plugin and logs the current value of the {@code useDurabilityPerBlock}
 * configuration option for
 * troubleshooting.
 * </p>
 */
public class ClickPlayerListener implements Listener {
    private final @Nonnull PowerMining plugin;
    private final @Nonnull DebuggingMessages debuggingMessages;

    /**
     * Constructs a {@code ClickPlayerListener} and registers it as an event
     * listener.
     * 
     * @param plugin The instance of {@link PowerMining} used for event
     *               registration.
     */
    public ClickPlayerListener(@Nonnull final PowerMining plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.debuggingMessages = Objects.requireNonNull(this.plugin.getDebuggingMessages(), "debuggingMessages");
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
        debuggingMessages.sendConsoleMessage(
                ChatColor.YELLOW + "Config (ClickPlayerListener) useDurabilityPerBlock="
                        + this.plugin.getConfig().getBoolean("useDurabilityPerBlock"));
    }

    /**
     * Handles PowerTool usage when a player interacts with a block.
     *
     * <p>
     * This event handler reacts to
     * {@link org.bukkit.event.player.PlayerInteractEvent} with
     * {@link org.bukkit.event.block.Action} indicating a block interaction. If the
     * interaction passes
     * {@link #basicVerifications(Action, Player, ItemStack, Material, Block)}, the
     * handler gathers the
     * player's facing direction from the plugin-managed
     * {@code PlayerInteractListener} and computes the
     * set of target blocks using
     * {@link jodelle.powermining.utils.PowerUtils#getSurroundingBlocksFarm(org.bukkit.block.BlockFace, org.bukkit.block.Block, java.lang.Integer)}.
     * </p>
     *
     * <h4>Behavior</h4>
     * <ul>
     * <li>Skips processing for non-applicable actions (left click, air click),
     * sneaking players, empty hands,
     * missing clicked block, non-tillable blocks, non-PowerTools, or missing use
     * permissions.</li>
     * <li>Optionally cancels the event when {@code useDurabilityPerBlock} is
     * disabled to prevent vanilla tool behavior
     * and keep durability changes fully controlled by the plugin.</li>
     * <li>Iterates all candidate surrounding blocks:
     * <ul>
     * <li>Skips blocks the player cannot modify based on
     * {@link jodelle.powermining.utils.PowerUtils#canBreak(PowerMining, Player, Block)}.</li>
     * <li>If
     * {@link jodelle.powermining.utils.PowerUtils#validatePlow(Material, Material)}
     * returns {@code true},
     * attempts to convert the block to {@link org.bukkit.Material#FARMLAND}.</li>
     * <li>If
     * {@link jodelle.powermining.utils.PowerUtils#validatePath(Material, Material)}
     * returns {@code true},
     * attempts to convert the block to {@link org.bukkit.Material#DIRT_PATH}.</li>
     * </ul>
     * </li>
     * <li>Counts how many blocks actually changed material and logs the result for
     * debugging.</li>
     * <li>Durability handling:
     * <ul>
     * <li><b>Per-block mode:</b> durability is reduced inside
     * {@link #usePowerTool(Player, ItemStack, Block, Material, boolean)}
     * for each block that actually changes (only in
     * {@link org.bukkit.GameMode#SURVIVAL}).</li>
     * <li><b>Per-use mode:</b> if at least one block changed, durability is reduced
     * exactly once at the end (only in
     * {@link org.bukkit.GameMode#SURVIVAL}).</li>
     * </ul>
     * </li>
     * </ul>
     *
     * <p>
     * This method does not modify durability in creative mode.
     * </p>
     *
     * @param event the interaction event fired by Bukkit; provided by the event
     *              system
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerUse(final PlayerInteractEvent event) {
        final @Nonnull Player player = Objects.requireNonNull(event.getPlayer(), "player");
        final @Nonnull ItemStack handItem = Objects.requireNonNull(player.getInventory().getItemInMainHand(),
                "handItem");
        final @Nonnull Material handItemType = Objects.requireNonNull(handItem.getType(), "handItemType");
        final @Nullable Block block = event.getClickedBlock();
        final @Nonnull Action action = Objects.requireNonNull(event.getAction(), "action");

        if (basicVerifications(action, player, handItem, handItemType, block)) {
            return;
        }
        // Prevent vanilla hoe behavior when durability-per-use is disabled.
        // We fully control durability handling in this mode.
        final boolean useDurabilityPerBlockNow = plugin.getConfig().getBoolean("useDurabilityPerBlock");
        if (!useDurabilityPerBlockNow) {
            event.setCancelled(true);
        }

        final @Nonnull String playerName = Objects.requireNonNull(player.getName(), "playerName");

        final @Nonnull PlayerInteractListener pil = Objects.requireNonNull(
                plugin.getPlayerInteractHandler().getListener(),
                "playerInteractListener");
        final @Nonnull BlockFace blockFace = Objects.requireNonNull(pil.getBlockFaceByPlayerName(playerName),
                "blockFace");

        /*
         * At this point the IDE shows a warning about the possibility of the argument
         * block being null. This warning can be ignored because in the method
         * basicVerifications()
         * we already make sure that the block is not null, and if it is indeed null
         * this method
         * never reached this point of the code.
         */
        int changedBlocks = 0;
        for (final Block e : PowerUtils.getSurroundingBlocksFarm(blockFace, block, Reference.RADIUS)) {
            final @Nonnull Material blockMat = Objects.requireNonNull(e.getType(), "blockMat");

            // Check if player has permission to break the block
            if (!PowerUtils.canBreak(plugin, player, e)) {
                continue;
            }

            if (PowerUtils.validatePlow(handItemType, blockMat)) {
                debuggingMessages.sendConsoleMessage(ChatColor.RED + "Tilling: " + e.getType());
                if (usePowerTool(player, handItem, e, Material.FARMLAND, useDurabilityPerBlockNow)) {
                    changedBlocks++;
                }

                continue;
            }

            if (PowerUtils.validatePath(handItemType, blockMat)) {
                if (usePowerTool(player, handItem, e, Material.DIRT_PATH, useDurabilityPerBlockNow)) {
                    changedBlocks++;
                }
            }
        }
        debuggingMessages.sendConsoleMessage(
                ChatColor.YELLOW + "changedBlocks=" + changedBlocks
                        + " useDurabilityPerBlockNow=" + useDurabilityPerBlockNow);

        // Apply a single durability loss when per-block durability is disabled.
        // For hoe interactions, vanilla durability loss is not guaranteed when we
        // modify blocks manually.
        if (!useDurabilityPerBlockNow && changedBlocks > 0 && player.getGameMode() == GameMode.SURVIVAL) {
            PowerUtils.reduceDurability(player, handItem);
        }
    }

    /**
     * Applies a single block transformation for a PowerTool action and handles
     * durability if configured.
     *
     * <p>
     * This helper performs the actual block replacement and durability reduction
     * logic in a safe,
     * idempotent way: if the block already is the requested {@code material}, the
     * method performs no
     * world change and returns {@code false} to indicate that no effective change
     * occurred.
     * </p>
     *
     * <h3>Durability rules</h3>
     * <ul>
     * <li>Durability is only reduced when {@code useDurabilityPerBlockNow} is
     * {@code true} <em>and</em>
     * the player is in {@link org.bukkit.GameMode#SURVIVAL}.</li>
     * <li>When {@code useDurabilityPerBlockNow} is {@code false}, durability is
     * intentionally not reduced here;
     * it is handled once per use by the caller if at least one block changed.</li>
     * </ul>
     *
     * @param player                   the player performing the action; must not be
     *                                 {@code null}
     * @param handItem                 the item used as the tool; must not be
     *                                 {@code null}
     * @param block                    the target block to be transformed; must not
     *                                 be {@code null}
     * @param material                 the resulting material to apply to the block;
     *                                 must not be {@code null}
     * @param useDurabilityPerBlockNow whether durability should be reduced for each
     *                                 successful block change
     * @return {@code true} if the block was changed to {@code material};
     *         {@code false} if no change was necessary
     * @throws NullPointerException if any non-null annotated parameter is
     *                              {@code null}
     */
    private boolean usePowerTool(@Nonnull final Player player, @Nonnull final ItemStack handItem,
            @Nonnull final Block block, @Nonnull final Material material, final boolean useDurabilityPerBlockNow) {

        // Only apply the change if the block would actually change
        if (block.getType() == material) {
            return false;
        }

        block.setType(material);

        // Reduce durability for each block that was actually changed
        if (useDurabilityPerBlockNow && player.getGameMode() == GameMode.SURVIVAL) {
            PowerUtils.reduceDurability(player, handItem);
        }
        return true;
    }

    /**
     * Performs fast, conservative pre-checks to decide whether a PowerTool
     * interaction should be handled.
     *
     * <p>
     * This method centralizes all early-exit checks for the interaction event. It
     * is intentionally
     * strict: if any required condition is not met, it returns {@code true} to
     * indicate that the caller
     * should ignore the event and do nothing.
     * </p>
     *
     * <h3>Validation performed</h3>
     * <ul>
     * <li><b>Action type:</b> Ignores
     * {@link org.bukkit.event.block.Action#LEFT_CLICK_BLOCK},
     * {@link org.bukkit.event.block.Action#LEFT_CLICK_AIR}, and
     * {@link org.bukkit.event.block.Action#RIGHT_CLICK_AIR}.</li>
     * <li><b>Player state:</b> Ignores interactions while the player is sneaking
     * (to preserve alternate behavior).</li>
     * <li><b>Held item:</b> Ignores if the held item is
     * {@link org.bukkit.Material#AIR}.</li>
     * <li><b>Clicked block:</b> Requires a non-null clicked block reference.</li>
     * <li><b>Block eligibility:</b> Requires the clicked block's type to be
     * considered tillable by
     * {@link jodelle.powermining.utils.PowerUtils#isTillable(Material)}.</li>
     * <li><b>Tool eligibility:</b> Requires the item to be recognized as a
     * PowerTool via
     * {@link jodelle.powermining.utils.PowerUtils#isPowerTool(ItemStack)}.</li>
     * <li><b>Permission check:</b> Requires
     * {@link jodelle.powermining.utils.PowerUtils#checkUsePermission(PowerMining, Player, Material)}
     * to succeed for the given tool type.</li>
     * </ul>
     *
     * <p>
     * If this method returns {@code false}, the caller may safely assume that
     * {@code block} is non-null and
     * that the interaction is a valid candidate for PowerTool processing.
     * </p>
     *
     * @param action       the interaction action performed by the player; must not
     *                     be {@code null}
     * @param player       the interacting player; must not be {@code null}
     * @param handItem     the item in the player's hand; must not be {@code null}
     * @param handItemType the {@link org.bukkit.Material} of {@code handItem}; must
     *                     not be {@code null}
     * @param block        the clicked block; may be {@code null} for non-block
     *                     interactions
     * @return {@code true} if the caller should ignore this interaction;
     *         {@code false} if processing may continue
     * @throws NullPointerException if any non-null annotated parameter is
     *                              {@code null}
     */
    private boolean basicVerifications(@Nonnull final Action action, @Nonnull final Player player,
            @Nonnull final ItemStack handItem, @Nonnull final Material handItemType, @Nullable final Block block) {
        if (action == Action.LEFT_CLICK_BLOCK) {
            return true;
        }
        if (action == Action.LEFT_CLICK_AIR) {
            return true;
        }
        if (action == Action.RIGHT_CLICK_AIR) {
            return true;
        }
        if (player.isSneaking()) {
            return true;
        }
        if (handItem.getType().equals(Material.AIR)) {
            return true;
        }
        if (block == null) {
            return true;
        }
        final @Nonnull Material blockType = Objects.requireNonNull(block.getType(), "blockType");
        if (!PowerUtils.isTillable(blockType)) {
            return true;
        }
        if (!PowerUtils.isPowerTool(handItem)) {
            return true;
        }
        if (!PowerUtils.checkUsePermission(plugin, player, handItemType)) {
            return true;
        }

        return false;
    }
}
