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

/**
 * Renders a flowing text block composed of multiple styled spans.
 *
 * @author Tobias Dittmann
 */
public final class RichTextElement implements ContentElement {

  // -------------------------------------------------------------------------
  // Span — one styled run of text
  // -------------------------------------------------------------------------

  private final List<Span> spans = new ArrayList<>();

  // -------------------------------------------------------------------------
  // Token — a single word belonging to one span, or an explicit line-break
  // -------------------------------------------------------------------------
  private float lineHeight = 1.4f;

  // -------------------------------------------------------------------------
  // State
  // -------------------------------------------------------------------------
  private TextElement.Align align = TextElement.Align.LEFT;

  /** Adds a pre-built span. */
  public RichTextElement add(Span span) {
    spans.add(span);
    return this;
  }

  /** Creates a plain-text span and adds it. */
  public RichTextElement span(String text) {
    spans.add(new Span().text(text));
    return this;
  }

  // -------------------------------------------------------------------------
  // Fluent API
  // -------------------------------------------------------------------------

  /** Creates a span, applies the supplied configuration, and adds it. */
  public RichTextElement span(String text, java.util.function.Consumer<Span> config) {
    Span s = new Span().text(text);
    config.accept(s);
    spans.add(s);
    return this;
  }

  public RichTextElement lineHeight(float lh) {
    this.lineHeight = lh;
    return this;
  }

  public RichTextElement centerAlign() {
    this.align = TextElement.Align.CENTER;
    return this;
  }

  public RichTextElement rightAlign() {
    this.align = TextElement.Align.RIGHT;
    return this;
  }

  public RichTextElement leftAlign() {
    this.align = TextElement.Align.LEFT;
    return this;
  }

  @Override
  public float measure(LayoutContext context) {
    if (spans.isEmpty()) return 0f;
    int totalChars = spans.stream().mapToInt(s -> s.text.length()).sum();
    float avgSize = (float) spans.stream().mapToDouble(Span::getFontSize).average().orElse(11);
    float charsPerLine = context.width() / (avgSize * 0.52f);
    int lines = Math.max(1, (int) Math.ceil(totalChars / charsPerLine));
    return lines * avgSize * lineHeight;
  }

  @Override
  public void render(LayoutContext context, RenderContext renderCtx) {
    renderSlice(context, renderCtx, 0f, Float.MAX_VALUE);
  }

  // -------------------------------------------------------------------------
  // Element contract
  // -------------------------------------------------------------------------

  @Override
  public void renderSlice(
      LayoutContext context, RenderContext renderCtx, float offsetY, float availableHeight) {
    if (spans.isEmpty()) return;
    try {
      List<List<Token>> lines = buildLines(context.width(), renderCtx);
      float pageH = renderCtx.getPage().getMediaBox().getHeight();
      float cursor = 0f;

      for (List<Token> line : lines) {
        float linePt = maxFontSize(line) * lineHeight;
        float lineBottom = cursor + linePt;

        if (lineBottom <= offsetY) {
          cursor = lineBottom;
          continue;
        }
        if (cursor >= offsetY + availableHeight) break;

        float drawY = context.y() + (cursor - offsetY);
        float baseline = pageH - drawY - maxFontSize(line);

        float xPos = startX(line, context, renderCtx);
        Token prev = null;
        for (Token t : line) {
          PDFont pdFont = resolveFont(t.span(), renderCtx);
          if (prev != null) {
            PDFont prevFont = resolveFont(prev.span(), renderCtx);
            xPos += prevFont.getStringWidth(" ") / 1000f * prev.span().fontSize;
          }
          renderCtx.drawText(t.word(), xPos, baseline, pdFont, t.span().fontSize, t.span().color);
          xPos += renderCtx.textWidth(t.word(), pdFont, t.span().fontSize);
          prev = t;
        }
        cursor = lineBottom;
      }
    } catch (IOException e) {
      throw new KawaRenderException("Failed to render rich text", e);
    }
  }

