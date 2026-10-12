package chestcleaner.utils;

import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Lockable;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StorageSafetyTest {

    @Test
    void detectsNativeLockOnTheClickedBlock() {
        Block block = mock(Block.class);
        Chest state = mock(Chest.class);
        when(block.getState()).thenReturn(state);
        when(state.getLock()).thenReturn("redstone_key");

        assertTrue(StorageSafety.isLocked(block));
    }

    @Test
    void detectsLockOnEitherHalfOfCombinedChestInventory() {
        Block block = mock(Block.class);
        Chest state = mock(Chest.class);
        Inventory inventory = mock(Inventory.class);
        DoubleChest doubleChest = mock(DoubleChest.class);
        InventoryHolder left = mock(InventoryHolder.class, org.mockito.Mockito.withSettings().extraInterfaces(Lockable.class));
        InventoryHolder right = mock(InventoryHolder.class, org.mockito.Mockito.withSettings().extraInterfaces(Lockable.class));

        when(block.getState()).thenReturn(state);
        when(state.getLock()).thenReturn("");
        when(state.getInventory()).thenReturn(inventory);
        when(inventory.getHolder()).thenReturn(doubleChest);
        when(doubleChest.getLeftSide()).thenReturn(left);
        when(doubleChest.getRightSide()).thenReturn(right);
        when(((Lockable) left).getLock()).thenReturn("");
        when(((Lockable) right).getLock()).thenReturn("blue_key");

        assertTrue(StorageSafety.isLocked(block));
    }

    @Test
    void unlockedStorageDoesNotBlockSorting() {
        Block block = mock(Block.class);
        Chest state = mock(Chest.class);
        when(block.getState()).thenReturn(state);
        when(state.getLock()).thenReturn("");
        when(state.getInventory()).thenReturn(mock(Inventory.class));

        assertFalse(StorageSafety.isLocked(block));
    }
}
