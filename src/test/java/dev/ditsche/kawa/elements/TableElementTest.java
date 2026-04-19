package dev.ditsche.kawa.elements;

import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.ditsche.kawa.core.Document;
import dev.ditsche.kawa.core.PageSize;
import dev.ditsche.kawa.layout.LayoutContext;
import dev.ditsche.kawa.style.KawaColor;
import java.io.File;
import org.junit.jupiter.api.Test;

class TableElementTest {

  private static TableElement table(int rowCount) {
    return new TableElement(
        t -> {
          t.columns(
              cols -> {
                cols.relative(1);
                cols.relative(1);
              });
          t.header(
              h -> {
                h.cell("A");
                h.cell("B");
              });
          for (int i = 0; i < rowCount; i++) {
            t.row(
                r -> {
                  r.cell("x");
                  r.cell("y");
                });
          }
        });
  }

  @Test
  void measure_multipleRows_greaterThanSingleRow() {
    LayoutContext ctx = new LayoutContext(0, 0, 400, Float.MAX_VALUE);

    TableElement oneRow = table(1);
    TableElement twoRows = table(2);

    assertTrue(
        twoRows.measure(ctx) > oneRow.measure(ctx), "Two rows must measure more than one row");
  }

  @Test
  void generatesPdf() throws Exception {
    File out = new File("target/element-table.pdf");
    Document.create(
            doc ->
                doc.page(
                    page ->
                        page.size(PageSize.A4)
                            .margin(50)
                            .content(
                                c ->
                                    c.item()
                                        .table(
                                            table -> {
                                              table.columns(
                                                  cols -> {
                                                    cols.relative(3);
                                                    cols.relative(1);
                                                    cols.fixed(80);
                                                  });
                                              table.header(
                                                  h -> {
                                                    h.cell("Description").bold();
                                                    h.cell("Qty").bold().centerAlign();
                                                    h.cell("Price").bold().rightAlign();
                                                  });
                                              table.row(
                                                  r -> {
                                                    r.cell("Item A");
                                                    r.cell("2").centerAlign();
                                                    r.cell("€ 9.99").rightAlign();
                                                  });
                                              table.row(
                                                  r -> {
                                                    r.cell("Item B");
                                                    r.cell("1").centerAlign();
                                                    r.cell("€ 4.99").rightAlign();
                                                  });
                                            })
                                        .cellPadding(6)
                                        .alternateRowColor(KawaColor.rgb(248, 248, 250))
                                        .borderColor(KawaColor.rgb(210, 210, 210)))))
        .generatePdf(out);
    assertTrue(out.exists() && out.length() > 0);
  }
}
