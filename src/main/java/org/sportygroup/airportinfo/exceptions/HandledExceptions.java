package org.sportygroup.airportinfo.exceptions;

import java.io.IOException;
import org.jspecify.annotations.Nullable;

public class HandledExceptions extends IOException {
  public HandledExceptions(String msg) {
    super(msg);
  }

  public HandledExceptions(@Nullable String msg, @Nullable Throwable cause) {
    super(msg, cause);
  }
}
