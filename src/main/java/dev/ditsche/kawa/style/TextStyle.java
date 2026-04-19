package dev.ditsche.kawa.style;

import dev.ditsche.kawa.elements.TextElement;
import dev.ditsche.kawa.font.FontWeight;
import java.util.function.Consumer;

/** Collects reusable text formatting options. */
public class TextStyle {

  private Float fontSize;
  private Integer weight;
  private Boolean italic;
  private KawaColor color;
  private Float lineHeight;

  public TextStyle() {}

  /** Factory: creates and configures a TextStyle in one expression. */
  public static TextStyle of(Consumer<TextStyle> config) {
    TextStyle ts = new TextStyle();
    config.accept(ts);
    return ts;
  }

  // -------------------------------------------------------------------------
  // Fluent setters
  // -------------------------------------------------------------------------

  public TextStyle fontSize(float s) {
    this.fontSize = s;
    return this;
  }

  public TextStyle color(KawaColor c) {
    this.color = c;
    return this;
  }

  public TextStyle lineHeight(float lh) {
    this.lineHeight = lh;
    return this;
  }

  /** Sets the font weight to an arbitrary CSS-scale value (100–900). */
  public TextStyle weight(int w) {
    this.weight = w;
    return this;
  }

  /** Shortcut: weight 700. */
  public TextStyle bold() {
    this.weight = FontWeight.BOLD;
    return this;
  }

  /** Shortcut: weight 600. */
  public TextStyle semiBold() {
    this.weight = FontWeight.SEMI_BOLD;
    return this;
  }

  /** Shortcut: weight 500. */
  public TextStyle medium() {
    this.weight = FontWeight.MEDIUM;
    return this;
  }

  public TextStyle italic() {
    this.italic = true;
    return this;
  }

  public TextStyle italic(boolean i) {
    this.italic = i;
    return this;
  }

  // -------------------------------------------------------------------------
  // Getters
  // -------------------------------------------------------------------------

  public Float getFontSize() {
    return fontSize;
  }

  public Integer getWeight() {
    return weight;
  }

  public Boolean getItalic() {
    return italic;
  }

  public KawaColor getColor() {
    return color;
  }

  public Float getLineHeight() {
    return lineHeight;
  }

  // -------------------------------------------------------------------------
  // Application
  // -------------------------------------------------------------------------

  /** Applies all configured values to the given text element. */
  public void applyTo(TextElement element) {
    if (fontSize != null) element.fontSize(fontSize);
    if (weight != null) element.weight(weight);
    if (italic != null) element.italic(italic);
    if (color != null) element.color(color);
    if (lineHeight != null) element.lineHeight(lineHeight);
  }
}
