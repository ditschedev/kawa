package dev.ditsche.kawa.elements;

import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.ditsche.kawa.core.Document;
import dev.ditsche.kawa.core.PageSize;
import dev.ditsche.kawa.layout.LayoutContext;
import java.io.File;
import org.junit.jupiter.api.Test;

class RowElementTest {

  @Test
  void measure_returnsMaxChildHeight() {
    RowElement row =
        new RowElement(
            r -> {
              r.fixedColumn(100, col -> col.add(new SpacerElement(30)));
              r.fixedColumn(100, col -> col.add(new SpacerElement(50)));
            });
    float height = row.measure(new LayoutContext(0, 0, 400, Float.MAX_VALUE));
    assertTrue(height >= 50f, "Row height must be at least the tallest column: " + height);
  }

  @Test
  void generatesPdf() throws Exception {
    File out = new File("target/element-row.pdf");
    Document.create(
            doc ->
                doc.page(
                    page ->
                        page.size(PageSize.A4)
                            .margin(50)
                            .content(
                                c ->
                                    c.item()
                                        .row(
                                            row -> {
                                              row.fixedColumn(80, col -> col.text("Label:").bold());
                                              row.fillColumn(col -> col.text("Value"));
                                            }))))
        .generatePdf(out);
    assertTrue(out.exists() && out.length() > 0);
  }
}
