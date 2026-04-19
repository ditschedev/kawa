package dev.ditsche.kawa.elements;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.ditsche.kawa.core.Document;
import dev.ditsche.kawa.core.PageSize;
import dev.ditsche.kawa.layout.LayoutContext;
import dev.ditsche.kawa.style.KawaColor;
import java.io.File;
import org.junit.jupiter.api.Test;

class ButtonElementTest {

  private static final LayoutContext CTX = new LayoutContext(0, 0, 500, Float.MAX_VALUE);

  @Test
  void measure_returnsDefaultHeight() {
    assertEquals(24f, new ButtonElement("btn", "Click me").measure(CTX), 0.01f);
  }

  @Test
  void measure_returnsCustomHeight() {
    assertEquals(36f, new ButtonElement("btn", "Submit").height(36).measure(CTX), 0.01f);
  }

  @Test
  void generatesPdf() throws Exception {
    File out = new File("target/element-button.pdf");
    Document.create(
            doc ->
                doc.page(
                    page ->
                        page.size(PageSize.A4)
                            .margin(50)
                            .content(
                                c -> {
                                  c.add(new ButtonElement("submit", "Submit Form"));
                                  c.add(new SpacerElement(10));
                                  c.add(
                                      new ButtonElement("cancel", "Cancel")
                                          .height(28)
                                          .backgroundColor(KawaColor.rgb(220, 38, 38))
                                          .borderColor(KawaColor.rgb(185, 28, 28)));
                                })))
        .generatePdf(out);
    assertTrue(out.exists() && out.length() > 0);
  }
}
