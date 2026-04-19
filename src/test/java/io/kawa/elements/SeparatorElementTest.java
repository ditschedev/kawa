package io.kawa.elements;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.kawa.layout.LayoutContext;
import org.junit.jupiter.api.Test;

class SeparatorElementTest {

  private static final LayoutContext CTX = new LayoutContext(0, 0, 500, Float.MAX_VALUE);

  @Test
  void measure_defaultIsMarginTopPlusLineWidthPlusMarginBottom() {
    // default: marginTop=4, lineWidth=0.5, marginBottom=4 → 8.5
    assertEquals(8.5f, new SeparatorElement().measure(CTX), 0.01f);
  }

  @Test
  void measure_customValues() {
    SeparatorElement sep = new SeparatorElement().marginTop(6).marginBottom(4).lineWidth(1f);
    assertEquals(11f, sep.measure(CTX), 0.01f);
  }

  @Test
  void measure_symmetricMarginV() {
    SeparatorElement sep = new SeparatorElement().marginV(10).lineWidth(2f);
    assertEquals(22f, sep.measure(CTX), 0.01f);
  }
}
