package dev.ditsche.kawa.elements;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.ditsche.kawa.core.Document;
import dev.ditsche.kawa.core.PageSize;
import dev.ditsche.kawa.layout.LayoutContext;
import java.io.File;
import org.junit.jupiter.api.Test;

class BarcodeElementTest {

  @Test
  void measure_returnsConfiguredHeight() {
    assertEquals(
        50f,
        new BarcodeElement("12345")
            .height(50)
            .measure(new LayoutContext(0, 0, 500, Float.MAX_VALUE)),
        0.01f);
  }

  @Test
  void generatesPdf() throws Exception {
    File out = new File("target/element-barcode.pdf");
    Document.create(
            doc ->
                doc.page(
                    page ->
                        page.size(PageSize.A4)
                            .margin(50)
                            .content(
                                c ->
                                    c.add(
                                        new BarcodeElement("KW-2025-004711")
                                            .width(180)
                                            .height(50)))))
        .generatePdf(out);
    assertTrue(out.exists() && out.length() > 1_000);
  }
}
