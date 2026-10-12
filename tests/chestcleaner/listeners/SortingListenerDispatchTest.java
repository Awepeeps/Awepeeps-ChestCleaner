package chestcleaner.listeners;

import chestcleaner.config.PluginConfigManager;
import chestcleaner.cooldown.CMRegistry;
import chestcleaner.sorting.InventorySorter;
import chestcleaner.utils.SortingUtils;
import chestcleaner.utils.StorageSafety;
import chestcleaner.utils.messages.MessageSystem;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.ArgumentMatchers.anyInt;

class SortingListenerDispatchTest {

    @Test
    void normalBrushClickOpensContainerWithoutSortingEvenWithLegacyOpenEventEnabled() throws Exception {
        Fixture fixture = new Fixture(false, true);
        try (DispatchMocks mocks = new DispatchMocks(fixture, false, true)) {
            PlayerInteractEvent event = fixture.event(Action.RIGHT_CLICK_BLOCK, fixture.container, EquipmentSlot.HAND);

            invoke(fixture.listener, event);

            assertFalse(event.isCancelled());
            mocks.verifyBlockSortNever();
            mocks.verifyPlayerSortNever();
        }
    }

    @Test
    void sneakContainerSortsBeforeSelfInventoryAndConsumesOneUse() throws Exception {
        Fixture fixture = new Fixture(true, true);
        try (DispatchMocks mocks = new DispatchMocks(fixture, true, false)) {
            mocks.blockSort(fixture, true);
            PlayerInteractEvent event = fixture.event(Action.RIGHT_CLICK_BLOCK, fixture.container, EquipmentSlot.HAND);

            invoke(fixture.listener, event);

            assertTrue(event.isCancelled());
            assertEquals(1, damage(fixture.mainHand));
            mocks.verifyBlockSortOnce(fixture);
            mocks.verifyPlayerSortNever();
            mocks.verifyOneSortEffect(fixture);
        }
    }

