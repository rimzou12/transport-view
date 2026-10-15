package tn.transportview.infrastructure.web.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class SlidingWindowRateLimiterTest {

    @Test
    void allowsRequestsUpToTheLimitWithinTheWindow() {
        SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(3, Duration.ofMinutes(1));
        Instant now = Instant.parse("2026-10-15T08:00:00Z");

        assertThat(limiter.tryAcquire("1.2.3.4", now)).isTrue();
        assertThat(limiter.tryAcquire("1.2.3.4", now.plusSeconds(1))).isTrue();
        assertThat(limiter.tryAcquire("1.2.3.4", now.plusSeconds(2))).isTrue();
    }

    @Test
    void rejectsRequestsOverTheLimitWithinTheWindow() {
        SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(2, Duration.ofMinutes(1));
        Instant now = Instant.parse("2026-10-15T08:00:00Z");

        assertThat(limiter.tryAcquire("1.2.3.4", now)).isTrue();
        assertThat(limiter.tryAcquire("1.2.3.4", now.plusSeconds(1))).isTrue();
        assertThat(limiter.tryAcquire("1.2.3.4", now.plusSeconds(2))).isFalse();
    }

    @Test
    void allowsRequestsAgainOnceOldEntriesFallOutsideTheWindow() {
        SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(1, Duration.ofMinutes(1));
        Instant now = Instant.parse("2026-10-15T08:00:00Z");

        assertThat(limiter.tryAcquire("1.2.3.4", now)).isTrue();
        assertThat(limiter.tryAcquire("1.2.3.4", now.plusSeconds(30))).isFalse();
        assertThat(limiter.tryAcquire("1.2.3.4", now.plusSeconds(61))).isTrue();
    }

    @Test
    void tracksEachKeyIndependently() {
        SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(1, Duration.ofMinutes(1));
        Instant now = Instant.parse("2026-10-15T08:00:00Z");

        assertThat(limiter.tryAcquire("1.1.1.1", now)).isTrue();
        assertThat(limiter.tryAcquire("2.2.2.2", now)).isTrue();
        assertThat(limiter.tryAcquire("1.1.1.1", now)).isFalse();
    }
}
