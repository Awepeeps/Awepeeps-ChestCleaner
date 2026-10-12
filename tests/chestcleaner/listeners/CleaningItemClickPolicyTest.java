package chestcleaner.listeners;

import org.bukkit.event.block.Action;
import org.bukkit.event.Event;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CleaningItemClickPolicyTest {

    @Test
    void onlySneakRightClickOnStorageInterceptsTheContainer() {
        assertTrue(CleaningItemClickPolicy.isSneakStorageClick(Action.RIGHT_CLICK_BLOCK, true, true));
        assertFalse(CleaningItemClickPolicy.isSneakStorageClick(Action.RIGHT_CLICK_BLOCK, false, true));
        assertFalse(CleaningItemClickPolicy.isSneakStorageClick(Action.RIGHT_CLICK_BLOCK, true, false));
        assertFalse(CleaningItemClickPolicy.isSneakStorageClick(Action.RIGHT_CLICK_AIR, true, true));
    }

    @Test
    void matchingBrushHandIsProcessedOnceAndMainHandWinsWhenBothMatch() {
        assertTrue(CleaningItemClickPolicy.isMatchingHand(EquipmentSlot.HAND, true, true));
        assertFalse(CleaningItemClickPolicy.isMatchingHand(EquipmentSlot.OFF_HAND, true, true));
        assertFalse(CleaningItemClickPolicy.isMatchingHand(EquipmentSlot.HAND, false, true));
        assertTrue(CleaningItemClickPolicy.isMatchingHand(EquipmentSlot.OFF_HAND, false, true));
        assertFalse(CleaningItemClickPolicy.isMatchingHand(null, false, true));
    }

    @Test
    void explicitBlockCancellationAndUseDenialAreRejectedForContainerRouting() {
        Player player = mockPlayer();
        Block block = org.mockito.Mockito.mock(Block.class);
        PlayerInteractEvent deniedBlock = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK,
                new ItemStack(Material.IRON_HOE), block, BlockFace.UP, EquipmentSlot.HAND);
        deniedBlock.setUseInteractedBlock(Event.Result.DENY);
        assertTrue(CleaningItemClickPolicy.isDeniedInteraction(deniedBlock));

        PlayerInteractEvent cancelledBlock = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK,
                new ItemStack(Material.IRON_HOE), block, BlockFace.UP, EquipmentSlot.HAND);
        cancelledBlock.setCancelled(true);
        assertTrue(CleaningItemClickPolicy.isDeniedInteraction(cancelledBlock));
    }

    private Player mockPlayer() {
        return org.mockito.Mockito.mock(Player.class);
    }
}
