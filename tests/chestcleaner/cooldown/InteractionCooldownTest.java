package chestcleaner.cooldown;

import chestcleaner.cooldown.InteractionCooldown.ActionKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InteractionCooldownTest {

    private static final UUID ALICE = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID BOB = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private final AtomicLong now = new AtomicLong(1_000_000L);
    private final AtomicInteger window = new AtomicInteger(750);
    private InteractionCooldown cooldown;

    @BeforeEach
    void setUp() {
        now.set(1_000_000L);
        window.set(750);
        cooldown = new InteractionCooldown(window::get, now::get);
    }

    private void advance(long millis) {
        now.addAndGet(millis);
    }

    @Test
    void firstInteractionIsAllowed() {
        assertFalse(cooldown.isDuplicate(ALICE, ActionKind.CONTAINER_SORT));
    }

    @Test
    void repeatInsideTheWindowIsDropped() {
        assertFalse(cooldown.isDuplicate(ALICE, ActionKind.CONTAINER_SORT));
        advance(749);
        assertTrue(cooldown.isDuplicate(ALICE, ActionKind.CONTAINER_SORT));
    }

    @Test
    void repeatAtAndAfterTheWindowIsAllowed() {
        assertFalse(cooldown.isDuplicate(ALICE, ActionKind.CONTAINER_SORT));

        advance(750);
        assertFalse(cooldown.isDuplicate(ALICE, ActionKind.CONTAINER_SORT));

        advance(750);
        assertFalse(cooldown.isDuplicate(ALICE, ActionKind.CONTAINER_SORT));
    }

    @Test
    void droppedDuplicateDoesNotExtendTheWindow() {
        assertFalse(cooldown.isDuplicate(ALICE, ActionKind.CONTAINER_SORT));
        advance(400);
        assertTrue(cooldown.isDuplicate(ALICE, ActionKind.CONTAINER_SORT));

        // 350 ms after the second (dropped) attempt, but 750 ms after the accepted one.
        advance(350);
        assertFalse(cooldown.isDuplicate(ALICE, ActionKind.CONTAINER_SORT));
    }

    @Test
    void playersAreDebouncedIndependently() {
        assertFalse(cooldown.isDuplicate(ALICE, ActionKind.CONTAINER_SORT));
        assertFalse(cooldown.isDuplicate(BOB, ActionKind.CONTAINER_SORT));
        assertTrue(cooldown.isDuplicate(ALICE, ActionKind.CONTAINER_SORT));
        assertTrue(cooldown.isDuplicate(BOB, ActionKind.CONTAINER_SORT));
    }

    @Test
    void actionKindsAreDebouncedIndependently() {
        assertFalse(cooldown.isDuplicate(ALICE, ActionKind.CONTAINER_SORT));
        assertFalse(cooldown.isDuplicate(ALICE, ActionKind.INVENTORY_SORT));
        assertFalse(cooldown.isDuplicate(ALICE, ActionKind.CLICK_SORT));
        assertTrue(cooldown.isDuplicate(ALICE, ActionKind.CONTAINER_SORT));
    }

    @Test
    void zeroWindowDisablesTheDebounce() {
        window.set(0);

        assertFalse(cooldown.isDuplicate(ALICE, ActionKind.CONTAINER_SORT));
        assertFalse(cooldown.isDuplicate(ALICE, ActionKind.CONTAINER_SORT));
        assertFalse(cooldown.isDuplicate(ALICE, ActionKind.CONTAINER_SORT));
        assertEquals(0, cooldown.trackedPlayers());
    }

    @Test
    void negativeWindowDisablesTheDebounce() {
        window.set(-1);
        assertFalse(cooldown.isDuplicate(ALICE, ActionKind.CONTAINER_SORT));
        assertFalse(cooldown.isDuplicate(ALICE, ActionKind.CONTAINER_SORT));
    }

    @Test
    void clearDropsTheRecordedStateOfOnePlayerOnly() {
        assertFalse(cooldown.isDuplicate(ALICE, ActionKind.CONTAINER_SORT));
        assertFalse(cooldown.isDuplicate(BOB, ActionKind.CONTAINER_SORT));
        assertEquals(2, cooldown.trackedPlayers());

        cooldown.clear(ALICE);

        assertEquals(1, cooldown.trackedPlayers());
        assertFalse(cooldown.isDuplicate(ALICE, ActionKind.CONTAINER_SORT));
        assertTrue(cooldown.isDuplicate(BOB, ActionKind.CONTAINER_SORT));
    }

    @Test
    void windowIsReadOnEveryCheck() {
        assertFalse(cooldown.isDuplicate(ALICE, ActionKind.CONTAINER_SORT));
        advance(100);
        window.set(50);
        assertFalse(cooldown.isDuplicate(ALICE, ActionKind.CONTAINER_SORT));
    }

}
