package org.sportygroup.airportinfo.utils;

import java.util.HashMap;
import java.util.Map;

public class HeaderUtils {

  public static Map<String, String> generateHeaders() {
    Map<String, String> headerMap = new HashMap<>();
    headerMap.put("Accept", "*/*");
    return headerMap;
  }
}