    @Test
    void sneakAirStillSortsPersonalInventoryWhenVanillaMarksNoActionDenied() throws Exception {
        Fixture fixture = new Fixture(true, true);
        try (DispatchMocks mocks = new DispatchMocks(fixture, true, false)) {
            mocks.playerSort(fixture, true);
            PlayerInteractEvent event = fixture.event(Action.RIGHT_CLICK_AIR, null, EquipmentSlot.HAND);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DEFAULT);
            event.setCancelled(true);

            invoke(fixture.listener, event);

            assertTrue(event.isCancelled());
            assertEquals(1, damage(fixture.mainHand));
            mocks.verifyBlockSortNever();
            mocks.verifyPlayerSortOnce(fixture);
        }
    }

    @Test
    void sneakNonContainerClickRetainsPersonalSortEvenWhenVanillaDoesNotHandleTheClick() throws Exception {
        Fixture fixture = new Fixture(true, true);
        fixture.nonContainer();
        try (DispatchMocks mocks = new DispatchMocks(fixture, true, false)) {
            mocks.playerSort(fixture, true);
            PlayerInteractEvent event = fixture.event(Action.RIGHT_CLICK_BLOCK, fixture.container, EquipmentSlot.HAND);
            event.setCancelled(true);

            invoke(fixture.listener, event);

            assertTrue(event.isCancelled());
            assertEquals(1, damage(fixture.mainHand));
            mocks.verifyBlockSortNever();
            mocks.verifyPlayerSortOnce(fixture);
        }
    }

    @Test
    void claimDenialDoesNotSortContainerOrFallThroughToPersonalInventory() throws Exception {
        Fixture fixture = new Fixture(true, true);
        try (DispatchMocks mocks = new DispatchMocks(fixture, true, false)) {
            mocks.playerSort(fixture, true);
            PlayerInteractEvent event = fixture.event(Action.RIGHT_CLICK_BLOCK, fixture.container, EquipmentSlot.HAND);
            event.setUseInteractedBlock(Event.Result.DENY);

            invoke(fixture.listener, event);

            assertTrue(event.isCancelled());
            assertEquals(0, damage(fixture.mainHand));
            mocks.verifyBlockSortNever();
            mocks.verifyPlayerSortNever();
        }
    }

    @Test
    void cancelledInventorySortHasNoEffectsOrDurabilityCharge() throws Exception {
        Fixture fixture = new Fixture(true, true);
        try (DispatchMocks mocks = new DispatchMocks(fixture, true, false)) {
            mocks.blockSort(fixture, false);
            PlayerInteractEvent event = fixture.event(Action.RIGHT_CLICK_BLOCK, fixture.container, EquipmentSlot.HAND);

            invoke(fixture.listener, event);

            assertFalse(event.isCancelled());
            assertEquals(0, damage(fixture.mainHand));
            mocks.verifyBlockSortOnce(fixture);
            mocks.verifyNoSortEffect(fixture);
            mocks.verifyPlayerSortNever();
        }
    }

    @Test
    void directOffhandOnlyBrushUseSortsAndDamagesTheOffhandItem() throws Exception {
        Fixture fixture = new Fixture(true, false);
        when(fixture.offHand.getType()).thenReturn(Material.IRON_HOE);
        try (DispatchMocks mocks = new DispatchMocks(fixture, true, false)) {
            mocks.blockSort(fixture, true);
            PlayerInteractEvent event = fixture.event(Action.RIGHT_CLICK_BLOCK, fixture.container, EquipmentSlot.OFF_HAND);

            invoke(fixture.listener, event);

            assertTrue(event.isCancelled());
            assertEquals(0, damage(fixture.mainHand));
            assertEquals(1, damage(fixture.offHand));
            mocks.verifyBlockSortOnce(fixture);
            mocks.verifyPlayerSortNever();
        }
    }

    @Test
    void mainAndOffhandEventsCannotSortOrChargeTwiceEvenWhenCooldownIsDisabled() throws Exception {
        Fixture fixture = new Fixture(true, true);
        when(fixture.offHand.getType()).thenReturn(Material.IRON_HOE);
        try (DispatchMocks mocks = new DispatchMocks(fixture, true, false)) {
            mocks.blockSort(fixture, true);
            PlayerInteractEvent mainHand = fixture.event(Action.RIGHT_CLICK_BLOCK, fixture.container, EquipmentSlot.HAND);
            PlayerInteractEvent offHand = fixture.event(Action.RIGHT_CLICK_BLOCK, fixture.container, EquipmentSlot.OFF_HAND);

            invoke(fixture.listener, mainHand);
            invoke(fixture.listener, offHand);

            assertTrue(mainHand.isCancelled());
            assertFalse(offHand.isCancelled());
            assertEquals(1, damage(fixture.mainHand));
            assertEquals(0, damage(fixture.offHand));
            mocks.verifyBlockSortOnce(fixture);
            mocks.verifyOneSortEffect(fixture);
            mocks.verifyPlayerSortNever();
        }
    }

    private static int damage(ItemStack item) {
        return ((Damageable) item.getItemMeta()).getDamage();
    }

    private static void invoke(SortingListener listener, PlayerInteractEvent event) throws Exception {
        Method method = SortingListener.class.getDeclaredMethod("onRightClick", PlayerInteractEvent.class);
        method.setAccessible(true);
        method.invoke(listener, event);
    }

    private static final class Fixture {
        private final SortingListener listener = new SortingListener();
        private final Player player = mock(Player.class);
        private final PlayerInventory inventory = mock(PlayerInventory.class);
        private final Block container = mock(Block.class);
        private final TestItem offhandItem = new TestItem(Material.AIR);
        private final ItemStack offHand = offhandItem.item;
        private final ItemStack cleaningItem = new TestItem(Material.IRON_HOE).item;
        private final ItemStack mainHand;
        private final TestItem mainHandItem;

        private Fixture(boolean sneaking, boolean mainHandHasBrush) {
            mainHandItem = new TestItem(mainHandHasBrush ? Material.IRON_HOE : Material.AIR);
            mainHand = mainHandItem.item;
            BlockState state = mock(BlockState.class, org.mockito.Mockito.withSettings().extraInterfaces(org.bukkit.inventory.InventoryHolder.class));
            when(player.isSneaking()).thenReturn(sneaking);
            when(player.getInventory()).thenReturn(inventory);
            when(player.hasPermission(anyString())).thenReturn(true);
            when(inventory.getItemInMainHand()).thenReturn(mainHand);
            when(inventory.getItemInOffHand()).thenReturn(offHand);
            when(container.getState()).thenReturn(state);
            when(container.getType()).thenReturn(Material.CHEST);
        }

        private PlayerInteractEvent event(Action action, Block block, EquipmentSlot hand) {
            return new PlayerInteractEvent(player, action, hand == EquipmentSlot.HAND ? mainHand : offHand,
                    block, block == null ? BlockFace.SELF : BlockFace.NORTH, hand);
        }

        private void nonContainer() {
            when(container.getState()).thenReturn(mock(BlockState.class));
            when(container.getType()).thenReturn(Material.STONE);
        }
    }

    private static final class TestItem {
        private final ItemStack item = mock(ItemStack.class);
        private final ItemStack comparison = mock(ItemStack.class);
        private final Damageable meta = mock(Damageable.class);
        private final Damageable comparisonMeta = mock(Damageable.class);
        private int damage;

        private TestItem(Material material) {
            when(item.getType()).thenReturn(material);
            when(item.getItemMeta()).thenReturn(meta);
            when(item.clone()).thenReturn(comparison);
            when(meta.getDamage()).thenAnswer(invocation -> damage);
            doAnswer(invocation -> {
                damage = invocation.getArgument(0);
                return null;
            }).when(meta).setDamage(anyInt());
            when(comparison.getType()).thenReturn(material);
            when(comparison.getItemMeta()).thenReturn(comparisonMeta);
            when(comparisonMeta.getDamage()).thenReturn(0);
            when(comparison.isSimilar(org.mockito.ArgumentMatchers.any(ItemStack.class))).thenReturn(true);
        }
    }

    private static final class DispatchMocks implements AutoCloseable {
        private final Fixture fixture;
        private final MockedStatic<PluginConfigManager> config = org.mockito.Mockito.mockStatic(PluginConfigManager.class);
        private final MockedStatic<SortingUtils> sortingUtils = org.mockito.Mockito.mockStatic(SortingUtils.class);
        private final MockedStatic<InventorySorter> sorter = org.mockito.Mockito.mockStatic(InventorySorter.class);
        private final MockedStatic<CMRegistry> cooldown = org.mockito.Mockito.mockStatic(CMRegistry.class);
        private final MockedStatic<StorageSafety> storageSafety = org.mockito.Mockito.mockStatic(StorageSafety.class);
        private final MockedStatic<MessageSystem> messages = org.mockito.Mockito.mockStatic(MessageSystem.class);

        private DispatchMocks(Fixture fixture, boolean durability, boolean openEvent) {
            this.fixture = fixture;
            config.when(PluginConfigManager::isCleaningItemActive).thenReturn(true);
            config.when(PluginConfigManager::getCleaningItem).thenReturn(fixture.cleaningItem);
            config.when(PluginConfigManager::isDurabilityLossActive).thenReturn(durability);
            config.when(PluginConfigManager::isOpenEvent).thenReturn(openEvent);
            config.when(PluginConfigManager::getBlacklistInventory).thenReturn(Collections.emptyList());
            cooldown.when(() -> CMRegistry.isOnCooldown(CMRegistry.CMIdentifier.SORTING, fixture.player)).thenReturn(false);
            storageSafety.when(() -> StorageSafety.isLocked(fixture.container)).thenReturn(false);
        }

        private void blockSort(Fixture fixture, boolean sorted) {
            sorter.when(() -> InventorySorter.sortPlayerBlock(fixture.container, fixture.player)).thenReturn(sorted);
        }

        private void playerSort(Fixture fixture, boolean sorted) {
            sortingUtils.when(() -> SortingUtils.sortPlayerInvWithEffects(fixture.player)).thenReturn(sorted);
        }

        private void verifyBlockSortOnce(Fixture fixture) {
            sorter.verify(() -> InventorySorter.sortPlayerBlock(fixture.container, fixture.player));
        }

        private void verifyBlockSortNever() {
            sorter.verifyNoInteractions();
        }

        private void verifyPlayerSortOnce(Fixture fixture) {
            sortingUtils.verify(() -> SortingUtils.sortPlayerInvWithEffects(fixture.player));
        }

        private void verifyPlayerSortNever() {
            sortingUtils.verifyNoInteractions();
        }

        private void verifyOneSortEffect(Fixture fixture) {
            sorter.verify(() -> InventorySorter.playSortingSound(fixture.player));
            messages.verify(() -> MessageSystem.sendSortedMessage(fixture.player));
        }

        private void verifyNoSortEffect(Fixture fixture) {
            sorter.verify(() -> InventorySorter.playSortingSound(fixture.player), never());
            messages.verify(() -> MessageSystem.sendSortedMessage(fixture.player), never());
        }

        @Override
        public void close() {
            messages.close();
            storageSafety.close();
            cooldown.close();
            sorter.close();
            sortingUtils.close();
            config.close();
        }
    }
}
