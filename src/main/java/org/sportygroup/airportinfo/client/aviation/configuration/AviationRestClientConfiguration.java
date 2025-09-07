package org.sportygroup.airportinfo.client.aviation.configuration;

import org.sportygroup.airportinfo.constants.AirportInfoConstant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AviationRestClientConfiguration {

  @Bean(name = AirportInfoConstant.AVIATION_REST_CLIENT)
  public RestClient getAviationRestClient() {
    return RestClient.builder().baseUrl(AirportInfoConstant.BASE_URL).build();
  }
}
