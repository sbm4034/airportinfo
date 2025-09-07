package org.sportygroup.airportinfo.utils;

import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HeaderUtilsTest {

  @Test
  void testGenerateHeaders() {
    Map<String, String> headers = HeaderUtils.generateHeaders();
    Assertions.assertNotNull(headers);
    Assertions.assertEquals(1, headers.size());
    Assertions.assertEquals("*/*", headers.get("Accept"));
  }
}
