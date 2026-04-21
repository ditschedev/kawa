package dev.ditsche.kawa.elements;

import dev.ditsche.kawa.style.KawaColor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;

/**
 * Shared rendering utilities for chart elements. Package-private.
 *
 * @author Tobias Dittmann
 */
final class ChartSupport {

  static final List<KawaColor> PALETTE =
      List.of(
          KawaColor.hex("#3b82f6"),
          KawaColor.hex("#22c55e"),
          KawaColor.hex("#f59e0b"),
          KawaColor.hex("#ef4444"),
          KawaColor.hex("#8b5cf6"),
          KawaColor.hex("#06b6d4"),
          KawaColor.hex("#f97316"),
          KawaColor.hex("#ec4899"));

  private ChartSupport() {}

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
    while (candidate < rawMax) candidate += magnitude;
    return candidate;
  }

  static String formatTick(double value, String unit) {
    String s;
    if (value >= 1_000_000) s = String.format("%.1fM", value / 1_000_000);
    else if (value >= 1_000) s = String.format("%.0fk", value / 1_000);
    else if (value == Math.floor(value)) s = String.valueOf((long) value);
    else s = String.format("%.1f", value);
    return unit != null ? s + " " + unit : s;
  }

  static String formatValue(double value) {
    if (value >= 1_000_000) return String.format("%.1fM", value / 1_000_000);
    if (value >= 1_000) return String.format("%.0fk", value / 1_000);
    if (value == Math.floor(value)) return String.valueOf((long) value);
    return String.format("%.1f", value);
  }

  static List<Double> toDoubles(List<? extends Number> values) {
    List<Double> out = new ArrayList<>(values.size());
    for (Number n : values) out.add(n.doubleValue());
    return out;
  }

  /**
   * Appends a circular arc to the current open path using cubic Bezier approximation.
   * Angles are in radians measured CCW from the positive x-axis (standard PDF math convention).
   * A negative span (endAngle < startAngle) produces a clockwise arc.
   *
   * <p>The caller is responsible for positioning the path at the arc start before calling this
   * method (e.g. via {@code moveTo} or {@code lineTo}).
   */
  static void appendArc(
      PDPageContentStream stream, float cx, float cy, float r,
      float startAngle, float endAngle) throws IOException {
    float span = endAngle - startAngle;
    int segments = Math.max(1, (int) Math.ceil(Math.abs(span) / (float) (Math.PI / 2)));
    float step = span / segments;
    for (int i = 0; i < segments; i++) {
      float a0 = startAngle + i * step;
      float a1 = a0 + step;
      float k = (4f / 3f) * (float) Math.tan(step / 4f);
      float p1x = cx + r * ((float) Math.cos(a0) - k * (float) Math.sin(a0));
      float p1y = cy + r * ((float) Math.sin(a0) + k * (float) Math.cos(a0));
      float p2x = cx + r * ((float) Math.cos(a1) + k * (float) Math.sin(a1));
      float p2y = cy + r * ((float) Math.sin(a1) - k * (float) Math.cos(a1));
      stream.curveTo(
          p1x, p1y, p2x, p2y,
          cx + r * (float) Math.cos(a1),
          cy + r * (float) Math.sin(a1));
    }
  }

  /** Fills a circle centred at (cx, cy) with the given radius and color. */
  static void fillCircle(
      PDPageContentStream stream, float cx, float cy, float r, KawaColor color)
      throws IOException {
    float k = 0.5523f * r;
    stream.moveTo(cx + r, cy);
    stream.curveTo(cx + r, cy + k, cx + k, cy + r, cx, cy + r);
    stream.curveTo(cx - k, cy + r, cx - r, cy + k, cx - r, cy);
    stream.curveTo(cx - r, cy - k, cx - k, cy - r, cx, cy - r);
    stream.curveTo(cx + k, cy - r, cx + r, cy - k, cx + r, cy);
    stream.setNonStrokingColor(color.toAwtColor());
    stream.fill();
  }

  /**
   * Sets the non-stroking alpha constant on the content stream's graphics state.
   * Call {@code stream.restoreGraphicsState()} after drawing the transparent shape.
   */
  static void setFillAlpha(PDPageContentStream stream, float alpha) throws IOException {
    PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
    gs.setNonStrokingAlphaConstant(Math.max(0f, Math.min(1f, alpha)));
    stream.setGraphicsStateParameters(gs);
  }
}
