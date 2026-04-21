package dev.ditsche.kawa.elements;

import dev.ditsche.kawa.layout.LayoutContext;
import dev.ditsche.kawa.renderer.RenderContext;
import dev.ditsche.kawa.style.Colors;
import dev.ditsche.kawa.style.KawaColor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.util.Matrix;

/**
 * Renders a vertical or horizontal bar chart. Supports multiple series (grouped bars), reference
 * lines, rounded corners, configurable legend position, and optional value labels.
 *
 * @author Tobias Dittmann
 */
public final class BarChartElement implements ContentElement {

  // Bezier factor for quarter-circle approximation
  private static final float K = 0.5523f;

  // ── Data ─────────────────────────────────────────────────────────────────
  private final List<Series> seriesList = new ArrayList<>();
  private final List<ReferenceLine> referenceLines = new ArrayList<>();
  private List<String> labels = List.of();

  // ── Layout ───────────────────────────────────────────────────────────────
  private float chartHeight = 180f;
  private float yLabelWidth = 44f;
  private float xLabelHeight = 18f;
  private float categoryGap = 0.25f;
  private float barGap = 0.12f;
  private float maxBarWidth = Float.MAX_VALUE;
  private boolean horizontal = false;

  // ── Appearance ───────────────────────────────────────────────────────────
  private KawaColor axisColor = Colors.SLATE_300;
  private KawaColor gridColor = Colors.SLATE_100;
  private KawaColor labelColor = Colors.SLATE_500;
  private KawaColor valueColor = null; // null → falls back to labelColor
  private KawaColor barStrokeColor = null;
  private float barStrokeWidth = 0.5f;
  private float labelFontSize = 7.5f;
  private float cornerRadius = 0f;
  private int gridLineCount = 4;
  private boolean showValues = false;
  private String yUnit = null;
  private Double explicitYMax = null;
  private LegendPosition legendPosition = LegendPosition.TOP_RIGHT;

  // ── Title ─────────────────────────────────────────────────────────────────
  private String title = null;
  private float titleFontSize = 11f;
  private KawaColor titleColor = Colors.SLATE_800;

  // ─────────────────────────────────────────────────────────────────────────

  public BarChartElement() {}

  public BarChartElement(Consumer<BarChartElement> builder) {
    builder.accept(this);
  }

  // ── Fluent API ───────────────────────────────────────────────────────────

