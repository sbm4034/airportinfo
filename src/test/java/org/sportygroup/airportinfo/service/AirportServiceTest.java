package org.sportygroup.airportinfo.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.sportygroup.airportinfo.client.Client;
import org.sportygroup.airportinfo.client.ClientRequest;
import org.sportygroup.airportinfo.client.ClientResponse;
import org.sportygroup.airportinfo.exceptions.CircuitBreakerException;
import org.sportygroup.airportinfo.exceptions.HandledExceptions;
import org.sportygroup.airportinfo.exceptions.RateLimiterException;
import org.sportygroup.airportinfo.utils.AirportRecordToAirportModelMapperImpl;
import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryTemplate;

class AirportServiceTest {

  private AirportRecordToAirportModelMapperImpl mapper;
  private RetryTemplate retryTemplate;
  private CircuitBreaker circuitBreaker;
  private RateLimiter rateLimiter;
  private AirportService airportService;
  private Client<ClientRequest, ClientResponse> client;

  @BeforeEach
  void setUp() {
    mapper = mock(AirportRecordToAirportModelMapperImpl.class);
    retryTemplate = mock(RetryTemplate.class);
    circuitBreaker = mock(CircuitBreaker.class);
    rateLimiter = mock(RateLimiter.class);
    airportService = new AirportService(mapper, retryTemplate, circuitBreaker, rateLimiter);
    client = mock(Client.class);
    // You may need to mock ClientRegistry.getClient if used directly
  }

  @Test
  public void testCircuitBreakerClosed() throws Exception {
    when(rateLimiter.acquirePermission()).thenReturn(true);
    //    when(circuitBreaker.getState()).thenReturn(CircuitBreaker.State.CLOSED);
    ClientResponse response = mock(ClientResponse.class);
    when(response.getAirports()).thenReturn(Collections.emptyMap());
    // Mock retryTemplate to call supplier directly
    when(retryTemplate.execute(any())).thenReturn(response);

    // Mock AirportService.invokeClient to return the mocked response
    AirportService spyService = Mockito.spy(airportService);
    //    Mockito.doReturn(response).when(spyService).invokeClient(any());

    // Call airportService.getAirportQueryResponse and assert result
    var result = spyService.getAirportQueryResponse(List.of("ABC"));
    assertNotNull(result);
    assertTrue(result.getAirports().isEmpty());
  }

  @Test
  public void testCircuitBreakerOpen() throws Exception {
    when(rateLimiter.acquirePermission()).thenReturn(true);
    when(circuitBreaker.getState()).thenReturn(CircuitBreaker.State.OPEN);

    // Create the proper exception chain
    CircuitBreakerException circuitBreakerException = new CircuitBreakerException("State: OPEN");
    RetryException retryException = new RetryException("Retry failed", circuitBreakerException);

    when(retryTemplate.execute(any())).thenThrow(retryException);

    assertThrows(
        HandledExceptions.class, () -> airportService.getAirportQueryResponse(List.of("ABC")));
  }

  @Test
  public void testRetryExhaustion() throws Exception {
    when(rateLimiter.acquirePermission()).thenReturn(true);

    // Create the proper exception chain
    RetryException retryException = new RetryException("Retry failed");

    when(retryTemplate.execute(any())).thenThrow(retryException);

    assertThrows(
        HandledExceptions.class, () -> airportService.getAirportQueryResponse(List.of("ABC")));
  }

  @Test
  public void testRateLimiterExceeded() {
    when(rateLimiter.acquirePermission()).thenReturn(false);
    RateLimiterConfig rateLimiterConfig = mock(RateLimiterConfig.class);
    when(rateLimiter.getRateLimiterConfig()).thenReturn(rateLimiterConfig);
    when(rateLimiterConfig.getLimitForPeriod()).thenReturn(1);
    when(rateLimiterConfig.getLimitRefreshPeriod()).thenReturn(Duration.of(1, ChronoUnit.SECONDS));
    assertThrows(
        RateLimiterException.class, () -> airportService.getAirportQueryResponse(List.of("ABC")));
  }

  @Test
  public void testClientThrowsUnexpectedException() throws Exception {
    when(rateLimiter.acquirePermission()).thenReturn(true);
    when(retryTemplate.execute(any())).thenThrow(new RetryException("Unexpected"));
    assertThrows(
        HandledExceptions.class, () -> airportService.getAirportQueryResponse(List.of("ABC")));
  }
}
