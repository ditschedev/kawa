package io.kawa.elements;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.kawa.layout.LayoutContext;
import org.junit.jupiter.api.Test;

class TextElementTest {

  private static LayoutContext ctx(float width) {
    return new LayoutContext(0, 0, width, Float.MAX_VALUE);
  }

  @Test
  void measure_wrapsOnNarrowWidth() {
    TextElement el =
        new TextElement("Word one two three four five six seven eight nine ten").fontSize(12);
    float height = el.measure(ctx(100));
    assertTrue(height > 12 * 1.4f, "Should wrap to multiple lines: " + height);
  }

  @Test
  void measure_singleLineOnWideWidth() {
    TextElement el = new TextElement("Hi").fontSize(10);
    float height = el.measure(ctx(500));
    assertTrue(height <= 10 * 1.5f, "Short text on wide context should be ~1 line: " + height);
  }

  @Test
  void measure_increasesWithNarrowerWidth() {
    TextElement el = new TextElement("word ".repeat(20).trim()).fontSize(11);
    float wide = el.measure(ctx(400));
    float narrow = el.measure(ctx(100));
    assertTrue(narrow > wide, "Narrower width must produce more lines");
  }
}
