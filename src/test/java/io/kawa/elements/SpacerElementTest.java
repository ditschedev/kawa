package io.kawa.elements;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.kawa.layout.LayoutContext;
import org.junit.jupiter.api.Test;

class SpacerElementTest {

  private static final LayoutContext CTX = new LayoutContext(0, 0, 500, Float.MAX_VALUE);

  @Test
  void measure_returnsExactHeight() {
    assertEquals(20f, new SpacerElement(20).measure(CTX), 0.01f);
    assertEquals(0.5f, new SpacerElement(0.5f).measure(CTX), 0.01f);
  }

  @Test
  void measure_negativeClampsToZero() {
    assertEquals(0f, new SpacerElement(-5).measure(CTX), 0.01f);
  }
}
