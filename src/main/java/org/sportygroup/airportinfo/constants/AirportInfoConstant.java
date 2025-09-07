package org.sportygroup.airportinfo.constants;

public class AirportInfoConstant {
  public static final String AVIATION_REST_CLIENT = "AviationRestClient";
  public static final String BASE_URL = "https://api.aviationapi.com";

  public static final String OBJECT_MAPPER_BEAN = "objectMapperBean";

  public static final String RETRY_TEMPLATE_BEAN = "retryTemplateBean";
  public static final String AVIATION_CLIENT_IMPL = "aviationClientImpl";
  public static final String AVIATION_CLIENT_NAME = "aviationClient";
  public static final String AIRPORT_PATH = "/v1/airports";

  public static final String CIRCUIT_BREAKER_NAME = "airportInfoCircuitBreaker";
  public static final String CIRCUIT_BREAKER_BEAN_AVIATION_CLIENT =
      "circuitBreakerBeanAviationClient";
  public static final String RATE_LIMITER_NAME = "airportInfoRateLimiter";
  public static final String RATE_LIMITER_BEAN = "rateLimiterBean";
  public static final String RETRY_TEMPLATE_NAME = "airportInfoRetryTemplateName";
}
