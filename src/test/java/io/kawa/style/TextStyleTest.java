package io.kawa.style;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.kawa.elements.TextElement;
import io.kawa.layout.LayoutContext;
import org.junit.jupiter.api.Test;

class TextStyleTest {

  @Test
  void of_buildsAllFields() {
    TextStyle ts =
        TextStyle.of(t -> t.bold().italic().color(Colors.BLUE_900).fontSize(10f).lineHeight(1.5f));
    assertEquals(700, ts.getWeight());
    assertEquals(Boolean.TRUE, ts.getItalic());
    assertEquals(Colors.BLUE_900, ts.getColor());
    assertEquals(10f, ts.getFontSize());
    assertEquals(1.5f, ts.getLineHeight());
  }

  @Test
  void applyTo_affectsMeasuredHeight() {
    TextStyle ts = TextStyle.of(t -> t.fontSize(14f));
    TextElement el = new TextElement("Test");
    ts.applyTo(el);

    float height = el.measure(new LayoutContext(0, 0, 500, 1000));
    assertTrue(height >= 14f * 1.4f, "Expected height >= 19.6 for 14pt font, got: " + height);
  }
}
