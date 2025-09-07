package org.sportygroup.airportinfo.exceptions;

import org.jspecify.annotations.Nullable;

public class RateLimiterException extends HandledExceptions {
  public RateLimiterException(String msg) {
    super(msg);
  }

  public RateLimiterException(@Nullable String msg, @Nullable Throwable cause) {
    super(msg, cause);
  }
}
