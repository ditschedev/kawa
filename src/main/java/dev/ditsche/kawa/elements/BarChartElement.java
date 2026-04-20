package dev.ditsche.kawa.elements;

import dev.ditsche.kawa.layout.LayoutContext;
import dev.ditsche.kawa.renderer.RenderContext;
import dev.ditsche.kawa.style.Colors;
import dev.ditsche.kawa.style.KawaColor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.apache.pdfbox.pdmodel.font.PDFont;

/**
 * Renders a vertical bar chart. Supports multiple series (grouped bars), configurable grid lines,
 * optional value labels, and a fixed chart height.
 *
 * <pre>{@code
 * c.item().barChart(chart -> chart
 *     .labels(List.of("Q1", "Q2", "Q3", "Q4"))
 *     .series("Revenue", List.of(42_000, 55_000, 61_000, 58_000))
 *     .series("Expenses", List.of(31_000, 38_000, 40_000, 35_000))
 *     .height(200)
 *     .showValues(true));
 * }</pre>
 */
public final class BarChartElement implements ContentElement {

  // ── Default colour palette (one per series) ─────────────────────────────
  private static final List<KawaColor> PALETTE =
      List.of(
          KawaColor.hex("#3b82f6"), // blue-500
          KawaColor.hex("#22c55e"), // green-500
          KawaColor.hex("#f59e0b"), // amber-500
          KawaColor.hex("#ef4444"), // red-500
          KawaColor.hex("#8b5cf6"), // violet-500
          KawaColor.hex("#06b6d4"), // cyan-500
          KawaColor.hex("#f97316"), // orange-500
          KawaColor.hex("#ec4899")  // pink-500
      );

  // ── Data ─────────────────────────────────────────────────────────────────
  private final List<Series> seriesList = new ArrayList<>();
  private List<String> labels = List.of();

  // ── Layout ───────────────────────────────────────────────────────────────
  private float chartHeight = 180f;
  private float yLabelWidth = 44f;
  private float xLabelHeight = 18f;
  /** Fraction of a category slot that is spacing between categories (0–1). */
  private float categoryGap = 0.25f;
  /** Fraction of a per-series bar slot that is inter-bar spacing (0–1). */
  private float barGap = 0.12f;

  // ── Appearance ───────────────────────────────────────────────────────────
  private KawaColor axisColor = Colors.SLATE_300;
  private KawaColor gridColor = Colors.SLATE_100;
  private KawaColor labelColor = Colors.SLATE_500;
  private float labelFontSize = 7.5f;
  private int gridLineCount = 4;
  private boolean showValues = false;
  private String yUnit = null;

  // ─────────────────────────────────────────────────────────────────────────

  public BarChartElement() {}

  public BarChartElement(Consumer<BarChartElement> builder) {
    builder.accept(this);
  }

  // ── Fluent API ───────────────────────────────────────────────────────────

  /** Category labels for the X-axis (one per data point). */
  public BarChartElement labels(List<String> labels) {
    this.labels = List.copyOf(labels);
    return this;
  }

  /** Adds a named series with an explicit colour. */
  public BarChartElement series(String name, List<? extends Number> values, KawaColor color) {
    seriesList.add(new Series(name, toDoubles(values), color));
    return this;
  }

  /** Adds a named series; colour is picked automatically from the built-in palette. */
  public BarChartElement series(String name, List<? extends Number> values) {
    KawaColor color = PALETTE.get(seriesList.size() % PALETTE.size());
    return series(name, values, color);
  }

  /** Total height of the element in points (plot area + axis labels). */
  public BarChartElement height(float pts) {
    this.chartHeight = pts;
    return this;
  }

  /** Width reserved for Y-axis labels on the left. */
  public BarChartElement yLabelWidth(float pts) {
    this.yLabelWidth = pts;
    return this;
  }

  /** Height reserved for X-axis labels at the bottom. */
  public BarChartElement xLabelHeight(float pts) {
    this.xLabelHeight = pts;
    return this;
  }

  /** Number of horizontal grid lines (excluding the x-axis baseline). */
  public BarChartElement gridLines(int count) {
    this.gridLineCount = count;
    return this;
  }

  /** Fraction of a category slot used as the gap between groups (0–1). Default 0.25. */
  public BarChartElement categoryGap(float fraction) {
    this.categoryGap = Math.max(0f, Math.min(0.9f, fraction));
    return this;
  }

