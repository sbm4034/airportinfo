package org.sportygroup.airportinfo.service.health;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import java.util.Map;
import org.sportygroup.airportinfo.constants.AirportInfoConstant;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component(AirportEndpointHealthIndicator.AIRPORT_INFO_ENDPOINT)
public class AirportEndpointHealthIndicator implements HealthIndicator {

  public static final String AIRPORT_INFO_ENDPOINT = "airportInfoEndpoint";
  private final CircuitBreaker circuitBreaker;
  private final RateLimiter rateLimiter;

  public AirportEndpointHealthIndicator(
      @Qualifier(AirportInfoConstant.CIRCUIT_BREAKER_BEAN_AVIATION_CLIENT)
          CircuitBreaker circuitBreaker,
      @Qualifier(AirportInfoConstant.RATE_LIMITER_BEAN) RateLimiter rateLimiter) {
    this.circuitBreaker = circuitBreaker;
    this.rateLimiter = rateLimiter;
  }

  @Override
  public Health health() {
    Health.Builder builder = Health.up();

    try {
      // Circuit Breaker Health Details
      CircuitBreaker.State cbState = circuitBreaker.getState();
      CircuitBreaker.Metrics cbMetrics = circuitBreaker.getMetrics();

      builder.withDetail(
          "circuitBreaker",
          Map.of(
              "name", circuitBreaker.getName(),
              "state", cbState.toString(),
              "failureRate", String.format("%.2f%%", cbMetrics.getFailureRate()),
              "slowCallRate", String.format("%.2f%%", cbMetrics.getSlowCallRate()),
              "bufferedCalls", cbMetrics.getNumberOfBufferedCalls(),
              "failedCalls", cbMetrics.getNumberOfFailedCalls(),
              "successfulCalls", cbMetrics.getNumberOfSuccessfulCalls(),
              "slowCalls", cbMetrics.getNumberOfSlowCalls()));

      // Rate Limiter Health Details
      RateLimiter.Metrics rlMetrics = rateLimiter.getMetrics();
      builder.withDetail(
          "rateLimiter",
          Map.of(
              "name", rateLimiter.getName(),
              "availablePermissions", rlMetrics.getAvailablePermissions(),
              "waitingThreads", rlMetrics.getNumberOfWaitingThreads(),
              "limitForPeriod", rateLimiter.getRateLimiterConfig().getLimitForPeriod(),
              "limitRefreshPeriod",
                  rateLimiter.getRateLimiterConfig().getLimitRefreshPeriod().toString()));

      // Endpoint Status Logic
      if (cbState == CircuitBreaker.State.OPEN) {
        builder
            .down()
            .withDetail("status", "ENDPOINT_DOWN")
            .withDetail("reason", "Circuit breaker is OPEN - too many failures detected")
            .withDetail(
                "recommendation",
                "Wait for circuit breaker to recover or check downstream service");
      } else if (cbState == CircuitBreaker.State.HALF_OPEN) {
        builder
            .up() // Still UP but with warning
            .withDetail("status", "ENDPOINT_RECOVERING")
            .withDetail("reason", "Circuit breaker is HALF_OPEN - testing service recovery")
            .withDetail("recommendation", "Monitor closely - service is being tested");
      } else if (rlMetrics.getAvailablePermissions() == 0) {
        builder
            .up() // Rate limiting is expected behavior, so still UP
            .withDetail("status", "ENDPOINT_RATE_LIMITED")
            .withDetail("reason", "Rate limit reached - no available permissions")
            .withDetail("recommendation", "Requests are being throttled but service is healthy");
      } else {
        builder
            .up()
            .withDetail("status", "ENDPOINT_HEALTHY")
            .withDetail("reason", "All resilience components are operating normally");
      }

      // Additional endpoint info
      builder.withDetail(
          "endpoint",
          Map.of(
              "name", "Airport Info Service",
              "description",
                  "Provides airport information with circuit breaker and rate limiting protection",
              "lastChecked", java.time.Instant.now().toString()));

    } catch (Exception e) {
      builder
          .down()
          .withDetail("status", "ENDPOINT_CHECK_FAILED")
          .withDetail("error", e.getMessage())
          .withDetail("reason", "Health check execution failed")
          .withException(e);
    }

    return builder.build();
  }
}
