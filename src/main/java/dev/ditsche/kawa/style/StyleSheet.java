package dev.ditsche.kawa.style;

import dev.ditsche.kawa.units.Unit;
import java.util.function.Consumer;

/** Collects reusable layout and text styling options. */
public class StyleSheet {

  // Background
  private KawaColor background = null;

  // Padding per side (points)
  private float paddingTop = 0f;
  private float paddingRight = 0f;
  private float paddingBottom = 0f;
  private float paddingLeft = 0f;

  // Border widths per side (0 = no border)
  private float borderTopWidth = 0f;
  private float borderRightWidth = 0f;
  private float borderBottomWidth = 0f;
  private float borderLeftWidth = 0f;

  // Border colors per side (null = no border)
  private KawaColor borderTopColor = null;
  private KawaColor borderRightColor = null;
  private KawaColor borderBottomColor = null;
  private KawaColor borderLeftColor = null;

  // Optional text style
  private TextStyle textStyle = null;

  public StyleSheet() {}

  /** Factory: creates and configures a StyleSheet in one expression. */
  public static StyleSheet of(Consumer<StyleSheet> config) {
    StyleSheet ss = new StyleSheet();
    config.accept(ss);
    return ss;
  }

  // -------------------------------------------------------------------------
  // Background
  // -------------------------------------------------------------------------

  public StyleSheet background(KawaColor c) {
    this.background = c;
    return this;
  }

  // -------------------------------------------------------------------------
  // Padding
  // -------------------------------------------------------------------------

  /** Sets all four padding sides in points. */
  public StyleSheet padding(float all) {
    paddingTop = paddingRight = paddingBottom = paddingLeft = all;
    return this;
  }

  /** Sets all four padding sides in the specified unit. */
  public StyleSheet padding(float all, Unit unit) {
    return padding(unit.toPoints(all));
  }

  /** Sets horizontal (left + right) padding in points. */
  public StyleSheet paddingH(float h) {
    paddingLeft = paddingRight = h;
    return this;
  }

  public StyleSheet paddingH(float h, Unit unit) {
    return paddingH(unit.toPoints(h));
  }

  /** Sets vertical (top + bottom) padding in points. */
  public StyleSheet paddingV(float v) {
    paddingTop = paddingBottom = v;
    return this;
  }

  public StyleSheet paddingV(float v, Unit unit) {
    return paddingV(unit.toPoints(v));
  }

  public StyleSheet paddingTop(float v) {
    paddingTop = v;
    return this;
  }

  public StyleSheet paddingTop(float v, Unit unit) {
    return paddingTop(unit.toPoints(v));
  }

  public StyleSheet paddingRight(float v) {
    paddingRight = v;
    return this;
  }

  public StyleSheet paddingRight(float v, Unit unit) {
    return paddingRight(unit.toPoints(v));
  }

  public StyleSheet paddingBottom(float v) {
    paddingBottom = v;
    return this;
  }

  public StyleSheet paddingBottom(float v, Unit unit) {
    return paddingBottom(unit.toPoints(v));
  }

  public StyleSheet paddingLeft(float v) {
    paddingLeft = v;
    return this;
  }

  public StyleSheet paddingLeft(float v, Unit unit) {
    return paddingLeft(unit.toPoints(v));
  }

  // -------------------------------------------------------------------------
  // Borders
  // -------------------------------------------------------------------------

  /** Sets all four borders to the same width (points) and color. */
  public StyleSheet border(float width, KawaColor color) {
    return borderTop(width, color)
        .borderRight(width, color)
        .borderBottom(width, color)
        .borderLeft(width, color);
  }

  /** Sets all four borders to the same width in the specified unit and color. */
  public StyleSheet border(float width, Unit unit, KawaColor color) {
    return border(unit.toPoints(width), color);
  }

  public StyleSheet borderTop(float width, KawaColor color) {
    borderTopWidth = width;
    borderTopColor = color;
    return this;
  }

  public StyleSheet borderTop(float width, Unit unit, KawaColor color) {
    return borderTop(unit.toPoints(width), color);
  }

  public StyleSheet borderRight(float width, KawaColor color) {
    borderRightWidth = width;
    borderRightColor = color;
    return this;
  }

  public StyleSheet borderRight(float width, Unit unit, KawaColor color) {
    return borderRight(unit.toPoints(width), color);
  }

  public StyleSheet borderBottom(float width, KawaColor color) {
    borderBottomWidth = width;
    borderBottomColor = color;
    return this;
  }

  public StyleSheet borderBottom(float width, Unit unit, KawaColor color) {
    return borderBottom(unit.toPoints(width), color);
  }

  public StyleSheet borderLeft(float width, KawaColor color) {
    borderLeftWidth = width;
    borderLeftColor = color;
    return this;
  }

  public StyleSheet borderLeft(float width, Unit unit, KawaColor color) {
    return borderLeft(unit.toPoints(width), color);
  }

  // -------------------------------------------------------------------------
  // Text style
  // -------------------------------------------------------------------------

  /** Configures (or creates) the nested {@link TextStyle}. */
  public StyleSheet textStyle(Consumer<TextStyle> config) {
    if (textStyle == null) textStyle = new TextStyle();
    config.accept(textStyle);
    return this;
  }

  // -------------------------------------------------------------------------
  // Getters
  // -------------------------------------------------------------------------

  public KawaColor getBackground() {
    return background;
  }

  public float getPaddingTop() {
    return paddingTop;
  }

  public float getPaddingRight() {
    return paddingRight;
  }

  public float getPaddingBottom() {
    return paddingBottom;
  }

  public float getPaddingLeft() {
    return paddingLeft;
  }

  public float totalPaddingH() {
    return paddingLeft + paddingRight;
  }

  public float totalPaddingV() {
    return paddingTop + paddingBottom;
  }

  public float getBorderTopWidth() {
    return borderTopWidth;
  }

  public float getBorderRightWidth() {
    return borderRightWidth;
  }

  public float getBorderBottomWidth() {
    return borderBottomWidth;
  }

  public float getBorderLeftWidth() {
    return borderLeftWidth;
  }

  public KawaColor getBorderTopColor() {
    return borderTopColor;
  }

  public KawaColor getBorderRightColor() {
    return borderRightColor;
  }

  public KawaColor getBorderBottomColor() {
    return borderBottomColor;
  }

  public KawaColor getBorderLeftColor() {
    return borderLeftColor;
  }

  public TextStyle getTextStyle() {
    return textStyle;
  }
}