  /** Show numeric value labels above each bar. */
  public BarChartElement showValues(boolean show) {
    this.showValues = show;
    return this;
  }

  /** Unit string appended to Y-axis tick labels (e.g. {@code "€"} or {@code "k"}). */
  public BarChartElement yUnit(String unit) {
    this.yUnit = unit;
    return this;
  }

  public BarChartElement axisColor(KawaColor c) {
    this.axisColor = c;
    return this;
  }

  public BarChartElement gridColor(KawaColor c) {
    this.gridColor = c;
    return this;
  }

  public BarChartElement labelColor(KawaColor c) {
    this.labelColor = c;
    return this;
  }

  public BarChartElement labelFontSize(float size) {
    this.labelFontSize = size;
    return this;
  }

  // ── Element contract ─────────────────────────────────────────────────────

  @Override
  public float measure(LayoutContext context) {
    return chartHeight;
  }

  @Override
  public void render(LayoutContext context, RenderContext renderCtx) {
    try {
      renderChart(context, renderCtx);
    } catch (IOException e) {
      throw new KawaRenderException("Failed to render bar chart", e);
    }
  }

  // ── Rendering ────────────────────────────────────────────────────────────

  private void renderChart(LayoutContext context, RenderContext renderCtx) throws IOException {
    if (seriesList.isEmpty()) return;

    PDFont font = renderCtx.getFont(400, false);
    float pageH = renderCtx.getPage().getMediaBox().getHeight();

    // Geometry
    float plotH = chartHeight - xLabelHeight;
    float plotX = context.x() + yLabelWidth;
    float plotW = context.width() - yLabelWidth;

    // PDF y-coords (bottom-left origin)
    float pdfAxisY = pageH - context.y() - plotH;   // x-axis baseline in PDF space
    float pdfPlotTop = pageH - context.y();           // top of plot area in PDF space

    // Scale
    double rawMax = computeMax();
    double niceMax = niceMax(rawMax, gridLineCount);

    // ── Grid + Y-axis labels ─────────────────────────────────────────────
    for (int t = 0; t <= gridLineCount; t++) {
      double tickValue = niceMax * t / gridLineCount;
      float pdfY = pdfAxisY + (float) (tickValue / niceMax) * plotH;

      // Grid line
      KawaColor lineColor = (t == 0) ? axisColor : gridColor;
      float lineW = (t == 0) ? 0.75f : 0.5f;
      renderCtx.drawLine(plotX, pdfY, plotX + plotW, pdfY, lineColor, lineW);

      // Y-axis tick label (right-aligned within yLabelWidth)
      String tickLabel = formatTick(tickValue);
      float textW = renderCtx.textWidth(tickLabel, font, labelFontSize);
      float textX = context.x() + yLabelWidth - textW - 5f;
      float textY = pdfY - labelFontSize * 0.35f; // vertically centred on grid line
      renderCtx.drawText(tickLabel, textX, textY, font, labelFontSize, labelColor);
    }

    // ── Vertical axis line ───────────────────────────────────────────────
    renderCtx.drawLine(plotX, pdfAxisY, plotX, pdfPlotTop, axisColor, 0.75f);

    // ── Bars ─────────────────────────────────────────────────────────────
    int catCount = categoryCount();
    int seriesCount = seriesList.size();
    if (catCount == 0) return;

    float slotW = plotW / catCount;
    float groupW = slotW * (1f - categoryGap);
    float groupOffset = (slotW - groupW) / 2f;
    float barSlotW = groupW / seriesCount;
    float singleBarW = barSlotW * (1f - barGap);
    float barInsetW = barSlotW * barGap / 2f;

    for (int si = 0; si < seriesCount; si++) {
      Series series = seriesList.get(si);
      for (int ci = 0; ci < catCount; ci++) {
        double value = ci < series.values().size() ? series.values().get(ci) : 0.0;
        if (value <= 0) continue;

        float barH = (float) (value / niceMax) * plotH;
        float barX = plotX + ci * slotW + groupOffset + si * barSlotW + barInsetW;
        float pdfBarBottom = pdfAxisY;
        float pdfBarTop = pdfAxisY + barH;

        // Bar fill
        renderCtx.drawRect(barX, pdfBarBottom, singleBarW, barH, series.color());

        // Value label above bar
        if (showValues) {
          String label = formatValue(value);
          float textW = renderCtx.textWidth(label, font, labelFontSize);
          float textX = barX + (singleBarW - textW) / 2f;
          float textY = pdfBarTop + 2.5f;
          renderCtx.drawText(label, textX, textY, font, labelFontSize, labelColor);
        }
      }
    }

    // ── X-axis category labels ───────────────────────────────────────────
    for (int ci = 0; ci < catCount; ci++) {
      String label = ci < labels.size() ? labels.get(ci) : String.valueOf(ci + 1);
      float textW = renderCtx.textWidth(label, font, labelFontSize);
      float centerX = plotX + ci * slotW + slotW / 2f;
      float textX = centerX - textW / 2f;
      float textY = pdfAxisY - xLabelHeight * 0.65f;
      renderCtx.drawText(label, textX, textY, font, labelFontSize, labelColor);
    }

    // ── Legend (when multiple series) ────────────────────────────────────
    if (seriesCount > 1) {
      renderLegend(context, renderCtx, font, pageH);
    }
  }

