package chestcleaner.sorting;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InventorySorterProtectionTest {

    @Test
    void cancelledSortingEventLeavesInventoryUntouched() {
        Inventory inventory = mock(Inventory.class);
        Player player = mock(Player.class);
        ItemStack airItem = airItem();
        when(inventory.iterator()).thenReturn(Collections.singletonList(airItem).listIterator());
        PluginManager pluginManager = mock(PluginManager.class);
        doAnswer(invocation -> {
            ((SortingEvent) invocation.getArgument(0)).setCancelled(true);
            return null;
        }).when(pluginManager).callEvent(any(Event.class));

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(Bukkit::getPluginManager).thenReturn(pluginManager);

            assertFalse(InventorySorter.sortInventory(inventory, player));
            verify(inventory, never()).clear();
            verify(inventory, never()).setItem(org.mockito.ArgumentMatchers.anyInt(), any(ItemStack.class));
        }
    }

    /**
     * A mocked air item that {@link chestcleaner.utils.InventoryConverter} filters
     * out. Constructing a real {@code ItemStack} in Paper 26.3 needs the item
     * registry, which requires a running server.
     */
    private static ItemStack airItem() {
        ItemStack item = mock(ItemStack.class);
        when(item.getType()).thenReturn(Material.AIR);
        return item;
    }
}
