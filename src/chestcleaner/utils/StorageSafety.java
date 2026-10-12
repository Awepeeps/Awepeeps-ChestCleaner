package chestcleaner.utils;

import org.bukkit.block.Block;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Lockable;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/** Checks native lock metadata before a block inventory is sorted directly. */
public final class StorageSafety {

    private StorageSafety() {
    }

    public static boolean isLocked(Block block) {
        if (block == null) return false;
        if (isLockedState(block.getState())) return true;

        // A double chest exposes a combined inventory, so reject it if either half is locked.
        if (InventoryDetector.hasInventoryHolder(block)) {
            Inventory inventory = InventoryDetector.getInventoryFormBlock(block);
            if (inventory != null && inventory.getHolder() instanceof DoubleChest) {
                DoubleChest doubleChest = (DoubleChest) inventory.getHolder();
                return isLockedHolder(doubleChest.getLeftSide()) || isLockedHolder(doubleChest.getRightSide());
            }
        }
        return false;
    }

    private static boolean isLockedHolder(InventoryHolder holder) {
        return holder instanceof Lockable && isLockedState((Lockable) holder);
    }

    private static boolean isLockedState(Object state) {
        if (!(state instanceof Lockable)) {
            return false;
        }
        String key = ((Lockable) state).getLock();
        return key != null && !key.isEmpty();
    }
}
