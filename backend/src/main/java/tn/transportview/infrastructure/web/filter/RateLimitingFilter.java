package tn.transportview.infrastructure.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Rate-limits the public, no-auth itinerary search endpoint by client IP. Everything else
 * passes through untouched.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final String RATE_LIMITED_PATH = "/api/v1/itineraries";
    private static final int MAX_REQUESTS_PER_WINDOW = 30;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final int HTTP_TOO_MANY_REQUESTS = 429;

    private final SlidingWindowRateLimiter rateLimiter = new SlidingWindowRateLimiter(MAX_REQUESTS_PER_WINDOW, WINDOW);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!RATE_LIMITED_PATH.equals(request.getRequestURI()) || rateLimiter.tryAcquire(request.getRemoteAddr(), Instant.now())) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HTTP_TOO_MANY_REQUESTS);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"Too many requests, please slow down\"}");
    }
}
