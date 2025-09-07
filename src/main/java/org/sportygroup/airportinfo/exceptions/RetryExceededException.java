package org.sportygroup.airportinfo.exceptions;

import org.jspecify.annotations.Nullable;

public class RetryExceededException extends HandledExceptions {

  public RetryExceededException(@Nullable String msg, @Nullable Throwable cause) {
    super(msg, cause);
  }
}
