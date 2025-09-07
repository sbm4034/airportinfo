package org.sportygroup.airportinfo.client.aviation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.sportygroup.airportinfo.client.Client;
import org.sportygroup.airportinfo.client.ClientRequest;
import org.sportygroup.airportinfo.client.ClientResponse;
import org.sportygroup.airportinfo.client.aviation.records.Airport;
import org.sportygroup.airportinfo.constants.AirportInfoConstant;
import org.sportygroup.airportinfo.constants.LogConstants;
import org.sportygroup.airportinfo.exceptions.ClientServerNotFoundError;
import org.sportygroup.airportinfo.exceptions.HandledExceptions;
import org.sportygroup.airportinfo.exceptions.UnhandledException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
@Slf4j
public class AviationClient implements Client<ClientRequest, ClientResponse> {

  public static final String QUERY_PARAM = "apt";
  public static final String AIRPORTS_PATH = "v1/airports";
  public static final StringBuilder NO_DATA_FOUND_FOR_ICAO_CODE =
      new StringBuilder("No data found for ICAO code: ");
  private final RestClient restClient;

  private final ObjectMapper objectMapper;

  public AviationClient(
      @Autowired @Qualifier(AirportInfoConstant.AVIATION_REST_CLIENT) RestClient restClient,
      @Autowired ObjectMapper objectMapper) {
    this.restClient = restClient;
    this.objectMapper = objectMapper;
  }

  /**
   * Fetches a list of airports based on the provided ICAO code.
   *
   * @param airportQueryRequest The request containing the ICAO code.
   * @return A list of airports matching the ICAO code.
   * @throws ClientServerNotFoundError For server-side errors (5xx) or 4xx(other than 404) during
   *     the REST call.
   * @throws UnhandledException For non-retryable exceptions during the REST call.
   * @throws RestClientException For retryable exceptions during the REST call.
   */
  public ClientResponse getAirportsByICAOCode(ClientRequest airportQueryRequest) {
    try {
      return restClient
          .get()
          .uri(
              uriBuilder ->
                  uriBuilder
                      .path(AIRPORTS_PATH)
                      .queryParam(QUERY_PARAM, String.join(",", airportQueryRequest.getIcaoCodes()))
                      .build())
          .exchange(
              ((clientRequest, clientResponse) ->
                  getClientResponse(airportQueryRequest, clientResponse)));

    } catch (RestClientException e) {
      // propagates to retry template
      if (e.getCause() instanceof IOException) {
        log.error(LogConstants.IO_EXCEPTION_IN_CLIENT, e.getClass(), e.getMessage());
        throw e;
      }
      // handles non-retryable exceptions
      log.error(LogConstants.REST_CLIENT_EXCEPTION, e.getClass(), e.getMessage());
      throw new UnhandledException(e.getMessage(), e.getCause());
    }
  }

  private ClientResponse getClientResponse(
      ClientRequest airportQueryRequest,
      RestClient.RequestHeadersSpec.ConvertibleClientHttpResponse clientResponse)
      throws IOException {
    return switch (clientResponse.getStatusCode()) {
      case HttpStatus.OK -> handleOkResponse(airportQueryRequest, clientResponse);
      case HttpStatus.NOT_FOUND -> handleNotFoundResponse(airportQueryRequest, clientResponse);
      default -> handleUnexpectedResponse(airportQueryRequest, clientResponse);
    };
  }

  private ClientResponse handleUnexpectedResponse(
      ClientRequest airportQueryRequest,
      RestClient.RequestHeadersSpec.ConvertibleClientHttpResponse clientResponse)
      throws IOException {
    // check for 500 status code which would be retried
    log.info(
        LogConstants.UNEXPECTED_STATUS_CODE_EXCEPTION_IN_CLIENT,
        AviationClient.class,
        clientResponse.getStatusCode());
    throw new HandledExceptions("Unexpected response status: " + clientResponse.getStatusCode());
  }

  private ClientResponse handleNotFoundResponse(
      ClientRequest airportQueryRequest,
      RestClient.RequestHeadersSpec.ConvertibleClientHttpResponse clientResponse)
      throws ClientServerNotFoundError {
    String messageForNotFound =
        NO_DATA_FOUND_FOR_ICAO_CODE.append(airportQueryRequest.getIcaoCodes()).toString();
    log.info(LogConstants.NOT_FOUND_EXCEPTION_IN_CLIENT, AviationClient.class, messageForNotFound);
    throw new ClientServerNotFoundError(messageForNotFound);
  }

  private ClientResponse handleOkResponse(
      ClientRequest airportQueryRequest,
      RestClient.RequestHeadersSpec.ConvertibleClientHttpResponse clientResponse)
      throws IOException {
    log.info(
        "Received 200 OK from Aviation API for ICAO codes: {}", airportQueryRequest.getIcaoCodes());
    Map<String, List<Airport>> airportMap =
        objectMapper.readValue(
            clientResponse.getBody(), new TypeReference<Map<String, List<Airport>>>() {});
    log.debug("Received airport map: {}", airportMap);
    ClientResponse response = new ClientResponse();
    // helps in managing multi value map of headers to single value
    response.setHeaders(clientResponse.getHeaders().toSingleValueMap());

    // Handles the case for 200OK but no actual response from client
    if (CollectionUtils.isEmpty(airportMap)) {
      log.info(
          "Received 200 OK from Aviation API for ICAO codes: {}. But it was empty for all of the icaoCodes",
          airportQueryRequest.getIcaoCodes());
      // helps in managing multi value map of headers to single value
      String messageForNotFound =
          NO_DATA_FOUND_FOR_ICAO_CODE.append(airportQueryRequest.getIcaoCodes()).toString();
      log.info(
          LogConstants.NOT_FOUND_EXCEPTION_IN_CLIENT, AviationClient.class, messageForNotFound);
      throw new ClientServerNotFoundError(messageForNotFound);
    } else {
      response.setAirports(airportMap);
      return response;
    }
  }

  @Override
  public String getName() {
    return AirportInfoConstant.AVIATION_CLIENT_NAME;
  }
}
