package org.sportygroup.airportinfo.enums;

import lombok.Getter;

/** Enum representing ownership types of airports. */
@Getter
public enum OWNERSHIP {
  PU("public"),
  PR("private");

  private final String fullName;

  OWNERSHIP(String fullName) {
    this.fullName = fullName;
  }
}
