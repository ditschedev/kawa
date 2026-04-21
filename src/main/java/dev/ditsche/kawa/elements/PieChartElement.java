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
 * Renders a pie or donut chart with named slices, optional percentage/value labels, configurable
 * legend, and an optional center label for donut mode.
 *
 * @author Tobias Dittmann
 */
public final class PieChartElement implements ContentElement {

  // ── Data ─────────────────────────────────────────────────────────────────
  private final List<Slice> slices = new ArrayList<>();

  // ── Layout ───────────────────────────────────────────────────────────────
  private float chartHeight = 200f;
  /** Fraction of the outer radius reserved as the donut hole (0 = solid pie). */
  private float innerRadiusRatio = 0f;
  private String centerLabel = null;
  private float centerLabelFontSize = 9f;
  private KawaColor centerLabelColor = Colors.SLATE_700;

  // ── Appearance ───────────────────────────────────────────────────────────
  private KawaColor labelColor = Colors.SLATE_700;
  private float labelFontSize = 7.5f;
  private boolean showPercentages = true;
  private boolean showValues = false;
  private KawaColor strokeColor = KawaColor.WHITE;
  private float strokeWidth = 1.5f;
  private LegendPosition legendPosition = LegendPosition.BOTTOM;

  // ── Title ─────────────────────────────────────────────────────────────────
  private String title = null;
  private float titleFontSize = 11f;
  private KawaColor titleColor = Colors.SLATE_800;

  // ─────────────────────────────────────────────────────────────────────────

  public PieChartElement() {}

  public PieChartElement(Consumer<PieChartElement> builder) {
    builder.accept(this);
  }

  // ── Fluent API ───────────────────────────────────────────────────────────

  public PieChartElement slice(String name, double value, KawaColor color) {
    slices.add(new Slice(name, value, color));
    return this;
  }

  public PieChartElement slice(String name, double value) {
    return slice(name, value, ChartSupport.PALETTE.get(slices.size() % ChartSupport.PALETTE.size()));
  }

  public PieChartElement title(String title) {
    this.title = title;
    return this;
  }

  public PieChartElement titleFontSize(float size) {
    this.titleFontSize = size;
    return this;
  }

  public PieChartElement titleColor(KawaColor color) {
    this.titleColor = color;
    return this;
  }

  public PieChartElement height(float pts) {
    this.chartHeight = pts;
    return this;
  }

  /**
   * Enables donut mode by specifying the inner hole radius as a fraction of the outer radius.
   * E.g. {@code 0.55f} gives a typical donut appearance.
   */
  public PieChartElement donut(float innerRadiusRatio) {
    this.innerRadiusRatio = Math.max(0f, Math.min(0.95f, innerRadiusRatio));
    return this;
  }

  /** Label drawn at the centre of a donut chart. Has no effect in solid pie mode. */
  public PieChartElement centerLabel(String label) {
    this.centerLabel = label;
    return this;
  }

  public PieChartElement centerLabelFontSize(float size) {
    this.centerLabelFontSize = size;
    return this;
  }

  public PieChartElement centerLabelColor(KawaColor color) {
    this.centerLabelColor = color;
    return this;
  }

  public PieChartElement showPercentages(boolean show) {
    this.showPercentages = show;
    return this;
  }

  public PieChartElement showValues(boolean show) {
    this.showValues = show;
    return this;
  }

  /** Stroke drawn between slices. Pass {@code null} color to disable. */
  public PieChartElement stroke(KawaColor color, float width) {
    this.strokeColor = color;
    this.strokeWidth = width;
    return this;
  }

  public PieChartElement legendPosition(LegendPosition position) {
    this.legendPosition = position;
    return this;
  }

  public PieChartElement labelColor(KawaColor c) {
    this.labelColor = c;
    return this;
  }

