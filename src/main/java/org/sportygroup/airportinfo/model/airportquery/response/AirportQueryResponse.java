package org.sportygroup.airportinfo.model.airportquery.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import org.sportygroup.airportinfo.model.airportquery.response.airport.Airport;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AirportQueryResponse {
  Map<String, List<Airport>> airports;
  String errorMessage;
}
