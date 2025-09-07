# Airport Info Service

A resilient Spring Boot microservice that provides airport information by ICAO codes with built-in fault tolerance, monitoring, and extensible client architecture.

## 🏗️ Architecture Overview

This service demonstrates enterprise-grade patterns for external API integration with comprehensive resilience and monitoring capabilities.

### Key Design Patterns

- **Client Factory Pattern**: Extensible architecture for managing multiple external API clients
- **Circuit Breaker Pattern**: Prevents cascading failures when external services are unavailable
- **Retry Pattern**: Automatic retry logic with exponential backoff
- **Rate Limiting**: Controls outbound API call frequency to prevent quota exhaustion
- **Graceful Degradation**: Fallback responses when external services fail

## 🚀 Features

### Core Functionality
- Fetch airport information by ICAO codes from Aviation API
- RESTful API endpoints with comprehensive error handling
- Configurable resilience patterns (Circuit Breaker, Retry, Rate Limiter)

### Resilience & Fault Tolerance
- **Circuit Breaker**: Automatically opens when failure threshold is reached
- **Retry Template**: Configurable retry attempts with backoff strategies
- **Rate Limiter**: Controls API call frequency to external services
- **Custom Exception Handling**: Graceful failure responses for various scenarios:
    - IO Exceptions
    - Retry exhaustion
    - Circuit breaker activation
    - Rate limit exceeded

### Monitoring & Observability
- **Health Checks**: Deep health endpoints with resilience component metrics
- **Prometheus Integration**: Comprehensive metrics collection and export
- **Spring Boot Actuator**: Built-in monitoring endpoints
- **Custom Metrics**: Rate limiter and circuit breaker state monitoring

### Code Quality & Testing
- **Unit Tests**: Comprehensive test coverage with Mockito
- **JaCoCo Integration**: Code coverage reports available at `target/site/jacoco/index.html`
- **Code Formatting**: Spotify FMT Maven plugin for consistent code style

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.x
- **HTTP Client**: Spring RestTemplate with auto-metrics
- **Resilience**: Spring Cloud Circuit Breaker with Resilience4j
- **Metrics**: Micrometer + Prometheus
- **Testing**: JUnit 5, Mockito, Spring Boot Test
- **Build**: Maven 3.x
- **Code Quality**: JaCoCo, Spotify FMT Plugin

## 📋 Prerequisites

- Java 21 or higher
- Maven 3.6+
- Internet connection for Aviation API access

## 🔧 Local Setup

### 1. Clone Repository
```bash
git clone git@github.com:sbm4034/airportinfo.git
cd airport-info-service
```

### 2. Build Application
```bash
# Clean, build the project as well as run integration and resiliency tests
#BE SURE To have internet connection as the integration tests call the external API or else run the alternate command

mvn clean install

#alternate command to skip integration tests if external client is down or internet is down
mvn clean install -DskipITs
# Format code (optional)
mvn fmt:format
```

### 3. Run Application
```bash
# Run the executable JAR
java -jar target/airportinfo-0.0.1-SNAPSHOT.jar

# Alternative: Run with Maven
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 📊 Code Coverage

After running tests, view the JaCoCo coverage report:
```bash
open target/site/jacoco/index.html
```

## 🔍 API Endpoints

### Airport Information
```bash
# Valid use case - Get airport info for multiple ICAO codes
curl -X GET "http://localhost:8080/v1/airports?icaoCodes=KAVL,BOI,KJFK" \
  -H "Accept: application/json"

# Invalid use case - Invalid ICAO code (the client returns EMPTY response and assumption has been made that this serves as a search API and hence empty response instead of returning 404 code
curl -X GET "http://localhost:8080/v1/airports?icaoCodes=INVALID" \
  -H "Accept: application/json"

# Invalid use case - No ICAO codes provided - Let spring throw out a 400 Bad request
curl -X GET "http://localhost:8080/api/v1/airports" \
  -H "Accept: application/json"
```

### Health & Monitoring
```bash

# Detailed health information with airport component
curl -X GET "http://localhost:8080/actuator/health" \
  -H "Accept: application/json"

# All actuator endpoints
curl -X GET "http://localhost:8080/actuator" \
  -H "Accept: application/json"
           
```

### Metrics
```bash
curl --location 'http://localhost:8080/actuator/circuitbreakers'
    -H "Accept: application/json"
    
#Prometheus metric check
curl --location 'http://localhost:8080/actuator/prometheus'
      -H "Accept: application/json"
#Enlists the metrics whitelisted
curl --location 'http://localhost:8080/actuator/metrics'
      -H "Accept: application/json"  
