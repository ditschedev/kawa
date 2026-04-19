package dev.ditsche.kawa.elements;

import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.ditsche.kawa.core.Document;
import dev.ditsche.kawa.core.PageSize;
import java.io.File;
import org.junit.jupiter.api.Test;

class ImageElementTest {

  @Test
  void generatesPdf_fromSvgResource() throws Exception {
    File out = new File("target/element-image-svg.pdf");
    Document.create(
            doc ->
                doc.page(
                    page ->
                        page.size(PageSize.A4)
                            .margin(50)
                            .content(
                                c ->
                                    c.add(
                                        ImageElement.ofResource("/sunclub-logo.svg").width(180)))))
        .generatePdf(out);
    assertTrue(out.exists() && out.length() > 1_000);
  }
}