  private void renderLegend(
      LayoutContext context, RenderContext renderCtx, PDFont font, float pageH) throws IOException {

    float swatchSize = 7f;
    float itemGap = 16f;
    float legendY = pageH - context.y() - chartHeight + xLabelHeight * 0.3f;

    // Measure total legend width for right-alignment
    float totalW = 0f;
    for (Series s : seriesList) {
      totalW += swatchSize + 3f + renderCtx.textWidth(s.name(), font, labelFontSize) + itemGap;
    }
    totalW -= itemGap;

    float x = context.x() + context.width() - totalW;
    float plotX = context.x() + yLabelWidth;
    if (x < plotX) x = plotX; // clamp to plot area

    for (Series s : seriesList) {
      float swatchBottom = legendY - swatchSize + labelFontSize * 0.15f;
      renderCtx.drawRect(x, swatchBottom, swatchSize, swatchSize, s.color());
      x += swatchSize + 3f;
      float nameW = renderCtx.textWidth(s.name(), font, labelFontSize);
      renderCtx.drawText(s.name(), x, legendY - labelFontSize * 0.85f, font, labelFontSize, labelColor);
      x += nameW + itemGap;
    }
  }

  // ── Helpers ──────────────────────────────────────────────────────────────

  private int categoryCount() {
    if (!labels.isEmpty()) return labels.size();
    return seriesList.stream().mapToInt(s -> s.values().size()).max().orElse(0);
  }

  private double computeMax() {
    return seriesList.stream()
        .flatMap(s -> s.values().stream())
        .mapToDouble(Double::doubleValue)
        .max()
        .orElse(1.0);
  }

  /**
   * Rounds {@code rawMax} up to a "nice" number that divides evenly into {@code steps} ticks.
   * E.g. 92 400 → 100 000, 38 → 40.
   */
  static double niceMax(double rawMax, int steps) {
    if (rawMax <= 0) return steps;
    double magnitude = Math.pow(10, Math.floor(Math.log10(rawMax)));
    double normalised = rawMax / magnitude;
    double niceNorm = normalised <= 1 ? 1 : normalised <= 2 ? 2 : normalised <= 5 ? 5 : 10;
    double candidate = niceNorm * magnitude;
    // Ensure the candidate divides cleanly into `steps` ticks; bump if not enough headroom
    while (candidate < rawMax) candidate += magnitude;
    return candidate;
  }

  private String formatTick(double value) {
    String s;
    if (value >= 1_000_000) s = String.format("%.1fM", value / 1_000_000);
    else if (value >= 1_000) s = String.format("%.0fk", value / 1_000);
    else if (value == Math.floor(value)) s = String.valueOf((long) value);
    else s = String.format("%.1f", value);
    return yUnit != null ? s + " " + yUnit : s;
  }

  private String formatValue(double value) {
    if (value >= 1_000_000) return String.format("%.1fM", value / 1_000_000);
    if (value >= 1_000) return String.format("%.0fk", value / 1_000);
    if (value == Math.floor(value)) return String.valueOf((long) value);
    return String.format("%.1f", value);
  }

  private static List<Double> toDoubles(List<? extends Number> values) {
    List<Double> out = new ArrayList<>(values.size());
    for (Number n : values) out.add(n.doubleValue());
    return out;
  }

  // ── Series record ─────────────────────────────────────────────────────────

  private record Series(String name, List<Double> values, KawaColor color) {}
}
