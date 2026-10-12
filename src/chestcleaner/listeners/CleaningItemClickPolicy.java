package chestcleaner.listeners;

import org.bukkit.event.block.Action;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

/** Interaction routing for the configured cleaning item. */
final class CleaningItemClickPolicy {

    private CleaningItemClickPolicy() {
    }

    static boolean isSneakStorageClick(Action action, boolean sneaking, boolean storageBlock) {
        return action == Action.RIGHT_CLICK_BLOCK && sneaking && storageBlock;
    }

    static boolean isMatchingHand(EquipmentSlot eventHand, boolean mainHandHasBrush, boolean offHandHasBrush) {
        if (eventHand == null) return false;
        if (mainHandHasBrush) return eventHand == EquipmentSlot.HAND;
        return offHandHasBrush && eventHand == EquipmentSlot.OFF_HAND;
    }

    static boolean isDeniedInteraction(PlayerInteractEvent event) {
        return event.isCancelled() || event.useInteractedBlock() == Event.Result.DENY;
    }

}
