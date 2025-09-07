package org.sportygroup.airportinfo.client;

public interface Client<T extends ClientRequest, R extends ClientResponse> {
  R getAirportsByICAOCode(T request);

  String getName();
}
