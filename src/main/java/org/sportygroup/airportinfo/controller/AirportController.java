package org.sportygroup.airportinfo.controller;

import java.util.List;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.sportygroup.airportinfo.constants.AirportInfoConstant;
import org.sportygroup.airportinfo.exceptions.*;
import org.sportygroup.airportinfo.model.airportquery.response.AirportQueryResponse;
import org.sportygroup.airportinfo.service.AirportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AirportInfoConstant.AIRPORT_PATH)
@Slf4j
public class AirportController {

  public static final String ICAO_CODES_QUERY_PARAM = "icaoCodes";
  private final AirportService airportService;

  public AirportController(@Autowired AirportService airportService) {
    this.airportService = airportService;
  }

  @GetMapping
  public ResponseEntity<AirportQueryResponse> getAirports(
      @RequestParam(value = ICAO_CODES_QUERY_PARAM) @NonNull List<String> icaoCodes) {
    log.info("Received request to fetch airport details for ICAO codes: {}", icaoCodes);
    AirportQueryResponse airportQueryResponse = null;
    try {
      airportQueryResponse = airportService.getAirportQueryResponse(icaoCodes);
    } catch (HandledExceptions e) {
      return getAirportQueryResponseBasedOnResiliencyFailure(icaoCodes, e);
    }
    log.info("Got successful response for ICAO codes: {}", icaoCodes);
    return new ResponseEntity<>(airportQueryResponse, HttpStatus.OK);
  }

  private static ResponseEntity<AirportQueryResponse>
      getAirportQueryResponseBasedOnResiliencyFailure(List<String> icaoCodes, HandledExceptions e) {

    Throwable cause = e.getCause();
    String message = e.getLocalizedMessage();

    if (cause instanceof ClientServerNotFoundError) {
      log.info("Got 404/empty response for ICAO codes: {}", icaoCodes);
      return ResponseEntity.notFound().build();
    }

    HttpStatus status = getHttpStatusForException(e);
    log.info("Error for ICAO codes: {} - {}", icaoCodes, message);

    return ResponseEntity.status(status)
        .body(AirportQueryResponse.builder().errorMessage(message).build());
  }

  private static HttpStatus getHttpStatusForException(Throwable cause) {
    if (cause instanceof CircuitBreakerException) return HttpStatus.SERVICE_UNAVAILABLE;
    if (cause instanceof RateLimiterException) return HttpStatus.TOO_MANY_REQUESTS;
    if (cause instanceof RetryExceededException) return HttpStatus.GATEWAY_TIMEOUT;
    return HttpStatus.INTERNAL_SERVER_ERROR;
  }
}