  private List<List<Token>> buildLines(float maxWidth, RenderContext ctx) throws IOException {
    List<Token> tokens = tokenize();
    List<List<Token>> lines = new ArrayList<>();
    List<Token> current = new ArrayList<>();
    float currentW = 0f;

    for (Token t : tokens) {
      if (t.lineBreak()) {
        lines.add(current);
        current = new ArrayList<>();
        currentW = 0f;
        continue;
      }
      PDFont pdFont = resolveFont(t.span(), ctx);
      float spacePt =
          current.isEmpty()
              ? 0f
              : resolveFont(current.get(current.size() - 1).span(), ctx).getStringWidth(" ")
                  / 1000f
                  * current.get(current.size() - 1).span().fontSize;
      float wordPt = pdFont.getStringWidth(t.word()) / 1000f * t.span().fontSize;

      if (!current.isEmpty() && currentW + spacePt + wordPt > maxWidth) {
        lines.add(current);
        current = new ArrayList<>();
        currentW = 0f;
      }
      current.add(t);
      currentW += (current.size() == 1 ? 0f : spacePt) + wordPt;
    }

    if (!current.isEmpty()) lines.add(current);
    return lines.isEmpty() ? List.of(List.of()) : lines;
  }

  private List<Token> tokenize() {
    List<Token> tokens = new ArrayList<>();
    for (Span span : spans) {
      String[] paragraphs = span.text.split("\n", -1);
      for (int i = 0; i < paragraphs.length; i++) {
        String[] words = paragraphs[i].split("\\s+", -1);
        for (String word : words) {
          if (!word.isEmpty()) tokens.add(new Token(word, span));
        }
        if (i < paragraphs.length - 1) tokens.add(new Token("", span, true));
      }
    }
    return tokens;
  }

  // -------------------------------------------------------------------------
  // Internal — line building
  // -------------------------------------------------------------------------

  private float maxFontSize(List<Token> line) {
    return (float) line.stream().mapToDouble(t -> t.span().fontSize).max().orElse(11);
  }

  private float lineWidth(List<Token> line, RenderContext ctx) throws IOException {
    float total = 0f;
    Token prev = null;
    for (Token t : line) {
      PDFont pdFont = resolveFont(t.span(), ctx);
      if (prev != null) {
        PDFont prevFont = resolveFont(prev.span(), ctx);
        total += prevFont.getStringWidth(" ") / 1000f * prev.span().fontSize;
      }
      total += pdFont.getStringWidth(t.word()) / 1000f * t.span().fontSize;
      prev = t;
    }
    return total;
  }

  private float startX(List<Token> line, LayoutContext ctx, RenderContext renderCtx)
      throws IOException {
    if (align == TextElement.Align.LEFT) return ctx.x();
    float lw = lineWidth(line, renderCtx);
    return switch (align) {
      case CENTER -> ctx.x() + (ctx.width() - lw) / 2f;
      case RIGHT -> ctx.x() + ctx.width() - lw;
      default -> ctx.x();
    };
  }

  private PDFont resolveFont(Span span, RenderContext ctx) throws IOException {
    if (span.font != null) return ctx.resolveFont(span.font, span.weight, span.italic);
    return ctx.getFont(span.weight, span.italic);
  }

  public static final class Span {
    private String text = "";
    private float fontSize = 11f;
    private int weight = FontWeight.REGULAR;
    private boolean italic = false;
    private KawaColor color = KawaColor.BLACK;
    private KawaFont font = null;

    public Span text(String t) {
      this.text = t == null ? "" : t;
      return this;
    }

    public Span fontSize(float s) {
      this.fontSize = s;
      return this;
    }

    /** Sets the font weight to an arbitrary CSS-scale value (100–900). */
    public Span weight(int w) {
      this.weight = w;
      return this;
    }

    /** Shortcut: weight 700. */
    public Span bold() {
      this.weight = FontWeight.BOLD;
      return this;
    }

    public Span bold(boolean b) {
      this.weight = b ? FontWeight.BOLD : FontWeight.REGULAR;
      return this;
    }

    /** Shortcut: weight 600. */
    public Span semiBold() {
      this.weight = FontWeight.SEMI_BOLD;
      return this;
    }

    public Span italic() {
      this.italic = true;
      return this;
    }

    public Span italic(boolean i) {
      this.italic = i;
      return this;
    }

    public Span color(KawaColor c) {
      this.color = c;
      return this;
    }

    public Span font(KawaFont f) {
      this.font = f;
      return this;
    }

    String getText() {
      return text;
    }

    float getFontSize() {
      return fontSize;
    }

    int getWeight() {
      return weight;
    }

    boolean isItalic() {
      return italic;
    }

    boolean isBold() {
      return weight >= FontWeight.BOLD;
    }

    KawaColor getColor() {
      return color;
    }

    KawaFont getFont() {
      return font;
    }
  }

  private record Token(String word, Span span, boolean lineBreak) {
    Token(String word, Span span) {
      this(word, span, false);
    }
  }
}
