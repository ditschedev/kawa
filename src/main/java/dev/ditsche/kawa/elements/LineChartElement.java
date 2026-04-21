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

/**
 * Renders a line (or area) chart with multiple series, optional smooth curves, data point markers,
 * value labels, reference lines, and a configurable legend.
 *
 * @author Tobias Dittmann
 */
public final class LineChartElement implements ContentElement {

  // ── Data ─────────────────────────────────────────────────────────────────
  private final List<Series> seriesList = new ArrayList<>();
  private final List<ReferenceLine> referenceLines = new ArrayList<>();
  private List<String> labels = List.of();

  // ── Layout ───────────────────────────────────────────────────────────────
  private float chartHeight = 180f;
  private float yLabelWidth = 44f;
  private float xLabelHeight = 18f;

  // ── Appearance ───────────────────────────────────────────────────────────
  private KawaColor axisColor = Colors.SLATE_300;
  private KawaColor gridColor = Colors.SLATE_100;
  private KawaColor labelColor = Colors.SLATE_500;
  private KawaColor valueColor = null;
  private float labelFontSize = 7.5f;
  private float lineWidth = 1.5f;
  private float pointRadius = 3f;
  private boolean smooth = false;
  private boolean fillArea = false;
  private boolean showPoints = true;
  private boolean showValues = false;
  private int gridLineCount = 4;
  private String yUnit = null;
  private Double explicitYMax = null;
  private LegendPosition legendPosition = LegendPosition.TOP_RIGHT;

  // ── Title ─────────────────────────────────────────────────────────────────
  private String title = null;
  private float titleFontSize = 11f;
  private KawaColor titleColor = Colors.SLATE_800;

  // ─────────────────────────────────────────────────────────────────────────

  public LineChartElement() {}

  public LineChartElement(Consumer<LineChartElement> builder) {
    builder.accept(this);
  }

  // ── Fluent API ───────────────────────────────────────────────────────────

  public LineChartElement labels(List<String> labels) {
    this.labels = List.copyOf(labels);
    return this;
  }

  public LineChartElement series(String name, List<? extends Number> values, KawaColor color) {
    seriesList.add(new Series(name, ChartSupport.toDoubles(values), color));
    return this;
  }

  public LineChartElement series(String name, List<? extends Number> values) {
    return series(name, values,
        ChartSupport.PALETTE.get(seriesList.size() % ChartSupport.PALETTE.size()));
  }

  public LineChartElement referenceLine(double value, String label, KawaColor color) {
    referenceLines.add(new ReferenceLine(value, label, color));
    return this;
  }

  public LineChartElement referenceLine(double value, String label) {
    return referenceLine(value, label, Colors.RED_500);
  }

  public LineChartElement title(String title) {
    this.title = title;
    return this;
  }

  public LineChartElement titleFontSize(float size) {
    this.titleFontSize = size;
    return this;
  }

  public LineChartElement titleColor(KawaColor color) {
    this.titleColor = color;
    return this;
  }

  public LineChartElement height(float pts) {
    this.chartHeight = pts;
    return this;
  }

  public LineChartElement yLabelWidth(float pts) {
    this.yLabelWidth = pts;
    return this;
  }

  public LineChartElement xLabelHeight(float pts) {
    this.xLabelHeight = pts;
    return this;
  }

  public LineChartElement gridLines(int count) {
    this.gridLineCount = count;
    return this;
  }

  public LineChartElement yMax(double max) {
    this.explicitYMax = max;
    return this;
  }

  public LineChartElement yUnit(String unit) {
    this.yUnit = unit;
    return this;
  }

  public LineChartElement legendPosition(LegendPosition position) {
    this.legendPosition = position;
    return this;
  }

  /** Whether to draw smooth Catmull-Rom curves instead of straight line segments. */
  public LineChartElement smooth(boolean smooth) {
    this.smooth = smooth;
    return this;
  }

  /** Whether to fill the area below each series line with a semi-transparent tint. */
  public LineChartElement fillArea(boolean fill) {
    this.fillArea = fill;
    return this;
  }

  /** Whether to draw a dot at each data point. */
  public LineChartElement showPoints(boolean show) {
    this.showPoints = show;
    return this;
  }

