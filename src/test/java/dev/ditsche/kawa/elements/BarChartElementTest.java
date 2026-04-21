package dev.ditsche.kawa.elements;

import static org.junit.jupiter.api.Assertions.*;

import dev.ditsche.kawa.core.Document;
import dev.ditsche.kawa.core.PageSize;
import dev.ditsche.kawa.layout.LayoutContext;
import dev.ditsche.kawa.style.Colors;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Test;
import static dev.ditsche.kawa.elements.LegendPosition.*;

class BarChartElementTest {

  // ── measure ──────────────────────────────────────────────────────────────

  @Test
  void measure_returnsConfiguredHeight() {
    BarChartElement chart = new BarChartElement().height(150).series("A", List.of(10, 20, 30));
    assertEquals(150f, chart.measure(new LayoutContext(0, 0, 400, Float.MAX_VALUE)), 0.01f);
  }

  @Test
  void measure_defaultHeight_isNonZero() {
    assertTrue(new BarChartElement().series("A", List.of(5, 10))
        .measure(new LayoutContext(0, 0, 400, Float.MAX_VALUE)) > 0);
  }

  // ── niceMax ──────────────────────────────────────────────────────────────

  @Test
  void niceMax_roundsUpToNiceNumber() {
    assertEquals(100.0, BarChartElement.niceMax(92.4, 4), 0.001);
    assertEquals(10.0,  BarChartElement.niceMax(7.3, 5),  0.001);
    assertEquals(50.0,  BarChartElement.niceMax(41.0, 5), 0.001);
  }

  @Test
  void niceMax_alwaysGreaterThanOrEqualToRawMax() {
    for (double raw : new double[]{1, 5, 9.9, 42, 100, 999, 1234, 99_999, 1_000_000}) {
      assertTrue(BarChartElement.niceMax(raw, 4) >= raw,
          "niceMax must be >= rawMax for input " + raw);
    }
  }

  @Test
  void niceMax_zeroInput_returnsPositive() {
    assertTrue(BarChartElement.niceMax(0, 4) > 0);
  }

  // ── Vertical (default) ───────────────────────────────────────────────────

  @Test
  void generatesPdf_singleSeries() throws Exception {
    File out = new File("target/barchart-single.pdf");
    Document.create(doc -> doc.page(page -> page.size(PageSize.A4).margin(50).content(c ->
        c.item().barChart(chart -> chart
            .labels(List.of("Jan", "Feb", "Mar", "Apr", "May", "Jun"))
            .series("Revenue", List.of(42_000, 55_000, 48_000, 61_000, 58_000, 70_000))
            .height(200)
            .showValues(true)
            .yUnit("€")
            .gridLines(5)))))
        .generatePdf(out);
    assertTrue(out.exists() && out.length() > 0);
  }

  @Test
  void generatesPdf_multiSeries_withLegendBottom() throws Exception {
    File out = new File("target/barchart-multi-legend-bottom.pdf");
    Document.create(doc -> doc.page(page -> page.size(PageSize.A4).margin(50).content(c ->
        c.item().barChart(chart -> chart
            .labels(List.of("Q1", "Q2", "Q3", "Q4"))
            .series("Revenue",  List.of(92_400, 101_000, 87_500, 115_200))
            .series("Expenses", List.of(61_000,  74_000, 68_000,  79_500))
            .series("Profit",   List.of(31_400,  27_000, 19_500,  35_700))
            .legendPosition(BOTTOM)
            .height(220)
            .yUnit("€")
            .gridLines(4)))))
        .generatePdf(out);
    assertTrue(out.exists() && out.length() > 0);
  }

