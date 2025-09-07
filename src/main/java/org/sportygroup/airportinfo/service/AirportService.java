package org.sportygroup.airportinfo.service;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.sportygroup.airportinfo.client.Client;
import org.sportygroup.airportinfo.client.ClientRegistry;
import org.sportygroup.airportinfo.client.ClientRequest;
import org.sportygroup.airportinfo.client.ClientResponse;
import org.sportygroup.airportinfo.client.aviation.records.Airport;
import org.sportygroup.airportinfo.constants.AirportInfoConstant;
import org.sportygroup.airportinfo.exceptions.CircuitBreakerException;
import org.sportygroup.airportinfo.exceptions.HandledExceptions;
import org.sportygroup.airportinfo.exceptions.RateLimiterException;
import org.sportygroup.airportinfo.model.airportquery.response.AirportQueryResponse;
import org.sportygroup.airportinfo.utils.AirportRecordToAirportModelMapperImpl;
import org.sportygroup.airportinfo.utils.HeaderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AirportService {

  // This is a known issue where mapstruct and lombok do not work well together for static mapping
  // https://stackoverflow.com/questions/47676369/mapstruct-lombok-together-not-compiling-unknown-property-in-result-type
  private AirportRecordToAirportModelMapperImpl airportRecordToAirportModelMapper;

  private RetryTemplate retryTemplate;

  private CircuitBreaker circuitBreaker;

  private RateLimiter rateLimiter;

  public AirportService(
      @Autowired AirportRecordToAirportModelMapperImpl airportRecordToAirportModelMapper,
      @Autowired RetryTemplate retryTemplate,
      @Autowired CircuitBreaker circuitBreaker,
      @Autowired RateLimiter rateLimiter) {
    this.airportRecordToAirportModelMapper = airportRecordToAirportModelMapper;
    this.retryTemplate = retryTemplate;
    this.circuitBreaker = circuitBreaker;
    this.rateLimiter = rateLimiter;
  }

  public ClientResponse invokeClient(ClientRequest clientRequest) {
    // Invokes the client based on the client name registered in ClientRegistry (can be passed as
    // param to service in for other clients as well)
    Client<ClientRequest, ClientResponse> client =
        ClientRegistry.getClient(AirportInfoConstant.AVIATION_CLIENT_NAME);
    return client.getAirportsByICAOCode(clientRequest);
  }

  public AirportQueryResponse getAirportQueryResponse(List<String> icaoCodes)
      throws HandledExceptions {
    if (rateLimiter.acquirePermission()) {
      log.info("Fetching airport details for ICAO codes: {} -- STARTS", icaoCodes);
      // Creates the client request model for different clients with potentially same request model
      // with icaoCode being consistent
      Map<String, String> headerMap = HeaderUtils.generateHeaders();
      ClientRequest requestForAirportInfo =
          ClientRequest.builder().icaoCodes(icaoCodes).headers(headerMap).build();
      ClientResponse clientResponse = null;

      try {
        Supplier<ClientResponse> circuitBreakerDecoraterSupplier =
            CircuitBreaker.decorateSupplier(
                circuitBreaker, () -> invokeClient(requestForAirportInfo));
        clientResponse =
            retryTemplate.execute(
                () -> {
                  try {
                    return circuitBreakerDecoraterSupplier.get();
                  } catch (CallNotPermittedException ex) {
                    log.warn(
                        "Circuit breaker activated, using fallback. Error: {}", ex.getMessage());
                    return fallbackClientResponse(circuitBreaker.getState().name());
                  }
                });
      } catch (RetryException e) {
        if (e.getCause() instanceof CircuitBreakerException) {
          throw new HandledExceptions(
              "Circuit breaker activated with state : " + circuitBreaker.getState());
        }
        throw new HandledExceptions("Exceeded max retries to fetch data from client", e);
      }

      // Maps the client response to service response model for different clients with potentially
      // different response models
      Map<String, List<Airport>> airportMapFromClient = clientResponse.getAirports();
      Map<String, List<org.sportygroup.airportinfo.model.airportquery.response.airport.Airport>>
          airportResponseMap = new HashMap<>(airportMapFromClient.size());
      airportMapFromClient.forEach(
          (airportCode, airportList) ->
              airportResponseMap.put(
                  airportCode, AirportRecordToAirportModelMapperImpl.map(airportList)));
      if (!airportResponseMap.isEmpty()) {
        log.info("Fetching airport details for ICAO codes: {} -- ENDS", icaoCodes);
        return AirportQueryResponse.builder().airports(airportResponseMap).build();
      }
      log.info("Fetching airport details for ICAO codes: {} -- ENDS", icaoCodes);
      return AirportQueryResponse.builder().airports(Collections.EMPTY_MAP).build();
    } else {
      throw new RateLimiterException(
          "Rate limit exceeded before attempting service call with limit "
              + rateLimiter.getRateLimiterConfig().getLimitForPeriod()
              + " per duration of"
              + rateLimiter.getRateLimiterConfig().getLimitRefreshPeriod());
    }
  }

  private ClientResponse fallbackClientResponse(String state) throws CircuitBreakerException {
    throw new CircuitBreakerException("State: " + state);
  }
}
