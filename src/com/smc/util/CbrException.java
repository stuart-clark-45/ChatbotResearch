package com.smc.util;

/**
 * @author Stuart Clark
 */
public class CbrException extends Exception {

  public CbrException() {}

  public CbrException(String message) {
    super(message);
  }

  public CbrException(String message, Throwable cause) {
    super(message, cause);
  }

  public CbrException(Throwable cause) {
    super(cause);
  }

  public CbrException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