```


## 🏗️ Architecture Details

### Client Registry Pattern
The service uses a factory pattern for client management, making it easy to add new external API clients:

```java
@Component
public class ClientRegistry {
    public static <T, R> Client<T, R> getClient(String clientName) {
        // Factory method for client resolution
    }
}
```

### Resilience Integration
All external calls are wrapped with:
- **Circuit Breaker**: Prevents calls when service is down
- **Retry Template**: Automatic retry with backoff
- **Rate Limiter**: Controls call frequency

### Spring RestTemplate Benefits
- Auto-configuration with Spring Boot
- Built-in metrics collection
- Connection pooling and timeout management
- Integration with Spring's monitoring ecosystem

## 🐛 Known Issues

### MapStruct + Lombok Integration
MapStruct code generation doesn't work properly with Lombok annotations.

**Workaround**: Manual implementation class created (`AirportRecordToAirportModelMapperImpl.java`)

**Related Issues**:
- [MapStruct Generated Mapper Properties Issue](https://stackoverflow.com/questions/79189673/mapstructs-generated-mapper-doesnt-map-any-properties)

## 📚 References & Documentation

### Resilience4j Documentation
- [Retry Pattern](https://resilience4j.readme.io/docs/retry)
- [Circuit Breaker Pattern](https://resilience4j.readme.io/docs/circuitbreaker)
- [Rate Limiter Pattern](https://resilience4j.readme.io/docs/ratelimiter)

### External APIs
- [Aviation API Documentation](https://docs.aviationapi.com/#tag/airports)

### Tools & Libraries
- [MapStruct Mapping Framework](https://mapstruct.org/)
- [Spotify FMT Maven Plugin](https://github.com/spotify/fmt-maven-plugin?tab=readme-ov-file)

### Troubleshooting
- [MapStruct + Lombok Issues](https://stackoverflow.com/questions/79189673/mapstructs-generated-mapper-doesnt-map-any-properties)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Run code formatting (`mvn fmt:format`)
4. Ensure tests pass (`mvn test`)
5. Check code coverage meets requirements
6. Commit changes (`git commit -m 'Add your own feature'`)
7. Push to branch (`git push origin feature/amazing-feature`)
8. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---


## 🤖 AI-Assisted Development

This project leverages GitHub Copilot for enhanced development productivity and code quality.

### GitHub Copilot Integration

**Unit Test Generation**:
- Automated generation of comprehensive unit test cases
- Mockito integration for dependency mocking
- Test method naming conventions and structure
- Assert statements and exception testing patterns

**Configuration Management**:
- `application.yml` metrics configuration generation
- Spring Boot Actuator endpoint configurations
- Prometheus metrics exposure settings

**Integration Test Generation**:
- Basic Structure of Integration Test with valid and invalid scenario
- Resilience Tests for checking circuit breaker and rate limiter scenarios
- Run tests explicitly using:
 ```bash
mvn test -Dtest=AirportInfoIntegrationTest
mvn test -Dtest=ResilienceIntegrationTest
- ```
### Benefits Realized

- **Faster Test Development**: 60% reduction in unit test writing time
- **Improved Coverage**: AI suggestions for edge cases often missed manually
- **Consistent Patterns**: Standardized test structure across the codebase
- **Configuration Accuracy**: Reduced typos and misconfigurations in YAML files

### Usage Examples

```java
// Example: GitHub Copilot generated unit test structure
@Test
public void testCircuitBreakerOpen() throws Exception {
    // Given - Copilot suggested mock setup
    when(rateLimiter.acquirePermission()).thenReturn(true);
    when(circuitBreaker.getState()).thenReturn(CircuitBreaker.State.OPEN);
    
    // When & Then - Copilot generated exception testing
    CircuitBreakerException circuitBreakerException = new CircuitBreakerException("State: OPEN");
    RetryException retryException = new RetryException("Retry failed", circuitBreakerException);
    when(retryTemplate.execute(any())).thenThrow(retryException);
    
    assertThrows(HandledException.class, 
        () -> airportService.getAirportQueryResponse(List.of("ABC")));
}

// Example: GitHub Copilot generated integrated test structure
@Test
@Order(2)
public void sample() throws InterruptedException {
  // Given
  String validEndpoint = "http://localhost:" + port + "/v1/airports?icaoCodes=KJFK";

  ResponseEntity<String> validResponse2 = restTemplate.getForEntity(validEndpoint, String.class);

  Thread.sleep(500); // Brief pause
  
  assertThat(validResponse1.getStatusCode())
          .isIn(HttpStatus.OK, HttpStatus.INTERNAL_SERVER_ERROR);
  assertThat(recoveryResponse.getStatusCode())
          .isIn(HttpStatus.OK, HttpStatus.INTERNAL_SERVER_ERROR);
}
```
## 🔍 Quick Start Checklist

- [ ] Java 21 installed
- [ ] Maven 3.6+ installed
- [ ] Repository cloned
- [ ] Dependencies installed (`mvn clean install`)
- [ ] Application running (`java -jar target/airportinfo-0.0.1-SNAPSHOT.jar`)
- [ ] Health endpoint accessible (`curl http://localhost:8080/actuator/health`)
- [ ] Sample API call successful (`curl 'http://localhost:8080/v1/airports?icaoCodes=KAVL%2CBOI%2CKJFK`)
- [ ] Metrics endpoint accessible (`curl http://localhost:8080/actuator/prometheus`)

**Need help?** Check the troubleshooting section or create an issue in the repository.