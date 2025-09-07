package org.sportygroup.airportinfo.exceptions;

import lombok.Getter;

@Getter
public class CircuitBreakerException extends HandledExceptions {
  public CircuitBreakerException(String message) {
    super(message);
  }
}
