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
        when(inventory.iterator()).thenReturn(Collections.singletonList(new ItemStack(Material.STONE)).listIterator());
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
}
