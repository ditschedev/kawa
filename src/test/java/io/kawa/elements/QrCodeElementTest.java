package io.kawa.elements;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.kawa.core.Document;
import io.kawa.core.PageSize;
import java.io.File;
import org.junit.jupiter.api.Test;

class QrCodeElementTest {

  @Test
  void measure_returnsConfiguredSize() {
    assertEquals(80f, new QrCodeElement("test").size(80).measure(null), 0.01f);
    assertEquals(120f, new QrCodeElement("test").size(120).measure(null), 0.01f);
  }

  @Test
  void generatesPdf() throws Exception {
    File out = new File("target/element-qrcode.pdf");
    Document.create(
            doc ->
                doc.page(
                    page ->
                        page.size(PageSize.A4)
                            .margin(50)
                            .content(
                                c -> c.add(new QrCodeElement("https://example.com").size(100)))))
        .generatePdf(out);
    assertTrue(out.exists() && out.length() > 0);
  }
}
