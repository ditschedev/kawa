package dev.ditsche.kawa.elements;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.ditsche.kawa.layout.LayoutContext;
import org.junit.jupiter.api.Test;

class StackElementTest {

  private static final LayoutContext CTX = new LayoutContext(0, 0, 500, Float.MAX_VALUE);

  @Test
  void measure_returnsMaxLayerHeight() {
    StackElement stack =
        new StackElement()
            .layer(new SpacerElement(30))
            .layer(new SpacerElement(50))
            .layer(new SpacerElement(20));
    assertEquals(50f, stack.measure(CTX), 0.01f);
  }

  @Test
  void measure_singleLayerReturnsThatLayerHeight() {
    StackElement stack = new StackElement().layer(new SpacerElement(42));
    assertEquals(42f, stack.measure(CTX), 0.01f);
  }
}
