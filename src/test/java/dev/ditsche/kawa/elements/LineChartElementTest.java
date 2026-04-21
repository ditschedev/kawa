package dev.ditsche.kawa.elements;

import static org.junit.jupiter.api.Assertions.*;

import dev.ditsche.kawa.core.Document;
import dev.ditsche.kawa.core.PageSize;
import dev.ditsche.kawa.layout.LayoutContext;
import dev.ditsche.kawa.style.Colors;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Test;

class LineChartElementTest {

  // ── measure ──────────────────────────────────────────────────────────────

  @Test
  void measure_returnsConfiguredHeight() {
    LineChartElement chart = new LineChartElement().height(150).series("A", List.of(10, 20, 30));
    assertEquals(150f, chart.measure(new LayoutContext(0, 0, 400, Float.MAX_VALUE)), 0.01f);
  }

  @Test
  void measure_defaultHeight_isNonZero() {
    assertTrue(new LineChartElement().series("A", List.of(5, 10))
        .measure(new LayoutContext(0, 0, 400, Float.MAX_VALUE)) > 0);
  }

  // ── Single series ─────────────────────────────────────────────────────────

  @Test
  void generatesPdf_singleSeries() throws Exception {
    File out = new File("target/linechart-single.pdf");
    Document.create(doc -> doc.page(page -> page.size(PageSize.A4).margin(50).content(c ->
        c.item().lineChart(chart -> chart
            .labels(List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun"))
            .series("Revenue", List.of(42_000, 55_000, 48_000, 61_000, 58_000, 70_000))
            .showPoints(true)
            .showValues(true)
            .yUnit("€")
            .gridLines(5)
            .height(200)))))
        .generatePdf(out);
    assertTrue(out.exists() && out.length() > 0);
  }

  // ── Multi-series ──────────────────────────────────────────────────────────

  @Test
  void generatesPdf_multiSeries_legendBottom() throws Exception {
    File out = new File("target/linechart-multi-legend-bottom.pdf");
    Document.create(doc -> doc.page(page -> page.size(PageSize.A4).margin(50).content(c ->
        c.item().lineChart(chart -> chart
            .labels(List.of("Q1", "Q2", "Q3", "Q4"))
            .series("Revenue",  List.of(92_400, 101_000, 87_500, 115_200))
            .series("Expenses", List.of(61_000,  74_000, 68_000,  79_500))
            .legendPosition(LegendPosition.BOTTOM)
            .yUnit("€")
            .gridLines(4)
            .height(220)))))
        .generatePdf(out);
    assertTrue(out.exists() && out.length() > 0);
  }

  // ── Smooth + area fill ───────────────────────────────────────────────────

  @Test
  void generatesPdf_smooth_fillArea() throws Exception {
    File out = new File("target/linechart-smooth-fill.pdf");
    Document.create(doc -> doc.page(page -> page.size(PageSize.A4).margin(50).content(c ->
        c.item().lineChart(chart -> chart
            .title("Weekly Active Users")
            .labels(List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"))
            .series("Users", List.of(320, 410, 390, 520, 480, 610, 290))
            .smooth(true)
            .fillArea(true)
            .showPoints(true)
            .legendPosition(LegendPosition.NONE)
            .height(200)))))
        .generatePdf(out);
    assertTrue(out.exists() && out.length() > 0);
  }

  // ── Reference line ────────────────────────────────────────────────────────

  @Test
  void generatesPdf_withReferenceLine() throws Exception {
    File out = new File("target/linechart-reference.pdf");
    Document.create(doc -> doc.page(page -> page.size(PageSize.A4).margin(50).content(c ->
        c.item().lineChart(chart -> chart
            .labels(List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun"))
            .series("Revenue", List.of(42_000, 55_000, 48_000, 61_000, 58_000, 70_000), Colors.BLUE_500)
            .referenceLine(55_000, "Target", Colors.RED_500)
            .yUnit("€")
            .height(200)))))
        .generatePdf(out);
    assertTrue(out.exists() && out.length() > 0);
  }

  // ── Multi-series smooth ───────────────────────────────────────────────────

  @Test
  void generatesPdf_multiSeries_smooth_fillArea() throws Exception {
    File out = new File("target/linechart-multi-smooth.pdf");
    Document.create(doc -> doc.page(page -> page.size(PageSize.A4).margin(50).content(c ->
        c.item().lineChart(chart -> chart
            .title("Budget vs. Actual")
            .labels(List.of("Q1", "Q2", "Q3", "Q4"))
            .series("Budget", List.of(80_000, 85_000, 90_000, 95_000), Colors.SLATE_400)
            .series("Actual", List.of(92_400, 78_200, 101_000, 88_500), Colors.BLUE_600)
            .smooth(true)
            .fillArea(true)
            .showPoints(true)
            .showValues(true)
            .yUnit("€")
            .legendPosition(LegendPosition.TOP_RIGHT)
            .height(220)))))
        .generatePdf(out);
    assertTrue(out.exists() && out.length() > 0);
  }

  // ── Embedded in report ────────────────────────────────────────────────────

  @Test
  void generatesPdf_embeddedInReport() throws Exception {
    File out = new File("target/linechart-report.pdf");
    Document.create(doc -> doc.page(page -> page.size(PageSize.A4).margin(50).content(c -> {
      c.item().text("Trend Analysis").bold().fontSize(16);
      c.item().text("Monthly performance over the last six months.").fontSize(10).color(Colors.SLATE_500);
      c.add(new SpacerElement(12));
      c.item().lineChart(chart -> chart
          .title("Revenue & Expenses")
          .labels(List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun"))
          .series("Revenue",  List.of(42_000, 55_000, 48_000, 61_000, 58_000, 70_000), Colors.BLUE_500)
          .series("Expenses", List.of(35_000, 38_000, 40_000, 41_000, 43_000, 45_000), Colors.RED_400)
          .smooth(true)
          .fillArea(true)
          .showPoints(true)
          .referenceLine(50_000, "Break-even", Colors.AMBER_500)
          .yUnit("€")
          .legendPosition(LegendPosition.BOTTOM)
          .height(220));
      c.add(new SpacerElement(16));
      c.item().text("All figures in EUR excluding VAT.").fontSize(8).color(Colors.SLATE_400);
    }))).generatePdf(out);
    assertTrue(out.exists() && out.length() > 0);
  }
}
