package dev.ditsche.kawa.elements;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.ditsche.kawa.core.Document;
import dev.ditsche.kawa.core.PageSize;
import dev.ditsche.kawa.layout.LayoutContext;
import java.io.File;
import org.junit.jupiter.api.Test;

class InputElementTest {

  private static final LayoutContext CTX = new LayoutContext(0, 0, 500, Float.MAX_VALUE);

  @Test
  void measure_noLabel_returnsFieldHeightOnly() {
    assertEquals(22f, new InputElement("field").height(22).measure(CTX), 0.01f);
  }

  @Test
  void measure_withLabel_includesLabelHeightAndGap() {
    InputElement el = new InputElement("field").label("Name").labelFontSize(10).height(22);
    float expected = 10 * 1.3f + 4f + 22f; // label line + gap + field
    assertEquals(expected, el.measure(CTX), 0.01f);
  }

  @Test
  void measure_multiline_returnsConfiguredHeight() {
    assertEquals(60f, new InputElement("notes").multiline(60).measure(CTX), 0.01f);
  }

  @Test
  void generatesPdf() throws Exception {
    File out = new File("target/element-input.pdf");
    Document.create(
            doc ->
                doc.page(
                    page ->
                        page.size(PageSize.A4)
                            .margin(50)
                            .content(
                                c -> {
                                  c.add(new InputElement("name").label("Name"));
                                  c.add(new SpacerElement(10));
                                  c.add(new InputElement("notes").label("Notes").multiline(60));
                                })))
        .generatePdf(out);
    assertTrue(out.exists() && out.length() > 0);
  }
}
