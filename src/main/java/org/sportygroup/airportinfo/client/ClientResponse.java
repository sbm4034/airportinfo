package org.sportygroup.airportinfo.client;

import java.util.List;
import java.util.Map;
import lombok.Data;
import org.sportygroup.airportinfo.client.aviation.records.Airport;
import org.springframework.http.HttpStatus;

@Data
public class ClientResponse {

  private Map<String, String> headers;
  private Map<String, List<Airport>> airports;
  private HttpStatus httpStatus;
}
