package chestcleaner.cooldown;

import chestcleaner.config.PluginConfigManager;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.IntSupplier;

/**
 * Per-player debounce for player-triggered actions.
 *
 * <p>Bedrock clients (Switch and other consoles, connected through Geyser/Floodgate)
 * can deliver a single press as two interaction or click events. Without a debounce
 * the same sort, refill or toggle therefore runs twice. This helper answers, for one
 * player and one {@link ActionKind}, whether an interaction repeats one that was
 * already accepted inside the configured {@code interaction-cooldown-ms} window.
 *
 * <p>The helper holds no Bukkit state and takes the current time from an injectable
 * monotonic {@link Clock}, so it is covered by plain unit tests. A window of {@code 0}
 * (or less) disables the debounce entirely.
 */
public final class InteractionCooldown {

    /** The distinct player-triggered action kinds the plugin debounces. */
    public enum ActionKind {
        CONTAINER_SORT,
        INVENTORY_SORT,
        CLICK_SORT
    }

    /** A monotonic clock, expressed in milliseconds. */
    @FunctionalInterface
    public interface Clock {
        long nowMillis();
    }

    private static final Clock MONOTONIC_CLOCK = () -> System.nanoTime() / 1_000_000L;

    private static InteractionCooldown shared;

    private final IntSupplier cooldownMillis;
    private final Clock clock;
    private final Map<UUID, EnumMap<ActionKind, Long>> lastAcceptedByPlayer = new HashMap<>();

    /**
     * @param cooldownMillis the debounce window in milliseconds, read on every check
     *                       so a config reload takes effect immediately; {@code <= 0}
     *                       disables the debounce.
     * @param clock          the monotonic clock used to stamp accepted interactions.
     */
    public InteractionCooldown(IntSupplier cooldownMillis, Clock clock) {
        this.cooldownMillis = Objects.requireNonNull(cooldownMillis, "cooldownMillis");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * The debounce shared by the event listeners. It reads {@code interaction-cooldown-ms}
     * from the plugin config.
     *
     * @return the shared instance.
     */
    public static InteractionCooldown shared() {
        if (shared == null) {
            shared = new InteractionCooldown(PluginConfigManager::getInteractionCooldownMillis, MONOTONIC_CLOCK);
        }
        return shared;
    }

    /**
     * Records {@code kind} for {@code playerId} and reports whether it duplicates an
     * interaction accepted inside the configured window.
     *
     * @param playerId the interacting player's unique id.
     * @param kind     the action the player triggered.
     * @return {@code true} when the caller must ignore the interaction, {@code false}
     *         when the caller must act on it.
     */
    public boolean isDuplicate(UUID playerId, ActionKind kind) {
        int window = cooldownMillis.getAsInt();
        if (window <= 0) {
            return false;
        }

        long now = clock.nowMillis();
        EnumMap<ActionKind, Long> lastAccepted = lastAcceptedByPlayer
                .computeIfAbsent(playerId, id -> new EnumMap<>(ActionKind.class));

        Long previous = lastAccepted.get(kind);
        if (previous != null && now - previous < window) {
            return true;
        }

        lastAccepted.put(kind, now);
        return false;
    }

    /** Drops the recorded interactions of one player, e.g. when they disconnect. */
    public void clear(UUID playerId) {
        lastAcceptedByPlayer.remove(playerId);
    }

    /** The number of players currently tracked; used by tests. */
    int trackedPlayers() {
        return lastAcceptedByPlayer.size();
    }

}
