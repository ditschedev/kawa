package dev.ditsche.kawa.style;

import java.awt.*;

/**
 * Immutable color value with factory methods for hex, HSL, and OKLCh input. Use {@link
 * #toAwtColor()} to convert to {@link Color} for PDFBox rendering.
 */
public final class KawaColor {

  public static final KawaColor BLACK = new KawaColor(0, 0, 0, 255);
  public static final KawaColor WHITE = new KawaColor(255, 255, 255, 255);

  private final int r, g, b, a;

  private KawaColor(int r, int g, int b, int a) {
    this.r = clamp(r);
    this.g = clamp(g);
    this.b = clamp(b);
    this.a = clamp(a);
  }

  // -------------------------------------------------------------------------
  // Factories
  // -------------------------------------------------------------------------

  /**
   * Creates a color from an RGB hex string, e.g. {@code "#3B82F6"} or {@code "3B82F6"}. An 8-digit
   * hex string (RRGGBBAA) also sets the alpha channel.
   */
  public static KawaColor hex(String hex) {
    String h = hex.startsWith("#") ? hex.substring(1) : hex;
    if (h.length() == 8) {
      int rgba = (int) Long.parseLong(h, 16);
      return new KawaColor(
          (rgba >> 24) & 0xFF, (rgba >> 16) & 0xFF, (rgba >> 8) & 0xFF, rgba & 0xFF);
    }
    int rgb = Integer.parseInt(h, 16);
    return new KawaColor((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, 255);
  }

  /**
   * Creates a color from RGB components (fully opaque).
   *
   * @param r red 0–255
   * @param g green 0–255
   * @param b blue 0–255
   */
  public static KawaColor rgb(int r, int g, int b) {
    return new KawaColor(r, g, b, 255);
  }

  /**
   * Creates a color from RGBA components.
   *
   * @param r red 0–255
   * @param g green 0–255
   * @param b blue 0–255
   * @param a alpha 0–255 (0 = fully transparent, 255 = fully opaque)
   */
  public static KawaColor rgba(int r, int g, int b, int a) {
    return new KawaColor(r, g, b, a);
  }

  /**
   * Creates a fully opaque color from HSL components.
   *
   * @param h hue 0–360 degrees
   * @param s saturation 0–1
   * @param l lightness 0–1
   */
  public static KawaColor hsl(float h, float s, float l) {
    return hsla(h, s, l, 255);
  }

  /**
   * Creates a color from HSLA components.
   *
   * @param h hue 0–360 degrees
   * @param s saturation 0–1
   * @param l lightness 0–1
   * @param a alpha 0–255
   */
  public static KawaColor hsla(float h, float s, float l, int a) {
    float c = (1f - Math.abs(2f * l - 1f)) * s;
    float x = c * (1f - Math.abs((h / 60f) % 2f - 1f));
    float m = l - c / 2f;
    float r1, g1, b1;
    if (h < 60f) {
      r1 = c;
      g1 = x;
      b1 = 0;
    } else if (h < 120f) {
      r1 = x;
      g1 = c;
      b1 = 0;
    } else if (h < 180f) {
      r1 = 0;
      g1 = c;
      b1 = x;
    } else if (h < 240f) {
      r1 = 0;
      g1 = x;
      b1 = c;
    } else if (h < 300f) {
      r1 = x;
      g1 = 0;
      b1 = c;
    } else {
      r1 = c;
      g1 = 0;
      b1 = x;
    }
    return new KawaColor(
        Math.round((r1 + m) * 255f), Math.round((g1 + m) * 255f), Math.round((b1 + m) * 255f), a);
  }

  /**
   * Creates a fully opaque color from OKLCh components.
   *
   * @param L lightness 0–1
   * @param C chroma 0–0.4 (typical)
   * @param h hue 0–360 degrees
   */
  public static KawaColor oklch(float L, float C, float h) {
    return oklcha(L, C, h, 255);
  }

  /**
   * Creates a color from OKLCh components with an explicit alpha.
   *
   * @param L lightness 0–1
   * @param C chroma 0–0.4 (typical)
   * @param h hue 0–360 degrees
   * @param a alpha 0–255
   */
  public static KawaColor oklcha(float L, float C, float h, int a) {
    double hRad = Math.toRadians(h);
    double ca = C * Math.cos(hRad);
    double cb = C * Math.sin(hRad);

    // L is in CSS percentage [0,100]; OKLab math requires [0,1]
    double Ln = L / 100.0;

    // OKLab → linear sRGB cube-root intermediates
    double l_ = Ln + 0.3963377774 * ca + 0.2158037573 * cb;
    double m_ = Ln - 0.1055613458 * ca - 0.0638541728 * cb;
    double s_ = Ln - 0.0894841775 * ca - 1.2914855480 * cb;

    double l = l_ * l_ * l_;
    double m = m_ * m_ * m_;
    double s = s_ * s_ * s_;

    double rl = 4.0767416621 * l - 3.3077115913 * m + 0.2309699292 * s;
    double gl = -1.2684380046 * l + 2.6097574011 * m - 0.3413193965 * s;
    double bl = -0.0041960863 * l - 0.7034186147 * m + 1.7076147010 * s;

    return new KawaColor(
        (int) Math.round(linearToSrgb(rl) * 255),
        (int) Math.round(linearToSrgb(gl) * 255),
        (int) Math.round(linearToSrgb(bl) * 255),
        a);
  }

  // -------------------------------------------------------------------------
  // Conversion
  // -------------------------------------------------------------------------

  private static double linearToSrgb(double c) {
    c = Math.max(0.0, Math.min(1.0, c));
    return c <= 0.0031308 ? 12.92 * c : 1.055 * Math.pow(c, 1.0 / 2.4) - 0.055;
  }

  // -------------------------------------------------------------------------
  // Accessors
  // -------------------------------------------------------------------------

  private static int clamp(int v) {
    return Math.max(0, Math.min(255, v));
  }

  /** Returns the equivalent {@link Color} for use with PDFBox, preserving alpha. */
  public Color toAwtColor() {
    return new Color(r, g, b, a);
  }

  public int getRed() {
    return r;
  }

  public int getGreen() {
    return g;
  }

  public int getBlue() {
    return b;
  }

  public int getAlpha() {
    return a;
  }

  // -------------------------------------------------------------------------
  // Internals
  // -------------------------------------------------------------------------

  /** Returns a copy of this color with the given alpha (0–255). */
  public KawaColor withAlpha(int alpha) {
    return new KawaColor(r, g, b, alpha);
  }

  /** Returns a copy of this color with the given alpha as a fraction (0.0–1.0). */
  public KawaColor withAlpha(float alpha) {
    return withAlpha(Math.round(alpha * 255f));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof KawaColor kc)) return false;
    return r == kc.r && g == kc.g && b == kc.b && a == kc.a;
  }

  @Override
  public int hashCode() {
    return (a << 24) | (r << 16) | (g << 8) | b;
  }

  @Override
  public String toString() {
    return a == 255
        ? String.format("KawaColor(#%02X%02X%02X)", r, g, b)
        : String.format("KawaColor(#%02X%02X%02X%02X)", r, g, b, a);
  }
}
