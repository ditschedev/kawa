package dev.ditsche.kawa.elements;

import dev.ditsche.kawa.font.FontWeight;
import dev.ditsche.kawa.font.KawaFont;
import dev.ditsche.kawa.layout.LayoutContext;
import dev.ditsche.kawa.renderer.RenderContext;
import dev.ditsche.kawa.style.KawaColor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.font.PDFont;

/** Renders wrapped text with configurable alignment and styling. */
public final class TextElement implements ContentElement {

  private final String text;
  private float fontSize = 11f;
  private int weight = FontWeight.REGULAR;
  private boolean italic = false;
  private KawaColor color = KawaColor.BLACK;
  private Align align = Align.LEFT;
  private float lineHeight = 1.4f;
  private KawaFont font = null; // null → use document default (Helvetica)

  public TextElement(String text) {
    this.text = text == null ? "" : text;
  }

  static List<String> wrapLines(String input, float maxWidth, PDFont font, float fontSize)
      throws IOException {
    List<String> lines = new ArrayList<>();
    String[] paragraphs = input.split("\n", -1);

    for (String para : paragraphs) {
      if (para.isBlank()) {
        lines.add("");
        continue;
      }

      String[] words = para.split(" ", -1);
      StringBuilder sb = new StringBuilder();

      for (String word : words) {
        String candidate = sb.isEmpty() ? word : sb + " " + word;
        float w = font.getStringWidth(candidate) / 1000f * fontSize;
        if (w <= maxWidth) {
          if (!sb.isEmpty()) sb.append(' ');
          sb.append(word);
        } else {
          if (!sb.isEmpty()) {
            lines.add(sb.toString());
            sb.setLength(0);
          }
          sb.append(word);
        }
      }
      if (!sb.isEmpty()) lines.add(sb.toString());
    }

    return lines.isEmpty() ? List.of("") : lines;
  }

  // -------------------------------------------------------------------------
  // Fluent setters
  // -------------------------------------------------------------------------

  public String getText() {
    return text;
  }

  public int getWeight() {
    return weight;
  }

  public boolean isItalic() {
    return italic;
  }

  public boolean isBold() {
    return weight >= FontWeight.BOLD;
  }

  public KawaColor getColor() {
    return color;
  }

  public TextElement font(KawaFont f) {
    this.font = f;
    return this;
  }

  public TextElement fontSize(float size) {
    this.fontSize = size;
    return this;
  }

  /** Sets the font weight to an arbitrary CSS-scale value (100–900). */
  public TextElement weight(int w) {
    this.weight = w;
    return this;
  }

  /** Shortcut: weight 700. */
  public TextElement bold() {
    this.weight = FontWeight.BOLD;
    return this;
  }

  public TextElement bold(boolean b) {
    this.weight = b ? FontWeight.BOLD : FontWeight.REGULAR;
    return this;
  }

  /** Shortcut: weight 600. */
  public TextElement semiBold() {
    this.weight = FontWeight.SEMI_BOLD;
    return this;
  }

  /** Shortcut: weight 500. */
  public TextElement medium() {
    this.weight = FontWeight.MEDIUM;
    return this;
  }

  /** Shortcut: weight 300. */
  public TextElement light() {
    this.weight = FontWeight.LIGHT;
    return this;
  }

  /** Resets weight to 400 and italic to false. */
  public TextElement regular() {
    this.weight = FontWeight.REGULAR;
    this.italic = false;
    return this;
  }

  public TextElement italic() {
    this.italic = true;
    return this;
  }

  public TextElement italic(boolean i) {
    this.italic = i;
    return this;
  }

  public TextElement color(KawaColor c) {
    this.color = c;
    return this;
  }

  public TextElement align(Align a) {
    this.align = a;
    return this;
  }

  public TextElement centerAlign() {
    return align(Align.CENTER);
  }

  public TextElement rightAlign() {
    return align(Align.RIGHT);
  }

  public TextElement lineHeight(float lh) {
    this.lineHeight = lh;
    return this;
  }

  // -------------------------------------------------------------------------
  // Element contract
  // -------------------------------------------------------------------------

  @Override
  public float measure(LayoutContext context) {
    try {
      return lineCount(context.width(), null) * lineHeightPt();
    } catch (IOException e) {
      return lineHeightPt();
    }
  }

  /** Measures the text using the resolved PDF font. */
  public float measureExact(LayoutContext context, RenderContext renderCtx) throws IOException {
    PDFont resolved = resolveFont(renderCtx);
    return wrapLines(text, context.width(), resolved, fontSize).size() * lineHeightPt();
  }

  @Override
  public void render(LayoutContext context, RenderContext renderCtx) {
    renderSlice(context, renderCtx, 0f, Float.MAX_VALUE);
  }

  @Override
  public void renderSlice(
      LayoutContext context, RenderContext renderCtx, float offsetY, float availableHeight) {
    try {
      PDFont resolved = resolveFont(renderCtx);
      float linePt = lineHeightPt();
      float pageHeight = renderCtx.getPage().getMediaBox().getHeight();

      List<String> lines = wrapLines(text, context.width(), resolved, fontSize);

      float cursor = 0f;
      for (String line : lines) {
        float lineBottom = cursor + linePt;

        if (lineBottom <= offsetY) {
          cursor = lineBottom;
          continue;
        }
        if (cursor >= offsetY + availableHeight) break;

        float drawY = context.y() + (cursor - offsetY);
        float xPos = xForLine(line, context, resolved);
        float baseline = pageHeight - drawY - fontSize;

        renderCtx.drawText(line, xPos, baseline, resolved, fontSize, color);
        cursor = lineBottom;
      }
    } catch (IOException e) {
      throw new KawaRenderException("Failed to render text: " + text, e);
    }
  }

  // -------------------------------------------------------------------------
  // Word-wrap algorithm
  // -------------------------------------------------------------------------

  private float lineHeightPt() {
    return fontSize * lineHeight;
  }

  // -------------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------------

  private PDFont resolveFont(RenderContext ctx) throws IOException {
    if (font != null) return ctx.resolveFont(font, weight, italic);
    return ctx.getFont(weight, italic);
  }

  private int lineCount(float width, PDFont f) throws IOException {
    if (f == null) {
      float charsPerLine = width / (fontSize * 0.52f);
      int lines = 0;
      for (String para : text.split("\n", -1)) {
        if (para.isBlank()) {
          lines++;
          continue;
        }
        lines += (int) Math.max(1, Math.ceil(para.length() / charsPerLine));
      }
      return Math.max(1, lines);
    }
    return wrapLines(text, width, f, fontSize).size();
  }

  private float xForLine(String line, LayoutContext ctx, PDFont f) throws IOException {
    float textW = f.getStringWidth(line) / 1000f * fontSize;
    return switch (align) {
      case LEFT -> ctx.x();
      case CENTER -> ctx.x() + (ctx.width() - textW) / 2f;
      case RIGHT -> ctx.x() + ctx.width() - textW;
    };
  }

  public enum Align {
    LEFT,
    CENTER,
    RIGHT
  }
}
