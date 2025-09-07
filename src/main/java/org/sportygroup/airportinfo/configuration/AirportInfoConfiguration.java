package org.sportygroup.airportinfo.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import org.sportygroup.airportinfo.client.Client;
import org.sportygroup.airportinfo.client.ClientRegistry;
import org.sportygroup.airportinfo.client.aviation.AviationClient;
import org.sportygroup.airportinfo.constants.AirportInfoConstant;
import org.sportygroup.airportinfo.exceptions.HandledExceptions;
import org.sportygroup.airportinfo.exceptions.UnhandledException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.util.backoff.ExponentialBackOff;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Configuration
public class AirportInfoConfiguration {

  Set<Class<? extends Throwable>> RETRYABLE_EXCPETIONS = Set.of(RestClientException.class);

  @Bean(name = AirportInfoConstant.OBJECT_MAPPER_BEAN)
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  public CircuitBreakerRegistry circuitBreakerRegistry() {
    return CircuitBreakerRegistry.ofDefaults();
  }

  @Bean
  public RateLimiterRegistry rateLimiterRegistry() {
    return RateLimiterRegistry.ofDefaults();
  }

  @Bean
  public RetryRegistry retryRegistry() {
    return RetryRegistry.ofDefaults();
  }

  @Bean(name = AirportInfoConstant.RETRY_TEMPLATE_BEAN)
  public RetryTemplate retryTemplate(RetryRegistry retryRegistry) {
    ExponentialBackOff exponentialBackOff = new ExponentialBackOff(1000, 2);
    exponentialBackOff.setMaxAttempts(2);
    exponentialBackOff.setMaxInterval(6000);
    RetryPolicy retryPolicy =
        RetryPolicy.builder().includes(RETRYABLE_EXCPETIONS).backOff(exponentialBackOff).build();
    RetryTemplate retryTemplate = new RetryTemplate(retryPolicy);
    retryRegistry.retry(AirportInfoConstant.RETRY_TEMPLATE_NAME);
    return retryTemplate;
  }

  @Bean(name = AirportInfoConstant.CIRCUIT_BREAKER_BEAN_AVIATION_CLIENT)
  public CircuitBreaker circuitBreaker(CircuitBreakerRegistry circuitBreakerRegistry) {
    CircuitBreakerConfig circuitBreakerConfig =
        CircuitBreakerConfig.custom()
            .failureRateThreshold(3)
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(4)
            .minimumNumberOfCalls(3)
            .waitDurationInOpenState(Duration.of(10, ChronoUnit.SECONDS))
            .automaticTransitionFromOpenToHalfOpenEnabled(true)
            .ignoreExceptions(UnhandledException.class)
            .recordExceptions(RestClientException.class, HandledExceptions.class)
            .build();
    return circuitBreakerRegistry.circuitBreaker(
        AirportInfoConstant.CIRCUIT_BREAKER_NAME, circuitBreakerConfig);
  }

  @Bean(name = AirportInfoConstant.AVIATION_CLIENT_IMPL)
  public Client aviationClient(
      @Autowired @Qualifier(AirportInfoConstant.AVIATION_REST_CLIENT) RestClient restClient) {
    AviationClient aviationClient = new AviationClient(restClient, objectMapper());
    ClientRegistry.registerClient(aviationClient.getName(), aviationClient);
    return ClientRegistry.getClient(aviationClient.getName());
  }

  @Bean(name = AirportInfoConstant.RATE_LIMITER_BEAN)
  public RateLimiter rateLimiter(RateLimiterRegistry rateLimiterRegistry) {
    RateLimiterConfig config =
        RateLimiterConfig.custom()
            .limitForPeriod(2) // 2 requests allowed in period (6 second)
            .limitRefreshPeriod(Duration.of(6, ChronoUnit.SECONDS))
            .timeoutDuration(Duration.of(500, ChronoUnit.MILLIS))
            .build();
    RateLimiter rateLimiter =
        rateLimiterRegistry.rateLimiter(AirportInfoConstant.RATE_LIMITER_NAME, config);
    return rateLimiter;
  }
}
