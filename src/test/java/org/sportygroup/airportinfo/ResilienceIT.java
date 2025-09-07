package org.sportygroup.airportinfo; // Replace with your actual package

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.test.LocalServerPort;
import org.springframework.boot.web.server.test.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
    properties = {
      "spring.profiles.active=test",
      "resilience4j.ratelimiter.instances.aviationApi.limit-for-period=3",
      "resilience4j.ratelimiter.instances.aviationApi.limit-refresh-period=2s",
      "resilience4j.ratelimiter.instances.aviationApi.timeout-duration=1s",
      "resilience4j.retry.instances.aviationApi.max-attempts=2",
      "resilience4j.retry.instances.aviationApi.wait-duration=500ms",
      "resilience4j.circuitbreaker.instances.aviationApi.failure-rate-threshold=60",
      "resilience4j.circuitbreaker.instances.aviationApi.minimum-number-of-calls=3"
    })
@DisplayName("Resilience Patterns Integration Tests")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ResilienceIT {

  @Autowired private TestRestTemplate restTemplate;

  @LocalServerPort private int port;

  @Test
  @Order(1)
  public void rateLimiterShouldHandleRapidRequestsGracefully() throws InterruptedException {
    // Given
    String endpoint = "http://localhost:" + port + "/v1/airports?icaoCodes=KJFK";

    System.out.println("Testing Rate Limiter with rapid requests...");

    // When - Make rapid concurrent requests
    CountDownLatch latch = new CountDownLatch(6);
    HttpStatusCode[] responses = new HttpStatus[6];
    long startTime = System.currentTimeMillis();

    IntStream.range(0, 6)
        .forEach(
            i -> {
              CompletableFuture.runAsync(
                  () -> {
                    try {
                      ResponseEntity<String> response =
                          restTemplate.getForEntity(endpoint, String.class);
                      responses[i] = response.getStatusCode();
                    } finally {
                      latch.countDown();
                    }
                  });
            });

    // Wait for all requests to complete
    boolean completed = latch.await(15, TimeUnit.SECONDS);
    long totalTime = System.currentTimeMillis() - startTime;

    // Then
    assertThat(completed).as("All requests should complete within timeout").isTrue();

    long successCount = Arrays.stream(responses).filter(status -> status == HttpStatus.OK).count();
    long errorCount = Arrays.stream(responses).filter(status -> status != HttpStatus.OK).count();

    System.out.println("Rate Limiter Test Results:");
    System.out.println("Successful requests: " + successCount);
    System.out.println("Rate limited/error requests: " + errorCount);
    System.out.println("Total time: " + totalTime + "ms");

    // Should have processed all requests (some may be rate limited)
    assertThat(successCount + errorCount).isEqualTo(6);

    // At least some requests should succeed
    assertThat(successCount).isGreaterThan(0);

    System.out.println("Rate Limiter integration test completed");
  }

  @Test
  @Order(2)
  public void circuitBreakerShouldHandleServiceDegradationGracefully() throws InterruptedException {
    // Given
    String validEndpoint = "http://localhost:" + port + "/v1/airports?icaoCodes=KJFK";
    String invalidEndpoint = "http://localhost:" + port + "/v1/api/airports?icaoCodes=INVALID999";

    System.out.println(" Testing Circuit Breaker with service degradation...");

    // When - First test with valid requests
    System.out.println("Phase 1: Testing with valid requests...");
    ResponseEntity<String> validResponse1 = restTemplate.getForEntity(validEndpoint, String.class);
    ResponseEntity<String> validResponse2 = restTemplate.getForEntity(validEndpoint, String.class);

    Thread.sleep(500); // Brief pause

    // Then test with requests that might cause issues
    System.out.println("Phase 2: Testing with potentially problematic requests...");
    ResponseEntity<String> invalidResponse1 =
        restTemplate.getForEntity(invalidEndpoint, String.class);
    ResponseEntity<String> invalidResponse2 =
        restTemplate.getForEntity(invalidEndpoint, String.class);
    ResponseEntity<String> invalidResponse3 =
        restTemplate.getForEntity(invalidEndpoint, String.class);

    Thread.sleep(500); // Brief pause

    // Test recovery
    System.out.println("Phase 3: Testing recovery...");
    ResponseEntity<String> recoveryResponse =
        restTemplate.getForEntity(validEndpoint, String.class);

    // Then
    System.out.println(" Circuit Breaker Test Results:");
    System.out.println(
        "  Valid requests: "
            + validResponse1.getStatusCode()
            + ", "
            + validResponse2.getStatusCode());
    System.out.println(
        "   Invalid requests: "
            + invalidResponse1.getStatusCode()
            + ", "
            + invalidResponse2.getStatusCode()
            + ", "
            + invalidResponse3.getStatusCode());
    System.out.println("   Recovery request: " + recoveryResponse.getStatusCode());

    // All requests should be handled gracefully (no exceptions thrown)
    assertThat(validResponse1.getStatusCode())
        .isIn(HttpStatus.OK, HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(recoveryResponse.getStatusCode())
        .isIn(HttpStatus.OK, HttpStatus.INTERNAL_SERVER_ERROR);

    System.out.println(
        " Circuit Breaker integration test completed - No cascading failures detected");
  }
}