  /** Category labels for the axis (one per data point). */
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
    return series(name, values, ChartSupport.PALETTE.get(seriesList.size() % ChartSupport.PALETTE.size()));
  }

  /**
   * Adds a horizontal reference line (e.g. a target or threshold) at the given value.
   * In horizontal mode the line is drawn vertically.
   */
  public BarChartElement referenceLine(double value, String label, KawaColor color) {
    referenceLines.add(new ReferenceLine(value, label, color));
    return this;
  }

  /** Adds a reference line with the default accent colour. */
  public BarChartElement referenceLine(double value, String label) {
    return referenceLine(value, label, Colors.RED_500);
  }

  /** Adds a reference line with no label. */
  public BarChartElement referenceLine(double value, KawaColor color) {
    return referenceLine(value, null, color);
  }

  /** Chart title rendered above the plot area. */
  public BarChartElement title(String title) {
    this.title = title;
    return this;
  }

  public BarChartElement titleFontSize(float size) {
    this.titleFontSize = size;
    return this;
  }

  public BarChartElement titleColor(KawaColor color) {
    this.titleColor = color;
    return this;
  }

  /** Switches to a horizontal (rotated) bar chart layout. */
  public BarChartElement horizontal() {
    this.horizontal = true;
    return this;
  }

  /** Corner radius for bar ends (top corners in vertical mode, right corners in horizontal). */
  public BarChartElement cornerRadius(float r) {
    this.cornerRadius = Math.max(0f, r);
    return this;
  }

  /** Explicit upper bound for the value axis; disables auto-scaling. */
  public BarChartElement yMax(double max) {
    this.explicitYMax = max;
    return this;
  }

  /** Where to place the series legend. */
  public BarChartElement legendPosition(LegendPosition position) {
    this.legendPosition = position;
    return this;
  }

  /** Total height of the element in points. */
  public BarChartElement height(float pts) {
    this.chartHeight = pts;
    return this;
  }

  /** Width reserved for the category/value labels on the Y axis (vertical) or left side (horizontal). */
  public BarChartElement yLabelWidth(float pts) {
    this.yLabelWidth = pts;
    return this;
  }

  /** Height reserved for axis labels at the bottom. */
  public BarChartElement xLabelHeight(float pts) {
    this.xLabelHeight = pts;
    return this;
  }

  /** Number of grid lines across the value axis. */
  public BarChartElement gridLines(int count) {
    this.gridLineCount = count;
    return this;
  }

  /** Fraction of a category slot used as padding between groups (0–1). Default 0.25. */
  public BarChartElement categoryGap(float fraction) {
    this.categoryGap = Math.max(0f, Math.min(0.9f, fraction));
    return this;
  }

  /**
   * Caps the width of a single bar in points. Useful when only a few categories are present and
   * the proportional width would produce bars that are too wide. Slack is distributed as symmetric
   * padding so the group remains centred in its slot.
   */
  public BarChartElement maxBarWidth(float pts) {
    this.maxBarWidth = Math.max(1f, pts);
    return this;
  }

  /** Show numeric value labels on each bar. */
  public BarChartElement showValues(boolean show) {
    this.showValues = show;
    return this;
  }

  /** Unit string appended to value-axis tick labels (e.g. {@code "€"}). */
  public BarChartElement yUnit(String unit) {
    this.yUnit = unit;
    return this;
  }

  /** Colour for value labels above/beside bars. Defaults to {@link #labelColor} when not set. */
  public BarChartElement valueColor(KawaColor color) {
    this.valueColor = color;
    return this;
  }

  /** Outline drawn around each bar. Pass {@code null} to disable. */
  public BarChartElement barStroke(KawaColor color, float width) {
    this.barStrokeColor = color;
    this.barStrokeWidth = width;
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
    PDFont boldFont = renderCtx.getFont(700, false);
    float pageH = renderCtx.getPage().getMediaBox().getHeight();
    PDPageContentStream stream = renderCtx.getContentStream();

    // Title
    float titleH = 0f;
    if (title != null) {
      titleH = titleFontSize * 1.4f + 6f;
      float textW = renderCtx.textWidth(title, boldFont, titleFontSize);
      float textX = context.x() + yLabelWidth + (context.width() - yLabelWidth - textW) / 2f;
      float textY = pageH - context.y() - titleFontSize;
      renderCtx.drawText(title, textX, textY, boldFont, titleFontSize, titleColor);
    }

    // Legend height at bottom (reserved below xLabelHeight)
    float bottomLegendH = (legendPosition == LegendPosition.BOTTOM && seriesList.size() > 1)
        ? labelFontSize + 10f : 0f;

    // Plot geometry
    float plotH = chartHeight - titleH - xLabelHeight - bottomLegendH;
    float plotX = context.x() + yLabelWidth;
    float plotW = context.width() - yLabelWidth;
    float pdfPlotTop = pageH - context.y() - titleH;
    float pdfPlotBottom = pdfPlotTop - plotH;

    // Axis scale
    double rawMax = computeMax();
    double axisMax = explicitYMax != null ? explicitYMax : niceMax(rawMax, gridLineCount);

    if (horizontal) {
      renderHorizontal(context, renderCtx, stream, font, boldFont,
          plotX, plotW, plotH, pdfPlotTop, pdfPlotBottom, axisMax);
    } else {
      renderVertical(context, renderCtx, stream, font, boldFont,
          plotX, plotW, plotH, pdfPlotTop, pdfPlotBottom, axisMax);
    }

    // Bottom legend
    if (legendPosition == LegendPosition.BOTTOM && seriesList.size() > 1) {
      float legendY = pdfPlotBottom - xLabelHeight - bottomLegendH * 0.5f;
      renderLegendHorizontal(context, renderCtx, font, plotX, plotX + plotW, legendY);
    }
  }

  // ── Vertical (default) ───────────────────────────────────────────────────

  private void renderVertical(
      LayoutContext context, RenderContext renderCtx, PDPageContentStream stream,
      PDFont font, PDFont boldFont,
      float plotX, float plotW, float plotH,
      float pdfPlotTop, float pdfPlotBottom,
      double axisMax) throws IOException {

    // Grid + Y-axis labels
    for (int t = 0; t <= gridLineCount; t++) {
      double tickValue = axisMax * t / gridLineCount;
      float pdfY = pdfPlotBottom + (float) (tickValue / axisMax) * plotH;

      KawaColor lineColor = (t == 0) ? axisColor : gridColor;
      float lineW = (t == 0) ? 0.75f : 0.5f;
      renderCtx.drawLine(plotX, pdfY, plotX + plotW, pdfY, lineColor, lineW);

      String tickLabel = formatTick(tickValue);
      float textW = renderCtx.textWidth(tickLabel, font, labelFontSize);
      renderCtx.drawText(tickLabel,
          context.x() + yLabelWidth - textW - 5f,
          pdfY - labelFontSize * 0.35f,
          font, labelFontSize, labelColor);
    }

    // Vertical axis line
    renderCtx.drawLine(plotX, pdfPlotBottom, plotX, pdfPlotTop, axisColor, 0.75f);

    // Reference lines
    for (ReferenceLine ref : referenceLines) {
      float pdfY = pdfPlotBottom + (float) (ref.value() / axisMax) * plotH;
      renderCtx.drawLine(plotX, pdfY, plotX + plotW, pdfY, ref.color(), 1f);
      if (ref.label() != null) {
        float textW = renderCtx.textWidth(ref.label(), font, labelFontSize);
        renderCtx.drawText(ref.label(),
            plotX + plotW - textW - 3f,
            pdfY + 3f,
            font, labelFontSize, ref.color());
      }
    }

    // Bars
    int catCount = categoryCount();
    int seriesCount = seriesList.size();
    if (catCount == 0) return;

    float slotW = plotW / catCount;
    float groupW = slotW * (1f - categoryGap);
    float groupOffset = (slotW - groupW) / 2f;
    float barSlotW = groupW / seriesCount;
    float singleBarW = Math.max(1f, Math.min(maxBarWidth, barSlotW * (1f - barGap)));
    // re-centre the group if maxBarWidth capped the bar width
    float effectiveGroupW = singleBarW * seriesCount + barSlotW * barGap * seriesCount;
    groupOffset += (groupW - effectiveGroupW) / 2f;
    float barInset = barSlotW * barGap / 2f;

    for (int si = 0; si < seriesCount; si++) {
      Series series = seriesList.get(si);
      for (int ci = 0; ci < catCount; ci++) {
        double value = ci < series.values().size() ? series.values().get(ci) : 0.0;
        if (value <= 0) continue;

        float barH = (float) (value / axisMax) * plotH;
        float barX = plotX + ci * slotW + groupOffset + si * barSlotW + barInset;
        float r = Math.min(cornerRadius, Math.min(singleBarW / 2f, barH));

        drawBar(stream, barX, pdfPlotBottom, singleBarW, barH, r, false, series.color());

        if (showValues) {
          String label = formatValue(value);
          float textW = renderCtx.textWidth(label, font, labelFontSize);
          renderCtx.drawText(label,
              barX + (singleBarW - textW) / 2f,
              pdfPlotBottom + barH + 2.5f,
              font, labelFontSize, effectiveValueColor());
        }

        if (legendPosition == LegendPosition.ON_BAR && seriesCount > 1) {
          drawOnBarLabelVertical(stream, font, series.name(),
              barX, pdfPlotBottom, singleBarW, barH);
        }
      }
    }

    // X-axis category labels
    for (int ci = 0; ci < catCount; ci++) {
      String label = ci < labels.size() ? labels.get(ci) : String.valueOf(ci + 1);
      float textW = renderCtx.textWidth(label, font, labelFontSize);
      float centerX = plotX + ci * slotW + slotW / 2f;
      renderCtx.drawText(label,
          centerX - textW / 2f,
          pdfPlotBottom - xLabelHeight * 0.65f,
          font, labelFontSize, labelColor);
    }

    // Top-right legend
    if (legendPosition == LegendPosition.TOP_RIGHT && seriesCount > 1) {
      renderLegendHorizontal(context, renderCtx, font, plotX, plotX + plotW,
          pdfPlotTop + labelFontSize * 0.15f);
    }
  }

  // ── Horizontal ───────────────────────────────────────────────────────────

  private void renderHorizontal(
      LayoutContext context, RenderContext renderCtx, PDPageContentStream stream,
      PDFont font, PDFont boldFont,
      float plotX, float plotW, float plotH,
      float pdfPlotTop, float pdfPlotBottom,
      double axisMax) throws IOException {

    // Grid + value-axis labels (vertical grid lines, labels at bottom)
    for (int t = 0; t <= gridLineCount; t++) {
      double tickValue = axisMax * t / gridLineCount;
      float pdfX = plotX + (float) (tickValue / axisMax) * plotW;

      KawaColor lineColor = (t == 0) ? axisColor : gridColor;
      float lineW = (t == 0) ? 0.75f : 0.5f;
      renderCtx.drawLine(pdfX, pdfPlotBottom, pdfX, pdfPlotTop, lineColor, lineW);

      String tickLabel = formatTick(tickValue);
      float textW = renderCtx.textWidth(tickLabel, font, labelFontSize);
      renderCtx.drawText(tickLabel,
          pdfX - textW / 2f,
          pdfPlotBottom - xLabelHeight * 0.65f,
          font, labelFontSize, labelColor);
    }

    // Horizontal axis baseline
    renderCtx.drawLine(plotX, pdfPlotBottom, plotX, pdfPlotTop, axisColor, 0.75f);

    // Reference lines (vertical in horizontal mode)
    for (ReferenceLine ref : referenceLines) {
      float pdfX = plotX + (float) (ref.value() / axisMax) * plotW;
      renderCtx.drawLine(pdfX, pdfPlotBottom, pdfX, pdfPlotTop, ref.color(), 1f);
      if (ref.label() != null) {
        renderCtx.drawText(ref.label(), pdfX + 3f,
            pdfPlotBottom + 3f, font, labelFontSize, ref.color());
      }
    }

    // Bars
    int catCount = categoryCount();
    int seriesCount = seriesList.size();
    if (catCount == 0) return;

    float slotH = plotH / catCount;
    float groupH = slotH * (1f - categoryGap);
    float barSlotH = groupH / seriesCount;
    float singleBarH = Math.max(1f, Math.min(maxBarWidth, barSlotH * (1f - barGap)));
    float barInset = barSlotH * barGap / 2f;

    for (int si = 0; si < seriesCount; si++) {
      Series series = seriesList.get(si);
      for (int ci = 0; ci < catCount; ci++) {
        double value = ci < series.values().size() ? series.values().get(ci) : 0.0;
        if (value <= 0) continue;

        float barW = (float) (value / axisMax) * plotW;
        float groupTop = pdfPlotTop - ci * slotH - (slotH - groupH) / 2f;
        float barBottom = groupTop - (si + 1) * barSlotH + barInset;
        float r = Math.min(cornerRadius, Math.min(singleBarH / 2f, barW));

        drawBar(stream, plotX, barBottom, barW, singleBarH, r, true, series.color());

        if (showValues) {
          String label = formatValue(value);
          renderCtx.drawText(label,
              plotX + barW + 3f,
              barBottom + (singleBarH - labelFontSize) / 2f,
              font, labelFontSize, effectiveValueColor());
        }

        if (legendPosition == LegendPosition.ON_BAR && seriesCount > 1) {
          drawOnBarLabelHorizontal(stream, renderCtx, font, series.name(),
              plotX, barBottom, barW, singleBarH);
        }
      }
    }

    // Category labels (right-aligned in yLabelWidth zone, centred on each slot)
    for (int ci = 0; ci < catCount; ci++) {
      String label = ci < labels.size() ? labels.get(ci) : String.valueOf(ci + 1);
      float textW = renderCtx.textWidth(label, font, labelFontSize);
      float slotCenterY = pdfPlotTop - ci * slotH - slotH / 2f;
      renderCtx.drawText(label,
          context.x() + yLabelWidth - textW - 5f,
          slotCenterY - labelFontSize * 0.35f,
          font, labelFontSize, labelColor);
    }

    // Top-right legend
    if (legendPosition == LegendPosition.TOP_RIGHT && seriesCount > 1) {
      renderLegendHorizontal(context, renderCtx, font, plotX, plotX + plotW,
          pdfPlotTop + labelFontSize * 0.15f);
    }
  }

  // ── Legend ───────────────────────────────────────────────────────────────

  private void renderLegendHorizontal(
      LayoutContext context, RenderContext renderCtx, PDFont font,
      float plotLeft, float plotRight, float pdfY) throws IOException {

    float swatchSize = 7f;
    float itemGap = 14f;

    float totalW = 0f;
    for (Series s : seriesList) {
      totalW += swatchSize + 3f + renderCtx.textWidth(s.name(), font, labelFontSize) + itemGap;
    }
    totalW -= itemGap;

    float x = Math.max(plotLeft, plotRight - totalW);
    for (Series s : seriesList) {
      float swatchBottom = pdfY - swatchSize + labelFontSize * 0.15f;
      renderCtx.drawRect(x, swatchBottom, swatchSize, swatchSize, s.color());
      x += swatchSize + 3f;
      renderCtx.drawText(s.name(), x, pdfY - labelFontSize * 0.85f,
          font, labelFontSize, labelColor);
      x += renderCtx.textWidth(s.name(), font, labelFontSize) + itemGap;
    }
  }

  // ── On-bar labels ───────────────────────────────────────────────────────���

  /**
   * Draws a series name rotated 90° CCW inside a vertical bar.
   * Skipped silently when the bar is too short to fit the text.
   */
  private void drawOnBarLabelVertical(
      PDPageContentStream stream, PDFont font, String name,
      float barX, float pdfBarBottom, float barW, float barH) throws IOException {
    float textW;
    try {
      textW = font.getStringWidth(name) / 1000f * labelFontSize;
    } catch (IOException e) {
      return;
    }
    // Minimum bar height: text plus a small margin on each side
    if (barH < textW + labelFontSize * 2f) return;

    KawaColor color = effectiveOnBarColor();
    float barCenterX = barX + barW / 2f;
    float barCenterY = pdfBarBottom + barH / 2f;

    // With Matrix(0,1,-1,0,tx,ty): text advances upward (+y), glyphs extend left (−x).
    // tx positions the baseline (rightmost extent of glyphs + ascent offset).
    // ty is the bottom of the text run.
    float tx = barCenterX + labelFontSize * 0.35f;
    float ty = barCenterY - textW / 2f;

    stream.beginText();
    stream.setFont(font, labelFontSize);
    stream.setNonStrokingColor(color.toAwtColor());
    stream.setTextMatrix(new Matrix(0, 1, -1, 0, tx, ty));
    stream.showText(name);
    stream.endText();
  }

  /**
   * Draws a series name horizontally inside a horizontal bar.
   * Skipped silently when the bar is too narrow to fit the text.
   */
  private void drawOnBarLabelHorizontal(
      PDPageContentStream stream, RenderContext renderCtx, PDFont font, String name,
      float plotX, float barBottom, float barW, float barH) throws IOException {
    float textW = renderCtx.textWidth(name, font, labelFontSize);
    if (barW < textW + labelFontSize * 2f) return;

    KawaColor color = effectiveOnBarColor();
    float textX = plotX + labelFontSize * 0.8f;
    float textY = barBottom + (barH - labelFontSize) / 2f;
    renderCtx.drawText(name, textX, textY, font, labelFontSize, color);
  }

  // ── Bar drawing ──────────────────────────────────────────────────────────

  /**
   * Draws a single bar. When {@code horizontal} is true, {@code r} rounds the right corners;
   * otherwise the top corners.
   */
  private void drawBar(
      PDPageContentStream stream,
      float x, float y, float w, float h,
      float r, boolean roundRight,
      KawaColor fill) throws IOException {

    if (r <= 0) {
      stream.addRect(x, y, w, h);
    } else if (roundRight) {
      // Rounded right corners (used in horizontal mode)
      stream.moveTo(x, y);
      stream.lineTo(x + w - r, y);
      stream.curveTo(x + w - r + K * r, y, x + w, y + r - K * r, x + w, y + r);
      stream.lineTo(x + w, y + h - r);
      stream.curveTo(x + w, y + h - r + K * r, x + w - r + K * r, y + h, x + w - r, y + h);
      stream.lineTo(x, y + h);
      stream.closePath();
    } else {
      // Rounded top corners (used in vertical mode)
      stream.moveTo(x, y);
      stream.lineTo(x + w, y);
      stream.lineTo(x + w, y + h - r);
      stream.curveTo(x + w, y + h - r + K * r, x + w - r + K * r, y + h, x + w - r, y + h);
      stream.lineTo(x + r, y + h);
      stream.curveTo(x + r - K * r, y + h, x, y + h - r + K * r, x, y + h - r);
      stream.closePath();
    }

    if (barStrokeColor != null && barStrokeWidth > 0) {
      stream.setNonStrokingColor(fill.toAwtColor());
      stream.setStrokingColor(barStrokeColor.toAwtColor());
      stream.setLineWidth(barStrokeWidth);
      stream.fillAndStroke();
    } else {
      stream.setNonStrokingColor(fill.toAwtColor());
      stream.fill();
    }
  }

  // ── Helpers ──────────────────────────────────────────────────────────────

  private KawaColor effectiveValueColor() {
    return valueColor != null ? valueColor : labelColor;
  }

  private KawaColor effectiveOnBarColor() {
    return valueColor != null ? valueColor : KawaColor.WHITE;
  }

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

  /** Delegates to {@link ChartSupport#niceMax} — kept for backward-compatible test access. */
  static double niceMax(double rawMax, int steps) {
    return ChartSupport.niceMax(rawMax, steps);
  }

  private String formatTick(double value) {
    return ChartSupport.formatTick(value, yUnit);
  }

  private String formatValue(double value) {
    return ChartSupport.formatValue(value);
  }

  private static List<Double> toDoubles(List<? extends Number> values) {
    return ChartSupport.toDoubles(values);
  }

  // ── Records ───────────────────────────────────────────────────────────────

  private record Series(String name, List<Double> values, KawaColor color) {}

  private record ReferenceLine(double value, String label, KawaColor color) {}
}
