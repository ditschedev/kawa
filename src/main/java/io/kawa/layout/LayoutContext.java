package io.kawa.layout;

/**
 * Represents the available space for an element during layout. Passed top-down through the element
 * tree.
 */
public record LayoutContext(float x, float y, float width, float height) {

  /** Returns a new context moved down by {@code dy} and reduced in height accordingly. */
  public LayoutContext moveDown(float dy) {
    return new LayoutContext(x, y - dy, width, height - dy);
  }

  /** Returns a new context with horizontal insets applied. */
  public LayoutContext withHorizontalInsets(float left, float right) {
    return new LayoutContext(x + left, y, width - left - right, height);
  }

  /** Returns a new context with a fixed width. */
  public LayoutContext withWidth(float newWidth) {
    return new LayoutContext(x, y, newWidth, height);
  }

  /** Returns a new context offset to the right by {@code dx}. */
  public LayoutContext moveRight(float dx) {
    return new LayoutContext(x + dx, y, width - dx, height);
  }

  @Override
  public String toString() {
    return String.format("LayoutContext{x=%.1f, y=%.1f, w=%.1f, h=%.1f}", x, y, width, height);
  }
}
