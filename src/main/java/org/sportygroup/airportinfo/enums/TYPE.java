package org.sportygroup.airportinfo.enums;

import lombok.Getter;

/** Enum representing different types of airports. */
@Getter
public enum TYPE {
  AIRPORT("airport"),
  HELIPORT("heliport");

  TYPE(String name) {
    this.name = name;
  }

  private final String name;
}
