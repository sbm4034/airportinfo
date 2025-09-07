package org.sportygroup.airportinfo.controller;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.sportygroup.airportinfo.constants.AirportInfoConstant;
import org.sportygroup.airportinfo.exceptions.ClientServerNotFoundError;
import org.sportygroup.airportinfo.exceptions.HandledExceptions;
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
  private AirportService airportService;

  public AirportController(@Autowired AirportService airportService) {
    this.airportService = airportService;
  }

  @GetMapping
  public ResponseEntity<AirportQueryResponse> getAirports(
      @RequestParam(value = ICAO_CODES_QUERY_PARAM) List<String> icaoCodes) {
    log.info("Received request to fetch airport details for ICAO codes: {}", icaoCodes);
    AirportQueryResponse airportQueryResponse = null;
    try {
      airportQueryResponse = airportService.getAirportQueryResponse(icaoCodes);
    } catch (HandledExceptions e) {
      if (e.getCause() instanceof ClientServerNotFoundError) {
        log.info("Got response 404 or empty response for ICAO codes: {}", icaoCodes);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }
      log.info("Got unexpected error for ICAO codes: {}", icaoCodes);
      return new ResponseEntity<>(
          AirportQueryResponse.builder().errorMessage(e.getLocalizedMessage()).build(),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
    log.info("Got successful response for ICAO codes: {}", icaoCodes);
    return new ResponseEntity<>(airportQueryResponse, HttpStatus.OK);
  }
}
