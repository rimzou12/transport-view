package tn.transportview.infrastructure.web.filter;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple per-key sliding-window rate limiter, in-memory. Appropriate for a single-instance,
 * public, no-auth MVP endpoint; a multi-instance deployment would need a shared store
 * (e.g. Redis) instead, since this state doesn't survive a restart or get shared across nodes.
 */
public class SlidingWindowRateLimiter {

    private final int maxRequestsPerWindow;
    private final Duration window;
    private final ConcurrentHashMap<String, Deque<Instant>> requestTimestampsByKey = new ConcurrentHashMap<>();

    public SlidingWindowRateLimiter(int maxRequestsPerWindow, Duration window) {
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.window = window;
    }

    public boolean tryAcquire(String key, Instant now) {
        Deque<Instant> timestamps = requestTimestampsByKey.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (timestamps) {
            evictExpired(timestamps, now);
            if (timestamps.size() >= maxRequestsPerWindow) {
                return false;
            }
            timestamps.addLast(now);
            return true;
        }
    }

    private void evictExpired(Deque<Instant> timestamps, Instant now) {
        Instant cutoff = now.minus(window);
        while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(cutoff)) {
            timestamps.pollFirst();
        }
    }
}
