package dev.ditsche.kawa.elements;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.ditsche.kawa.core.Document;
import dev.ditsche.kawa.core.PageSize;
import dev.ditsche.kawa.layout.LayoutContext;
import java.io.File;
import org.junit.jupiter.api.Test;

class CheckboxElementTest {

  private static final LayoutContext CTX = new LayoutContext(0, 0, 500, Float.MAX_VALUE);

  @Test
  void measure_noLabel_returnsSize() {
    assertEquals(14f, new CheckboxElement("opt").measure(CTX), 0.01f);
  }

  @Test
  void measure_withLabel_returnsMaxOfSizeAndLabelHeight() {
    // size=14, labelFontSize=11 → label line height = 11 * 1.3 = 14.3 → max wins
    float expected = Math.max(14f, 11f * 1.3f);
    assertEquals(expected, new CheckboxElement("opt").label("Accept terms").measure(CTX), 0.01f);
  }

  @Test
  void measure_largeSize_returnsSize() {
    assertEquals(30f, new CheckboxElement("opt").size(30).measure(CTX), 0.01f);
  }

  @Test
  void generatesPdf() throws Exception {
    File out = new File("target/element-checkbox.pdf");
    Document.create(
            doc ->
                doc.page(
                    page ->
                        page.size(PageSize.A4)
                            .margin(50)
                            .content(
                                c -> {
                                  c.add(new CheckboxElement("agree").label("I agree to the terms"));
                                  c.add(new SpacerElement(8));
                                  c.add(
                                      new CheckboxElement("newsletter")
                                          .label("Subscribe to newsletter")
                                          .checked());
                                  c.add(new SpacerElement(8));
                                  c.add(new CheckboxElement("noLabel"));
                                })))
        .generatePdf(out);
    assertTrue(out.exists() && out.length() > 0);
  }
}