  @Test
  void generatesPdf_withTitle_cornerRadius_referenceLine() throws Exception {
    File out = new File("target/barchart-title-rounded-ref.pdf");
    Document.create(doc -> doc.page(page -> page.size(PageSize.A4).margin(50).content(c ->
        c.item().barChart(chart -> chart
            .title("Monthly Active Users")
            .labels(List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"))
            .series("Users", List.of(320, 410, 390, 520, 480, 610, 290))
            .referenceLine(450, "Target", Colors.RED_500)
            .cornerRadius(4)
            .showValues(true)
            .valueColor(Colors.SLATE_700)
            .legendPosition(NONE)
            .height(200)))))
        .generatePdf(out);
    assertTrue(out.exists() && out.length() > 0);
  }

  @Test
  void generatesPdf_withBarStroke_explicitYMax() throws Exception {
    File out = new File("target/barchart-stroke-ymax.pdf");
    Document.create(doc -> doc.page(page -> page.size(PageSize.A4).margin(50).content(c ->
        c.item().barChart(chart -> chart
            .labels(List.of("A", "B", "C", "D", "E"))
            .series("Score", List.of(72, 88, 65, 91, 78), Colors.INDIGO_500)
            .barStroke(Colors.INDIGO_700, 0.8f)
            .yMax(100)
            .cornerRadius(3)
            .showValues(true)
            .height(180)))))
        .generatePdf(out);
    assertTrue(out.exists() && out.length() > 0);
  }

  // ── Horizontal ───────────────────────────────────────────────────────────

  @Test
  void generatesPdf_horizontal_singleSeries() throws Exception {
    File out = new File("target/barchart-horizontal.pdf");
    Document.create(doc -> doc.page(page -> page.size(PageSize.A4).margin(50).content(c ->
        c.item().barChart(chart -> chart
            .horizontal()
            .labels(List.of("North", "South", "East", "West", "Central"))
            .series("Revenue", List.of(142_000, 98_000, 115_000, 87_000, 201_000))
            .cornerRadius(4)
            .showValues(true)
            .yUnit("€")
            .height(200)))))
        .generatePdf(out);
    assertTrue(out.exists() && out.length() > 0);
  }

  @Test
  void generatesPdf_horizontal_multiSeries_withTitle() throws Exception {
    File out = new File("target/barchart-horizontal-multi.pdf");
    Document.create(doc -> doc.page(page -> page.size(PageSize.A4).margin(50).content(c ->
        c.item().barChart(chart -> chart
            .title("Budget vs. Actual by Department")
            .horizontal()
            .yLabelWidth(70)
            .labels(List.of("Engineering", "Marketing", "Sales", "Support", "G&A"))
            .series("Budget", List.of(220_000, 95_000, 130_000, 60_000, 45_000), Colors.SLATE_300)
            .series("Actual", List.of(241_000, 88_000, 147_000, 54_000, 42_000), Colors.BLUE_500)
            .referenceLine(100_000, "Avg", Colors.AMBER_500)
            .legendPosition(BOTTOM)
            .showValues(true)
            .yUnit("€")
            .height(240)))))
        .generatePdf(out);
    assertTrue(out.exists() && out.length() > 0);
  }

  // ── Legend positions ─────────────────────────────────────────────────────

  @Test
  void generatesPdf_legendNone() throws Exception {
    File out = new File("target/barchart-legend-none.pdf");
    Document.create(doc -> doc.page(page -> page.size(PageSize.A4).margin(50).content(c ->
        c.item().barChart(chart -> chart
            .labels(List.of("Q1", "Q2", "Q3", "Q4"))
            .series("A", List.of(10, 20, 30, 25))
            .series("B", List.of(15, 12, 28, 32))
            .legendPosition(NONE)
            .height(180)))))
        .generatePdf(out);
    assertTrue(out.exists() && out.length() > 0);
  }

  // ── ON_BAR legend ────────────────────────────────────────────────────────

  @Test
  void generatesPdf_legendOnBar_vertical() throws Exception {
    File out = new File("target/barchart-legend-on-bar-vertical.pdf");
    Document.create(doc -> doc.page(page -> page.size(PageSize.A4).margin(50).content(c ->
        c.item().barChart(chart -> chart
            .title("Budget vs. Actual")
            .labels(List.of("Q1", "Q2", "Q3", "Q4"))
            .series("Budget", List.of(80_000, 85_000, 90_000, 95_000), Colors.SLATE_400)
            .series("Actual", List.of(92_400, 78_200, 101_000, 88_500), Colors.BLUE_600)
            .cornerRadius(3)
            .legendPosition(ON_BAR)
            .height(220)
            .yUnit("€")))))
        .generatePdf(out);
    assertTrue(out.exists() && out.length() > 0);
  }

  @Test
  void generatesPdf_legendOnBar_horizontal() throws Exception {
    File out = new File("target/barchart-legend-on-bar-horizontal.pdf");
    Document.create(doc -> doc.page(page -> page.size(PageSize.A4).margin(50).content(c ->
        c.item().barChart(chart -> chart
            .title("Department Headcount")
            .horizontal()
            .yLabelWidth(70)
            .labels(List.of("Engineering", "Marketing", "Sales", "Support"))
            .series("2025", List.of(42, 18, 31, 24), Colors.SLATE_300)
            .series("2026", List.of(51, 21, 38, 27), Colors.BLUE_500)
            .cornerRadius(3)
            .legendPosition(ON_BAR)
            .height(200)))))
        .generatePdf(out);
    assertTrue(out.exists() && out.length() > 0);
  }

  // ── Layout integration ───────────────────────────────────────────────────

  @Test
  void generatesPdf_embeddedInReport() throws Exception {
    File out = new File("target/barchart-report.pdf");
    Document.create(doc -> doc.page(page -> page.size(PageSize.A4).margin(50).content(c -> {
      c.item().text("Sales Overview").bold().fontSize(16);
      c.item().text("Year-to-date performance by quarter.").fontSize(10).color(Colors.SLATE_500);
      c.add(new SpacerElement(12));
      c.item().barChart(chart -> chart
          .title("Budget vs. Actual")
          .labels(List.of("Q1", "Q2", "Q3", "Q4"))
          .series("Budget", List.of(80_000, 85_000, 90_000, 95_000), Colors.SLATE_300)
          .series("Actual", List.of(92_400, 78_200, 101_000, 88_500), Colors.BLUE_500)
          .referenceLine(85_000, "Avg. Budget", Colors.AMBER_600)
          .cornerRadius(3)
          .showValues(true)
          .yUnit("€")
          .legendPosition(BOTTOM)
          .height(220));
      c.add(new SpacerElement(16));
      c.item().text("All figures in EUR excluding VAT.").fontSize(8).color(Colors.SLATE_400);
    }))).generatePdf(out);
    assertTrue(out.exists() && out.length() > 0);
  }
}
