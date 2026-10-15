package tn.transportview.infrastructure.web.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RateLimitingFilterTest {

    private static final String PATH = "/api/v1/itineraries";

    @Test
    void passesThroughRequestsBelowTheLimit() throws Exception {
        RateLimitingFilter filter = new RateLimitingFilter();
        FilterChain chain = org.mockito.Mockito.mock(FilterChain.class);

        filter.doFilter(requestFrom("1.2.3.4"), new MockHttpServletResponse(), chain);

        verify(chain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void returns429OnceTheIpExceedsTheLimit() throws Exception {
        RateLimitingFilter filter = new RateLimitingFilter();
        FilterChain chain = org.mockito.Mockito.mock(FilterChain.class);

        for (int i = 0; i < 30; i++) {
            filter.doFilter(requestFrom("5.6.7.8"), new MockHttpServletResponse(), chain);
        }

        MockHttpServletResponse blockedResponse = new MockHttpServletResponse();
        filter.doFilter(requestFrom("5.6.7.8"), blockedResponse, chain);

        assertThat(blockedResponse.getStatus()).isEqualTo(429);
    }

    @Test
    void doesNotRateLimitOtherPaths() throws Exception {
        RateLimitingFilter filter = new RateLimitingFilter();
        FilterChain chain = org.mockito.Mockito.mock(FilterChain.class);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        request.setRemoteAddr("9.9.9.9");

        for (int i = 0; i < 40; i++) {
            filter.doFilter(request, new MockHttpServletResponse(), chain);
        }

        verify(chain, org.mockito.Mockito.times(40))
                .doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    private static MockHttpServletRequest requestFrom(String remoteAddr) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", PATH);
        request.setRemoteAddr(remoteAddr);
        return request;
    }
}