  /** Whether to draw value labels at each data point. */
  public LineChartElement showValues(boolean show) {
    this.showValues = show;
    return this;
  }

  public LineChartElement lineWidth(float pts) {
    this.lineWidth = Math.max(0.5f, pts);
    return this;
  }

  public LineChartElement pointRadius(float pts) {
    this.pointRadius = Math.max(0f, pts);
    return this;
  }

  public LineChartElement valueColor(KawaColor color) {
    this.valueColor = color;
    return this;
  }

  public LineChartElement axisColor(KawaColor c) {
    this.axisColor = c;
    return this;
  }

  public LineChartElement gridColor(KawaColor c) {
    this.gridColor = c;
    return this;
  }

  public LineChartElement labelColor(KawaColor c) {
    this.labelColor = c;
    return this;
  }

  public LineChartElement labelFontSize(float size) {
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
      throw new KawaRenderException("Failed to render line chart", e);
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

    float bottomLegendH = (legendPosition == LegendPosition.BOTTOM && seriesList.size() > 1)
        ? labelFontSize + 10f : 0f;

    float plotH = chartHeight - titleH - xLabelHeight - bottomLegendH;
    float plotX = context.x() + yLabelWidth;
    float plotW = context.width() - yLabelWidth;
    float pdfPlotTop = pageH - context.y() - titleH;
    float pdfPlotBottom = pdfPlotTop - plotH;

    double rawMax = computeMax();
    double axisMax = explicitYMax != null ? explicitYMax : ChartSupport.niceMax(rawMax, gridLineCount);

    // Grid + Y-axis labels
    for (int t = 0; t <= gridLineCount; t++) {
      double tickValue = axisMax * t / gridLineCount;
      float pdfY = pdfPlotBottom + (float) (tickValue / axisMax) * plotH;
      KawaColor lineColor = (t == 0) ? axisColor : gridColor;
      float lw = (t == 0) ? 0.75f : 0.5f;
      renderCtx.drawLine(plotX, pdfY, plotX + plotW, pdfY, lineColor, lw);
      String tickLabel = ChartSupport.formatTick(tickValue, yUnit);
      float textW = renderCtx.textWidth(tickLabel, font, labelFontSize);
      renderCtx.drawText(tickLabel,
          context.x() + yLabelWidth - textW - 5f,
          pdfY - labelFontSize * 0.35f,
          font, labelFontSize, labelColor);
    }

    renderCtx.drawLine(plotX, pdfPlotBottom, plotX, pdfPlotTop, axisColor, 0.75f);

    // Reference lines
    for (ReferenceLine ref : referenceLines) {
      float pdfY = pdfPlotBottom + (float) (ref.value() / axisMax) * plotH;
      renderCtx.drawLine(plotX, pdfY, plotX + plotW, pdfY, ref.color(), 1f);
      if (ref.label() != null) {
        float tw = renderCtx.textWidth(ref.label(), font, labelFontSize);
        renderCtx.drawText(ref.label(), plotX + plotW - tw - 3f, pdfY + 3f,
            font, labelFontSize, ref.color());
      }
    }

    int n = dataPointCount();
    if (n == 0) return;

    // Pre-compute PDF coordinates for each series
    for (int si = 0; si < seriesList.size(); si++) {
      Series series = seriesList.get(si);
      int pts = Math.min(n, series.values().size());
      float[] xs = new float[pts];
      float[] ys = new float[pts];
      for (int i = 0; i < pts; i++) {
        xs[i] = n == 1 ? plotX + plotW / 2f : plotX + (float) i / (n - 1) * plotW;
        ys[i] = pdfPlotBottom + (float) (series.values().get(i) / axisMax) * plotH;
      }

      // Area fill (semi-transparent)
      if (fillArea && pts > 1) {
        stream.saveGraphicsState();
        ChartSupport.setFillAlpha(stream, 0.15f);
        stream.moveTo(xs[0], pdfPlotBottom);
        stream.lineTo(xs[0], ys[0]);
        appendLinePath(stream, xs, ys, pts);
        stream.lineTo(xs[pts - 1], pdfPlotBottom);
        stream.closePath();
        stream.setNonStrokingColor(series.color().toAwtColor());
        stream.fill();
        stream.restoreGraphicsState();
      }

      // Line
      stream.setStrokingColor(series.color().toAwtColor());
      stream.setLineWidth(lineWidth);
      stream.moveTo(xs[0], ys[0]);
      appendLinePath(stream, xs, ys, pts);
      stream.stroke();

      // Points
      if (showPoints && pointRadius > 0) {
        for (int i = 0; i < pts; i++) {
          ChartSupport.fillCircle(stream, xs[i], ys[i], pointRadius, series.color());
          // White inner dot for multi-series
          if (seriesList.size() > 1 && pointRadius > 1.5f) {
            ChartSupport.fillCircle(stream, xs[i], ys[i], pointRadius * 0.45f, KawaColor.WHITE);
          }
        }
      }

      // Value labels
      if (showValues) {
        KawaColor vc = valueColor != null ? valueColor : labelColor;
        for (int i = 0; i < pts; i++) {
          String label = ChartSupport.formatValue(series.values().get(i));
          float tw = renderCtx.textWidth(label, font, labelFontSize);
          renderCtx.drawText(label, xs[i] - tw / 2f, ys[i] + pointRadius + 3f,
              font, labelFontSize, vc);
        }
      }
    }

    // X-axis labels
    for (int i = 0; i < n; i++) {
      String label = i < labels.size() ? labels.get(i) : String.valueOf(i + 1);
      float tw = renderCtx.textWidth(label, font, labelFontSize);
      float x = n == 1 ? plotX + plotW / 2f : plotX + (float) i / (n - 1) * plotW;
      renderCtx.drawText(label, x - tw / 2f, pdfPlotBottom - xLabelHeight * 0.65f,
          font, labelFontSize, labelColor);
    }

    // Legends
    if (legendPosition == LegendPosition.TOP_RIGHT && seriesList.size() > 1) {
      renderLegend(context, renderCtx, font, plotX, plotX + plotW,
          pdfPlotTop + labelFontSize * 0.15f);
    }
    if (legendPosition == LegendPosition.BOTTOM && seriesList.size() > 1) {
      float legendY = pdfPlotBottom - xLabelHeight - bottomLegendH * 0.5f;
      renderLegend(context, renderCtx, font, plotX, plotX + plotW, legendY);
    }
  }

