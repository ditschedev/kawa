package io.kawa.elements;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.kawa.layout.LayoutContext;
import org.junit.jupiter.api.Test;

class RichTextElementTest {

  private static LayoutContext ctx(float width) {
    return new LayoutContext(0, 0, width, Float.MAX_VALUE);
  }

  @Test
  void measure_emptyReturnsZero() {
    assertEquals(0f, new RichTextElement().measure(ctx(500)), 0.01f);
  }

  @Test
  void measure_singleSpanReturnsReasonableHeight() {
    RichTextElement el = new RichTextElement().span("Hello world", s -> s.fontSize(12));
    float height = el.measure(ctx(500));
    assertTrue(height > 0f && height < 100f, "Single short span: " + height);
  }

  @Test
  void measure_narrowContextProducesGreaterHeightThanWide() {
    RichTextElement el =
        new RichTextElement().span("Word one two three four five six seven eight nine ten");
    float wide = el.measure(ctx(500));
    float narrow = el.measure(ctx(80));
    assertTrue(
        narrow > wide,
        "Narrow context must produce more lines: narrow=" + narrow + " wide=" + wide);
  }
}
