package org.sportygroup.airportinfo.enums;

import lombok.Getter;

/** Enum representing whether an airport is for public or private use. */
@Getter
public enum USE {
  PU("public"),
  PR("private");

  private final String fullName;

  USE(String fullName) {
    this.fullName = fullName;
  }
}
