package org.sportygroup.airportinfo.client;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ClientRegistry {

  private static final Map<String, Client> clientRegistry = new HashMap<>();

  public static void registerClient(String name, Client client) {
    clientRegistry.put(name, client);
  }

  public static Client getClient(String name) {
    return clientRegistry.get(name);
  }
}
