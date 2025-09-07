package org.sportygroup.airportinfo.client;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
public class ClientRequest {
  private Map<String, String> headers;
  private List<String> icaoCodes;

  @Builder
  public ClientRequest(Map<String, String> headers, List<String> icaoCodes) {
    this.headers = headers;
    this.icaoCodes = icaoCodes;
  }
}
