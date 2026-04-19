package dev.ditsche.kawa.elements;

import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.ditsche.kawa.core.Document;
import dev.ditsche.kawa.core.PageSize;
import dev.ditsche.kawa.layout.LayoutContext;
import java.io.File;
import org.junit.jupiter.api.Test;

class GridElementTest {

  @Test
  void measure_multipleRows_sumHeights() {
    GridElement grid =
        new GridElement(
                g -> {
                  g.columns(
                      cols -> {
                        cols.relative(1);
                        cols.relative(1);
                      });
                  g.row(
                      r -> {
                        r.cell(col -> col.text("A").fontSize(12));
                        r.cell(col -> col.text("B").fontSize(12));
                      });
                  g.row(
                      r -> {
                        r.cell(col -> col.text("C").fontSize(12));
                        r.cell(col -> col.text("D").fontSize(12));
                      });
                })
            .rowGap(8);

    float height = grid.measure(new LayoutContext(0, 0, 400, Float.MAX_VALUE));
    assertTrue(height > 20f, "Two rows with gap must measure more than a single row: " + height);
  }

  @Test
  void generatesPdf() throws Exception {
    File out = new File("target/element-grid.pdf");
    Document.create(
            doc ->
                doc.page(
                    page ->
                        page.size(PageSize.A4)
                            .margin(50)
                            .content(
                                c ->
                                    c.add(
                                        new GridElement(
                                            grid -> {
                                              grid.columns(
                                                  cols -> {
                                                    cols.fixed(80);
                                                    cols.relative(1);
                                                  });
                                              grid.rowGap(6);
                                              for (String[] kv :
                                                  new String[][] {
                                                    {"Name:", "Max"}, {"Email:", "max@example.com"}
                                                  }) {
                                                String k = kv[0], v = kv[1];
                                                grid.row(
                                                    r -> {
                                                      r.cell(
                                                          col -> col.text(k).bold().fontSize(10));
                                                      r.cell(col -> col.text(v).fontSize(10));
                                                    });
                                              }
                                            })))))
        .generatePdf(out);
    assertTrue(out.exists() && out.length() > 0);
  }
}
