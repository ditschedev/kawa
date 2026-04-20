package dev.ditsche.kawa.elements;

import static org.junit.jupiter.api.Assertions.*;

import dev.ditsche.kawa.core.Document;
import dev.ditsche.kawa.core.PageSize;
import dev.ditsche.kawa.layout.LayoutContext;
import dev.ditsche.kawa.style.Colors;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Test;

class BarChartElementTest {

  // ── measure ──────────────────────────────────────────────────────────────

  @Test
  void measure_returnsConfiguredHeight() {
    BarChartElement chart =
        new BarChartElement().height(150).series("A", List.of(10, 20, 30));

    float height = chart.measure(new LayoutContext(0, 0, 400, Float.MAX_VALUE));

    assertEquals(150f, height, 0.01f, "measure() must return the configured height");
  }

  @Test
  void measure_defaultHeight_isNonZero() {
    BarChartElement chart = new BarChartElement().series("A", List.of(5, 10));

    float height = chart.measure(new LayoutContext(0, 0, 400, Float.MAX_VALUE));

    assertTrue(height > 0, "default height must be positive");
  }

  // ── niceMax ──────────────────────────────────────────────────────────────

  @Test
  void niceMax_roundsUpToNiceNumber() {
    assertEquals(100.0, BarChartElement.niceMax(92.4, 4), 0.001);
    assertEquals(10.0,  BarChartElement.niceMax(7.3, 5), 0.001);
    assertEquals(50.0,  BarChartElement.niceMax(41.0, 5), 0.001);
  }

  @Test
  void niceMax_alwaysGreaterThanOrEqualToRawMax() {
    double[] samples = {1, 5, 9.9, 42, 100, 999, 1234, 99_999, 1_000_000};
    for (double raw : samples) {
      double nice = BarChartElement.niceMax(raw, 4);
      assertTrue(nice >= raw, "niceMax must be >= rawMax for input " + raw);
    }
  }

  @Test
  void niceMax_zeroInput_returnsPositive() {
    assertTrue(BarChartElement.niceMax(0, 4) > 0);
  }

  // ── PDF generation ───────────────────────────────────────────────────────

  @Test
  void generatesPdf_singleSeries() throws Exception {
    File out = new File("target/element-barchart-single.pdf");

    Document.create(
            doc ->
                doc.page(
                    page ->
                        page.size(PageSize.A4)
                            .margin(50)
                            .content(
                                c -> {
                                  c.item()
                                      .text("Monthly Revenue")
                                      .bold()
                                      .fontSize(14);
                                  c.item()
                                      .barChart(
                                          chart ->
                                              chart
                                                  .labels(
                                                      List.of(
                                                          "Jan", "Feb", "Mar", "Apr", "May", "Jun"))
                                                  .series(
                                                      "Revenue",
                                                      List.of(
                                                          42_000, 55_000, 48_000, 61_000, 58_000,
                                                          70_000))
                                                  .height(200)
                                                  .showValues(true)
                                                  .yUnit("€")
                                                  .gridLines(5));
                                })))
        .generatePdf(out);

    assertTrue(out.exists() && out.length() > 0, "output PDF must be written");
  }

  @Test
  void generatesPdf_multiSeries() throws Exception {
    File out = new File("target/element-barchart-multi.pdf");

    Document.create(
            doc ->
                doc.page(
                    page ->
                        page.size(PageSize.A4)
                            .margin(50)
                            .content(
                                c ->
                                    c.item()
                                        .barChart(
                                            chart ->
                                                chart
                                                    .labels(List.of("Q1", "Q2", "Q3", "Q4"))
                                                    .series(
                                                        "Revenue",
                                                        List.of(92_400, 101_000, 87_500, 115_200))
                                                    .series(
                                                        "Expenses",
                                                        List.of(61_000, 74_000, 68_000, 79_500))
                                                    .series(
                                                        "Profit",
                                                        List.of(31_400, 27_000, 19_500, 35_700))
                                                    .height(220)
                                                    .showValues(false)
                                                    .yUnit("€")
                                                    .gridLines(4)))))
        .generatePdf(out);

    assertTrue(out.exists() && out.length() > 0, "output PDF must be written");
  }

  @Test
  void generatesPdf_customColors() throws Exception {
    File out = new File("target/element-barchart-colors.pdf");

    Document.create(
            doc ->
                doc.page(
                    page ->
                        page.size(PageSize.A4)
                            .margin(50)
                            .content(
                                c ->
                                    c.item()
                                        .barChart(
                                            chart ->
                                                chart
                                                    .labels(
                                                        List.of("Mon", "Tue", "Wed", "Thu", "Fri"))
                                                    .series(
                                                        "Visitors",
                                                        List.of(320, 410, 390, 520, 480),
                                                        Colors.INDIGO_500)
                                                    .height(180)
                                                    .showValues(true)
                                                    .categoryGap(0.3f)))))
        .generatePdf(out);

    assertTrue(out.exists() && out.length() > 0, "output PDF must be written");
  }

  @Test
  void generatesPdf_embeddedInLayout() throws Exception {
    File out = new File("target/element-barchart-layout.pdf");

    Document.create(
            doc ->
                doc.page(
                    page ->
                        page.size(PageSize.A4)
                            .margin(50)
                            .content(
                                c -> {
                                  c.item().text("Sales Overview").bold().fontSize(16);
                                  c.item().text("Year-to-date performance by quarter.")
                                      .fontSize(10).color(Colors.SLATE_500);
                                  c.add(new SpacerElement(12));
                                  c.item()
                                      .barChart(
                                          chart ->
                                              chart
                                                  .labels(List.of("Q1", "Q2", "Q3", "Q4"))
                                                  .series(
                                                      "Budget",
                                                      List.of(80_000, 85_000, 90_000, 95_000),
                                                      Colors.SLATE_300)
                                                  .series(
                                                      "Actual",
                                                      List.of(92_400, 78_200, 101_000, 88_500),
                                                      Colors.BLUE_500)
                                                  .height(200)
                                                  .showValues(true)
                                                  .yUnit("€")
                                                  .gridLines(4));
                                  c.add(new SpacerElement(16));
                                  c.item().text("All figures in EUR excluding VAT.")
                                      .fontSize(8).color(Colors.SLATE_400);
                                })))
        .generatePdf(out);

    assertTrue(out.exists() && out.length() > 0, "output PDF must be written");
  }
}
