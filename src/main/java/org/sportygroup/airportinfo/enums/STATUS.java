package org.sportygroup.airportinfo.enums;

import lombok.Getter;

/** Enum representing the status of an airport. */
@Getter
public enum STATUS {
  O("Open"),
  C("Closed");

  private final String fullName;

  STATUS(String fullName) {
    this.fullName = fullName;
  }
}
