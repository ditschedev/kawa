package dev.ditsche.kawa.elements;

import static org.junit.jupiter.api.Assertions.*;

import dev.ditsche.kawa.core.Document;
import dev.ditsche.kawa.core.PageSize;
import dev.ditsche.kawa.layout.LayoutContext;
import dev.ditsche.kawa.style.Colors;
import java.io.File;
import org.junit.jupiter.api.Test;

class PieChartElementTest {

  // ── measure ──────────────────────────────────────────────────────────────

  @Test
  void measure_returnsConfiguredHeight() {
    PieChartElement chart = new PieChartElement().height(150).slice("A", 10).slice("B", 20);
    assertEquals(150f, chart.measure(new LayoutContext(0, 0, 400, Float.MAX_VALUE)), 0.01f);
  }

  @Test
  void measure_defaultHeight_isNonZero() {
    assertTrue(new PieChartElement().slice("A", 5).slice("B", 10)
        .measure(new LayoutContext(0, 0, 400, Float.MAX_VALUE)) > 0);
  }

  // ── Solid pie ─────────────────────────────────────────────────────────────

  @Test
  void generatesPdf_solidPie_withPercentages() throws Exception {
    File out = new File("target/piechart-solid.pdf");
    Document.create(doc -> doc.page(page -> page.size(PageSize.A4).margin(50).content(c ->
        c.item().pieChart(chart -> chart
            .slice("North",   42_000)
            .slice("South",   28_000)
            .slice("East",    35_000)
            .slice("West",    19_000)
            .showPercentages(true)
            .legendPosition(LegendPosition.BOTTOM)
            .height(220)))))
        .generatePdf(out);
    assertTrue(out.exists() && out.length() > 0);
  }

  @Test
  void generatesPdf_solidPie_withTitle_legendBottom() throws Exception {
    File out = new File("target/piechart-title.pdf");
    Document.create(doc -> doc.page(page -> page.size(PageSize.A4).margin(50).content(c ->
        c.item().pieChart(chart -> chart
            .title("Revenue by Region")
            .slice("North",   42_000, Colors.BLUE_500)
            .slice("South",   28_000, Colors.GREEN_500)
            .slice("East",    35_000, Colors.AMBER_500)
            .slice("West",    19_000, Colors.RED_500)
            .showPercentages(true)
            .legendPosition(LegendPosition.BOTTOM)
            .height(240)))))
        .generatePdf(out);
    assertTrue(out.exists() && out.length() > 0);
  }

  // ── Donut ─────────────────────────────────────────────────────────────────

  @Test
  void generatesPdf_donut_withCenterLabel() throws Exception {
    File out = new File("target/piechart-donut.pdf");
    Document.create(doc -> doc.page(page -> page.size(PageSize.A4).margin(50).content(c ->
        c.item().pieChart(chart -> chart
            .title("Market Share")
            .slice("Product A", 38.5)
            .slice("Product B", 27.0)
            .slice("Product C", 21.5)
            .slice("Others",    13.0)
            .donut(0.55f)
            .centerLabel("Total")
            .showPercentages(true)
            .legendPosition(LegendPosition.BOTTOM)
            .height(240)))))
        .generatePdf(out);
    assertTrue(out.exists() && out.length() > 0);
  }

  @Test
  void generatesPdf_donut_legendRight() throws Exception {
    File out = new File("target/piechart-donut-legend-right.pdf");
    Document.create(doc -> doc.page(page -> page.size(PageSize.A4).margin(50).content(c ->
        c.item().pieChart(chart -> chart
            .title("Department Budget Allocation")
            .slice("Engineering", 220_000)
            .slice("Marketing",    95_000)
            .slice("Sales",       130_000)
            .slice("Support",      60_000)
            .slice("G&A",          45_000)
            .donut(0.5f)
            .centerLabel("Budget")
            .showPercentages(true)
            .legendPosition(LegendPosition.TOP_RIGHT)
            .height(220)))))
        .generatePdf(out);
    assertTrue(out.exists() && out.length() > 0);
  }

  @Test
  void generatesPdf_donut_showValues() throws Exception {
    File out = new File("target/piechart-donut-values.pdf");
    Document.create(doc -> doc.page(page -> page.size(PageSize.A4).margin(50).content(c ->
        c.item().pieChart(chart -> chart
            .slice("Q1", 92_400)
            .slice("Q2", 101_000)
            .slice("Q3", 87_500)
            .slice("Q4", 115_200)
            .donut(0.6f)
            .showValues(true)
            .legendPosition(LegendPosition.BOTTOM)
            .height(220)))))
        .generatePdf(out);
    assertTrue(out.exists() && out.length() > 0);
  }

  // ── Legend positions ──────────────────────────────────────────────────────

  @Test
  void generatesPdf_legendNone() throws Exception {
    File out = new File("target/piechart-legend-none.pdf");
    Document.create(doc -> doc.page(page -> page.size(PageSize.A4).margin(50).content(c ->
        c.item().pieChart(chart -> chart
            .slice("A", 40)
            .slice("B", 30)
            .slice("C", 20)
            .slice("D", 10)
            .showPercentages(true)
            .legendPosition(LegendPosition.NONE)
            .height(200)))))
        .generatePdf(out);
    assertTrue(out.exists() && out.length() > 0);
  }

  // ── Embedded in report ────────────────────────────────────────────────────

  @Test
  void generatesPdf_embeddedInReport() throws Exception {
    File out = new File("target/piechart-report.pdf");
    Document.create(doc -> doc.page(page -> page.size(PageSize.A4).margin(50).content(c -> {
      c.item().text("Sales Distribution").bold().fontSize(16);
      c.item().text("Revenue share by product line, year-to-date.").fontSize(10).color(Colors.SLATE_500);
      c.add(new SpacerElement(12));
      c.item().pieChart(chart -> chart
          .title("Product Revenue Share")
          .slice("Software",  145_000, Colors.BLUE_500)
          .slice("Services",   87_000, Colors.GREEN_500)
          .slice("Hardware",   62_000, Colors.AMBER_500)
          .slice("Training",   28_000, Colors.VIOLET_500)
          .donut(0.55f)
          .centerLabel("YTD")
          .showPercentages(true)
          .legendPosition(LegendPosition.BOTTOM)
          .height(220));
      c.add(new SpacerElement(16));
      c.item().text("All figures in EUR.").fontSize(8).color(Colors.SLATE_400);
    }))).generatePdf(out);
    assertTrue(out.exists() && out.length() > 0);
  }
}
