package org.sportygroup.airportinfo.exceptions;

import org.jspecify.annotations.Nullable;
import org.springframework.core.NestedRuntimeException;

public class UnhandledException extends NestedRuntimeException {

  public UnhandledException(@Nullable String msg) {
    super(msg);
  }

  public UnhandledException(@Nullable String msg, @Nullable Throwable cause) {
    super(msg, cause);
  }
}
