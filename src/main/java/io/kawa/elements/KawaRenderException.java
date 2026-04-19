package io.kawa.elements;

/** Unchecked exception thrown when an element fails to render. */
public class KawaRenderException extends RuntimeException {

  public KawaRenderException(String message, Throwable cause) {
    super(message, cause);
  }

  public KawaRenderException(String message) {
    super(message);
  }
}
