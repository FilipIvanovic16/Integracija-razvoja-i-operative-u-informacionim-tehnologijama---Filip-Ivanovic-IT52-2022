package com.chronoshop.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import static org.assertj.core.api.Assertions.assertThat;

class PublicRouteMatcherTest {

    private final PublicRouteMatcher matcher = new PublicRouteMatcher();

    @Test
    void authEndpointsArePublic() {
        assertThat(matcher.isPublic("/api/auth/login", HttpMethod.POST)).isTrue();
        assertThat(matcher.isPublic("/api/auth/register", HttpMethod.POST)).isTrue();
    }

    @Test
    void stripeWebhookIsPublic() {
        assertThat(matcher.isPublic("/api/payments/webhook", HttpMethod.POST)).isTrue();
    }

    @Test
    void catalogBrowsingIsPublicOnlyForGet() {
        assertThat(matcher.isPublic("/api/watches", HttpMethod.GET)).isTrue();
        assertThat(matcher.isPublic("/api/watches/5", HttpMethod.GET)).isTrue();
        assertThat(matcher.isPublic("/api/watches", HttpMethod.POST)).isFalse();
        assertThat(matcher.isPublic("/api/watches/5", HttpMethod.DELETE)).isFalse();
    }

    @Test
    void ordersAndAccountRequireAuthentication() {
        assertThat(matcher.isPublic("/api/orders", HttpMethod.GET)).isFalse();
        assertThat(matcher.isPublic("/api/account/me", HttpMethod.GET)).isFalse();
        assertThat(matcher.isPublic("/api/admin/orders", HttpMethod.GET)).isFalse();
    }

    @Test
    void actuatorIsPublic() {
        assertThat(matcher.isPublic("/actuator/health", HttpMethod.GET)).isTrue();
    }
}
