package org.sportygroup.airportinfo; // Replace with your actual package

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.test.LocalServerPort;
import org.springframework.boot.web.server.test.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.profiles.active=test"})
@DisplayName("Airport Info Integration Tests")
class AirportInfoIT {

  @Autowired private TestRestTemplate restTemplate;

  @LocalServerPort private int port;

  @Test
  public void shouldReturnAirportInfoForValidIcaoCodesViaHttp() throws Exception {
    // Given
    String baseUrl = "http://localhost:" + port;
    String endpoint = "/v1/airports?icaoCodes=KJFK"; // Using a well-known airport
    String fullUrl = baseUrl + endpoint;

    // When
    ResponseEntity<String> response = restTemplate.getForEntity(fullUrl, String.class);

    // Then
    assertThat(response.getStatusCode())
        .as("HTTP response should be successful")
        .isEqualTo(HttpStatus.OK);

    assertThat(response.getBody()).as("Response body should not be empty").isNotNull().isNotEmpty();

    // Verify that response contains airport-related data
    // Adjust these assertions based on your actual response format
    String responseBody = response.getBody();
    assertThat(responseBody.toLowerCase())
        .as("Response should contain airport-related information")
        .containsAnyOf("kjfk", "kennedy", "airport", "jfk", "data");
  }

  @Test
  public void shouldHandleInvalidIcaoCodesGracefully() {
    // Given
    String baseUrl = "http://localhost:" + port;
    String endpoint = "/v1/airports?icaoCodes=INVALID999"; // Obviously invalid ICAO code
    String fullUrl = baseUrl + endpoint;

    // When
    ResponseEntity<String> response = restTemplate.getForEntity(fullUrl, String.class);

    // Then
    assertThat(response.getStatusCode())
        .as("Should handle invalid ICAO codes gracefully")
        .isEqualTo(HttpStatus.OK);

    assertThat(response.getBody())
        .as("Response should not be null even for invalid codes")
        .isNotNull();
  }

  @Test
  public void shouldReturnBadRequestWhenNoIcaoCodesProvided() {
    // Given
    String baseUrl = "http://localhost:" + port;
    String endpoint = "/v1/airports"; // No query parameters
    String fullUrl = baseUrl + endpoint;

    // When
    ResponseEntity<String> response = restTemplate.getForEntity(fullUrl, String.class);

    // Then
    assertThat(response.getStatusCode())
        .as("Should return bad request when no ICAO codes provided")
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  public void shouldHaveHealthEndpointAccessible() {
    // Given
    String baseUrl = "http://localhost:" + port;
    String endpoint = "/actuator/health";
    String fullUrl = baseUrl + endpoint;

    // When
    ResponseEntity<String> response = restTemplate.getForEntity(fullUrl, String.class);

    // Then
    assertThat(response.getStatusCode())
        .as("Health endpoint should be accessible")
        .isEqualTo(HttpStatus.OK);

    assertThat(response.getBody())
        .as("Health response should contain status information")
        .isNotNull()
        .contains("status");
  }
}
