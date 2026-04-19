package io.kawa.elements;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.kawa.core.Document;
import io.kawa.core.PageSize;
import io.kawa.layout.LayoutContext;
import java.io.File;
import org.junit.jupiter.api.Test;

class HyperlinkElementTest {

  @Test
  void measure_shortTextIsApproximatelyOneLine() {
    HyperlinkElement el = new HyperlinkElement("https://example.com", "example.com").fontSize(11);
    float height = el.measure(new LayoutContext(0, 0, 500, Float.MAX_VALUE));
    assertTrue(height > 0f && height < 30f, "Short link text should be ~1 line: " + height);
  }

  @Test
  void generatesPdf() throws Exception {
    File out = new File("target/element-hyperlink.pdf");
    Document.create(
            doc ->
                doc.page(
                    page ->
                        page.size(PageSize.A4)
                            .margin(50)
                            .content(
                                c ->
                                    c.add(
                                        new HyperlinkElement(
                                            "https://example.com", "example.com")))))
        .generatePdf(out);
    assertTrue(out.exists() && out.length() > 0);
  }
}