  private void appendLinePath(
      PDPageContentStream stream, float[] xs, float[] ys, int pts) throws IOException {
    if (smooth && pts > 2) {
      for (int i = 0; i < pts - 1; i++) {
        float px = i > 0 ? xs[i - 1] : xs[i];
        float py = i > 0 ? ys[i - 1] : ys[i];
        float nx = i < pts - 2 ? xs[i + 2] : xs[i + 1];
        float ny = i < pts - 2 ? ys[i + 2] : ys[i + 1];
        float cp1x = xs[i] + (xs[i + 1] - px) / 6f;
        float cp1y = ys[i] + (ys[i + 1] - py) / 6f;
        float cp2x = xs[i + 1] - (nx - xs[i]) / 6f;
        float cp2y = ys[i + 1] - (ny - ys[i]) / 6f;
        stream.curveTo(cp1x, cp1y, cp2x, cp2y, xs[i + 1], ys[i + 1]);
      }
    } else {
      for (int i = 1; i < pts; i++) {
        stream.lineTo(xs[i], ys[i]);
      }
    }
  }

  private void renderLegend(
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
      float midY = pdfY - labelFontSize * 0.35f;
      renderCtx.drawLine(x, midY, x + swatchSize, midY, s.color(), 2f);
      x += swatchSize + 3f;
      renderCtx.drawText(s.name(), x, pdfY - labelFontSize * 0.85f,
          font, labelFontSize, labelColor);
      x += renderCtx.textWidth(s.name(), font, labelFontSize) + itemGap;
    }
  }

  // ── Helpers ──────────────────────────────────────────────────────────────

  private int dataPointCount() {
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

  // ── Records ───────────────────────────────────────────────────────────────

  private record Series(String name, List<Double> values, KawaColor color) {}

  private record ReferenceLine(double value, String label, KawaColor color) {}
}
