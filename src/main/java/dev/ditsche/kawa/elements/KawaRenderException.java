package dev.ditsche.kawa.elements;

/**
 * Unchecked exception thrown when an element fails to render.
 *
 * @author Tobias Dittmann
 */
public class KawaRenderException extends RuntimeException {

  public KawaRenderException(String message, Throwable cause) {
    super(message, cause);
  }

  public KawaRenderException(String message) {
    super(message);
  }
}