  public PieChartElement labelFontSize(float size) {
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
      throw new KawaRenderException("Failed to render pie chart", e);
    }
  }

  // ── Rendering ────────────────────────────────────────────────────────────

  private void renderChart(LayoutContext context, RenderContext renderCtx) throws IOException {
    if (slices.isEmpty()) return;

    PDFont font = renderCtx.getFont(400, false);
    PDFont boldFont = renderCtx.getFont(700, false);
    float pageH = renderCtx.getPage().getMediaBox().getHeight();
    PDPageContentStream stream = renderCtx.getContentStream();

    // Title
    float titleH = 0f;
    if (title != null) {
      titleH = titleFontSize * 1.4f + 6f;
      float textW = renderCtx.textWidth(title, boldFont, titleFontSize);
      float textX = context.x() + (context.width() - textW) / 2f;
      float textY = pageH - context.y() - titleFontSize;
      renderCtx.drawText(title, textX, textY, boldFont, titleFontSize, titleColor);
    }

    float bottomLegendH = (legendPosition == LegendPosition.BOTTOM)
        ? labelFontSize + 18f : 0f;
    float rightLegendW = (legendPosition == LegendPosition.TOP_RIGHT && !slices.isEmpty())
        ? computeRightLegendWidth(renderCtx, font) + 16f : 0f;

    float pieAreaH = chartHeight - titleH - bottomLegendH;
    float pieAreaW = context.width() - rightLegendW;

    // Pie circle geometry
    float margin = 10f;
    float diameter = Math.max(0f, Math.min(pieAreaW, pieAreaH) - 2 * margin);
    float outerR = diameter / 2f;
    float innerR = outerR * innerRadiusRatio;

    // PDF coordinates — y increases upward
    float pdfPieAreaTop = pageH - context.y() - titleH;
    float pdfPieAreaBottom = pdfPieAreaTop - pieAreaH;
    float cx = context.x() + pieAreaW / 2f;
    float cy = (pdfPieAreaTop + pdfPieAreaBottom) / 2f;

    // Compute total for fractions
    double total = slices.stream().mapToDouble(Slice::value).sum();
    if (total <= 0) return;

    // Draw slices
    float startAngle = (float) (Math.PI / 2); // 12 o'clock = 90°
    for (Slice slice : slices) {
      float span = (float) (2 * Math.PI * slice.value() / total);
      float endAngle = startAngle - span; // CW = decreasing angle

      drawSlice(stream, cx, cy, outerR, innerR, startAngle, endAngle, slice.color());
      startAngle = endAngle;
    }

    // Stroke between slices
    if (strokeColor != null && strokeWidth > 0) {
      stream.setStrokingColor(strokeColor.toAwtColor());
      stream.setLineWidth(strokeWidth);
      startAngle = (float) (Math.PI / 2);
      for (Slice slice : slices) {
        float span = (float) (2 * Math.PI * slice.value() / total);
        float endAngle = startAngle - span;
        float ox = cx + outerR * (float) Math.cos(startAngle);
        float oy = cy + outerR * (float) Math.sin(startAngle);
        if (innerRadiusRatio > 0) {
          float ix = cx + innerR * (float) Math.cos(startAngle);
          float iy = cy + innerR * (float) Math.sin(startAngle);
          stream.moveTo(ix, iy);
          stream.lineTo(ox, oy);
        } else {
          stream.moveTo(cx, cy);
          stream.lineTo(ox, oy);
        }
        stream.stroke();
        startAngle = endAngle;
      }
    }

    // Slice labels (percentages / values)
    if (showPercentages || showValues) {
      startAngle = (float) (Math.PI / 2);
      for (Slice slice : slices) {
        float span = (float) (2 * Math.PI * slice.value() / total);
        float midAngle = startAngle - span / 2f;
        double pct = slice.value() / total * 100.0;
        if (pct < 4.0) { // skip tiny slices
          startAngle -= span;
          continue;
        }
        String labelText = buildSliceLabel(slice.value(), pct);
        if (labelText == null || labelText.isEmpty()) {
          startAngle -= span;
          continue;
        }
        float tw = renderCtx.textWidth(labelText, font, labelFontSize);
        float labelR = outerR * (innerRadiusRatio > 0 ? (0.5f + innerRadiusRatio * 0.5f) : 0.65f);
        float lx = cx + labelR * (float) Math.cos(midAngle) - tw / 2f;
        float ly = cy + labelR * (float) Math.sin(midAngle) - labelFontSize * 0.35f;
        renderCtx.drawText(labelText, lx, ly, font, labelFontSize, KawaColor.WHITE);
        startAngle -= span;
      }
    }

    // Center label (donut only)
    if (innerRadiusRatio > 0 && centerLabel != null) {
      float tw = renderCtx.textWidth(centerLabel, boldFont, centerLabelFontSize);
      renderCtx.drawText(centerLabel,
          cx - tw / 2f,
          cy - centerLabelFontSize * 0.35f,
          boldFont, centerLabelFontSize, centerLabelColor);
    }

    // Bottom legend
    if (legendPosition == LegendPosition.BOTTOM) {
      float legendY = pdfPieAreaBottom - bottomLegendH * 0.5f;
      renderLegendHorizontal(context, renderCtx, font, legendY, total);
    }

    // Right legend
    if (legendPosition == LegendPosition.TOP_RIGHT) {
      float legendX = context.x() + pieAreaW + 8f;
      float legendY = cy + (slices.size() * (labelFontSize + 5f)) / 2f;
      renderLegendVertical(renderCtx, font, legendX, legendY, total);
    }
  }

  private void drawSlice(
      PDPageContentStream stream,
      float cx, float cy, float outerR, float innerR,
      float startAngle, float endAngle, KawaColor color) throws IOException {
    if (innerR > 0) {
      // Donut sector: outer arc → inner arc (reversed)
      stream.moveTo(cx + outerR * (float) Math.cos(startAngle),
          cy + outerR * (float) Math.sin(startAngle));
      ChartSupport.appendArc(stream, cx, cy, outerR, startAngle, endAngle);
      stream.lineTo(cx + innerR * (float) Math.cos(endAngle),
          cy + innerR * (float) Math.sin(endAngle));
      ChartSupport.appendArc(stream, cx, cy, innerR, endAngle, startAngle);
    } else {
      // Solid pie sector
      stream.moveTo(cx, cy);
      stream.lineTo(cx + outerR * (float) Math.cos(startAngle),
          cy + outerR * (float) Math.sin(startAngle));
      ChartSupport.appendArc(stream, cx, cy, outerR, startAngle, endAngle);
    }
    stream.closePath();
    stream.setNonStrokingColor(color.toAwtColor());
    stream.fill();
  }

  private String buildSliceLabel(double value, double pct) {
    if (showPercentages && showValues) {
      return String.format("%.0f%%", pct);
    }
    if (showPercentages) return String.format("%.0f%%", pct);
    if (showValues) return ChartSupport.formatValue(value);
    return null;
  }

  private void renderLegendHorizontal(
      LayoutContext context, RenderContext renderCtx, PDFont font,
      float pdfY, double total) throws IOException {
    float swatchSize = 7f;
    float itemGap = 14f;
    float totalW = 0f;
    for (Slice s : slices) {
      String name = legendName(s, total);
      totalW += swatchSize + 3f + renderCtx.textWidth(name, font, labelFontSize) + itemGap;
    }
    totalW -= itemGap;
    float x = context.x() + (context.width() - totalW) / 2f;
    for (Slice s : slices) {
      String name = legendName(s, total);
      renderCtx.drawRect(x, pdfY - swatchSize + labelFontSize * 0.15f,
          swatchSize, swatchSize, s.color());
      x += swatchSize + 3f;
      renderCtx.drawText(name, x, pdfY - labelFontSize * 0.85f,
          font, labelFontSize, labelColor);
      x += renderCtx.textWidth(name, font, labelFontSize) + itemGap;
    }
  }

  private void renderLegendVertical(
      RenderContext renderCtx, PDFont font,
      float x, float startY, double total) throws IOException {
    float rowH = labelFontSize + 5f;
    float swatchSize = 7f;
    float y = startY;
    for (Slice s : slices) {
      String name = legendName(s, total);
      renderCtx.drawRect(x, y - swatchSize + labelFontSize * 0.15f,
          swatchSize, swatchSize, s.color());
      renderCtx.drawText(name, x + swatchSize + 3f, y - labelFontSize * 0.85f,
          font, labelFontSize, labelColor);
      y -= rowH;
    }
  }

  private String legendName(Slice s, double total) {
    double pct = s.value() / total * 100.0;
    return String.format("%s (%.0f%%)", s.name(), pct);
  }

  private float computeRightLegendWidth(RenderContext renderCtx, PDFont font) throws IOException {
    float max = 0f;
    double total = slices.stream().mapToDouble(Slice::value).sum();
    for (Slice s : slices) {
      float w = renderCtx.textWidth(legendName(s, total), font, labelFontSize);
      if (w > max) max = w;
    }
    return max + 10f; // swatch + gap
  }

  // ── Records ───────────────────────────────────────────────────────────────

  private record Slice(String name, double value, KawaColor color) {}
}
