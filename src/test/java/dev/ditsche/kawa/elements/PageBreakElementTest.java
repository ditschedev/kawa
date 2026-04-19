package dev.ditsche.kawa.elements;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.ditsche.kawa.core.Document;
import dev.ditsche.kawa.core.PageSize;
import dev.ditsche.kawa.layout.LayoutContext;
import java.io.File;
import org.apache.pdfbox.Loader;
import org.junit.jupiter.api.Test;

class PageBreakElementTest {

  @Test
  void measure_isZero() {
    float height = new PageBreakElement().measure(new LayoutContext(0, 0, 500, Float.MAX_VALUE));
    assertEquals(
        0f, height, "PageBreak has zero height; the paginator handles forced page transitions");
  }

  @Test
  void generatesPdf_withPageBreak_producesTwoPages() throws Exception {
    File out = new File("target/element-page-break.pdf");
    Document.create(
            doc ->
                doc.page(
                    page ->
                        page.size(PageSize.A4)
                            .margin(50)
                            .content(
                                c -> {
                                  c.add(new TextElement("Page one content"));
                                  c.add(new PageBreakElement());
                                  c.add(new TextElement("Page two content"));
                                })))
        .generatePdf(out);
    try (var pdDoc = Loader.loadPDF(out)) {
      assertEquals(
          2,
          pdDoc.getNumberOfPages(),
          "Document with one PageBreakElement should produce exactly 2 pages");
    }
  }
}
