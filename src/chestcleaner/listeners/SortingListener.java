package chestcleaner.listeners;

import chestcleaner.cooldown.CMRegistry;
import chestcleaner.cooldown.InteractionCooldown;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import chestcleaner.config.PlayerDataManager;
import chestcleaner.config.PluginConfigManager;
import chestcleaner.sorting.InventorySorter;
import chestcleaner.utils.InventoryDetector;
import chestcleaner.utils.PluginPermissions;
import chestcleaner.utils.SortingUtils;
import chestcleaner.utils.StorageSafety;
import chestcleaner.utils.messages.MessageSystem;

/**
 * @author Tom2208
 */
public class SortingListener implements org.bukkit.event.Listener {

    private String[] usableAnimalInventories = {"CraftMule", "CraftLlama", "CraftHorse"};
    private InventoryType[] getInventoryTypeWhiteList() {
        return new InventoryType[] {InventoryType.CHEST, InventoryType.ENDER_CHEST, InventoryType.BARREL, InventoryType.SHULKER_BOX, InventoryType.PLAYER, InventoryType.CREATIVE,
                InventoryType.DISPENSER, InventoryType.DROPPER, InventoryType.HOPPER};
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    private void onRightClick(PlayerInteractEvent e) {

        Player player = e.getPlayer();

        if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (PluginConfigManager.isCleaningItemActive()) {
                boolean mainHandHasBrush = isPlayerHoldingCleaningItemInMainHand(player);
                boolean offHandHasBrush = isPlayerHoldingCleaningItemInOffHand(player);
                if (!CleaningItemClickPolicy.isMatchingHand(e.getHand(), mainHandHasBrush, offHandHasBrush)) return;

                Block clickedBlock = e.getClickedBlock();
                boolean sneakStorageClick = CleaningItemClickPolicy.isSneakStorageClick(
                        e.getAction(), player.isSneaking(), isStorageBlock(clickedBlock));

                if (sneakStorageClick) {
                    // Do not turn a denied/locked container interaction into a personal inventory sort.
                    if (CleaningItemClickPolicy.isDeniedInteraction(e)) return;
                    if (StorageSafety.isLocked(clickedBlock)) return;
                    if (!player.hasPermission(PluginPermissions.CLEANING_ITEM_USE.getString())) return;
                    if (CMRegistry.isOnCooldown(CMRegistry.CMIdentifier.SORTING, player)) return;
                    if (PluginConfigManager.getBlacklistInventory().contains(clickedBlock.getType())) return;

                    // A Bedrock double event would otherwise sort the container twice.
                    if (isDuplicateInteraction(player, InteractionCooldown.ActionKind.CONTAINER_SORT)) {
                        e.setCancelled(true);
                        return;
                    }

                    if (InventorySorter.sortPlayerBlock(clickedBlock, player)) {
                        damageItem(player);
                        InventorySorter.playSortingSound(player);
                        MessageSystem.sendSortedMessage(player);
                        e.setCancelled(true);
                    }
                    return;
                }

                if (isPlayerAllowedToCleanOwnInv(player)) {
                    if (isDuplicateInteraction(player, InteractionCooldown.ActionKind.INVENTORY_SORT)) {
                        e.setCancelled(true);
                        return;
                    }

                    if (SortingUtils.sortPlayerInvWithEffects(player)) {
                        damageItem(player);
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    private boolean isStorageBlock(Block block) {
        return block != null && (InventoryDetector.hasInventoryHolder(block)
                || block.getType() == Material.ENDER_CHEST);
    }

    @EventHandler
    private void onCloseInventory(InventoryCloseEvent e) {
        // Doing the auto sorting here
        if (e.getInventory().getType().equals(InventoryType.ENDER_CHEST)
                || e.getInventory().getType().equals(InventoryType.CHEST)) {

            Player player = (Player) e.getPlayer();

            if (PlayerDataManager.isAutoSort(player)) {
                // The close event cannot be cancelled, so a duplicate just skips the sort.
                SortingUtils.sortInventoryWithEffects(e.getInventory(), player);
            }
        }
    }

    @EventHandler
    private void onClick(InventoryClickEvent e) {
        if (e.getClick().equals(ClickType.MIDDLE) || isNewClick(e)) {
            if (e.getWhoClicked().hasPermission(PluginPermissions.CLICK_SORT.getString())) {
                if (e.getSlot() == -999) {
                    if (e.getCurrentItem() == null) {
                        Player player = Bukkit.getServer().getPlayer(e.getWhoClicked().getName());
                        if (PlayerDataManager.isClickSort(player)
                                && !isDuplicateInteraction(player, InteractionCooldown.ActionKind.CLICK_SORT)) {
                            sortInventoryOnClick(player, e);
                        }
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    private boolean isNewClick(InventoryClickEvent e){
        return e.getClick().equals(ClickType.RIGHT) && e.getCurrentItem() == null;
    }

    /**
     * Reports whether the player repeated the same action inside the configured
     * interaction debounce window, which happens when a Bedrock client sends one press
     * as two events. The accepted interaction is recorded on the first call.
     */
    private static boolean isDuplicateInteraction(Player player, InteractionCooldown.ActionKind kind) {
        return InteractionCooldown.shared().isDuplicate(player.getUniqueId(), kind);
    }

    /** Drops the debounce state of a player who leaves, so it cannot leak. */
    @EventHandler
    private void onQuit(PlayerQuitEvent e) {
        InteractionCooldown.shared().clear(e.getPlayer().getUniqueId());
    }

    private void sortInventoryOnClick(Player player, InventoryClickEvent e) {

        boolean flag = false;
        for (InventoryType type : getInventoryTypeWhiteList()) {
            if (e.getInventory().getType().equals(type)) {
                flag = true;
                break;
            }
        }

        boolean animalInv = false;

        if (e.getInventory().getType().equals(InventoryType.CHEST)) {
            for (String holder : usableAnimalInventories) {
                if (e.getInventory().getHolder() != null && e.getInventory().getHolder().toString().contains(holder)) {
                    animalInv = true;
                    break;
                }
            }
        }
        if (!flag || animalInv) {
            SortingUtils.sortPlayerInvWithEffects(player);
        } else {
            SortingUtils.sortInventoryWithEffects(e.getInventory(), player);
        }

    }

    private ItemStack getComparableItem(ItemStack item) {
        ItemStack compItem = item.clone();
        ItemMeta itemMeta = compItem.getItemMeta();
        Damageable damageable = ((Damageable) itemMeta);
        if (damageable != null) {
            damageable.setDamage(0);
            compItem.setItemMeta(itemMeta);
        }
        return compItem;
    }

    private boolean isPlayerHoldingCleaningItemInMainHand(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().equals(Material.AIR)) {
            return false;
        }
        return getComparableItem(item).isSimilar(PluginConfigManager.getCleaningItem());
    }

    private boolean isPlayerHoldingCleaningItemInOffHand(Player player) {
        ItemStack item = player.getInventory().getItemInOffHand();
        if (item.getType().equals(Material.AIR)) {
            return false;
        }
        return getComparableItem(item).isSimilar(PluginConfigManager.getCleaningItem());
    }

    private boolean isPlayerAllowedToCleanOwnInv(Player player) {
        return player.hasPermission(PluginPermissions.CLEANING_ITEM_USE_OWN_INV.getString()) && player.isSneaking();
    }

    /**
     * Damages the item in the Hand of the {@code player} (using
     * player.getItemInHand()), if the {@code durability} (in class Main) is true.
     * Damaging means, that stackable items (maxStackSize > 1) get reduced in amount
     * by one, not stackable items get damaged and removed, if they reach the
     * highest durability .
     *
     * @param player the player who is holding the item, that you want to get
     *               damaged, in hand.
     */
    private void damageItem(Player player) {

        if (PluginConfigManager.isDurabilityLossActive()) {

            ItemStack item;
            if (isPlayerHoldingCleaningItemInMainHand(player)) {
                item = player.getInventory().getItemInMainHand();
            } else {
                item = player.getInventory().getItemInOffHand();
            }

            ItemMeta itemMeta = item.getItemMeta();
            Damageable damageable = ((Damageable) itemMeta);
            if (damageable != null) {
                if (!(damageable.getDamage() + 1 >= item.getType().getMaxDurability())) {
                    damageable.setDamage(damageable.getDamage() + 1);
                } else {
                    item.setAmount(item.getAmount() - 1);
                }
                item.setItemMeta(itemMeta);
            }
        }

    }

}
