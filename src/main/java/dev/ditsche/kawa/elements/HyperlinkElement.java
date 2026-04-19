package dev.ditsche.kawa.elements;

import dev.ditsche.kawa.font.FontWeight;
import dev.ditsche.kawa.font.KawaFont;
import dev.ditsche.kawa.layout.LayoutContext;
import dev.ditsche.kawa.renderer.RenderContext;
import dev.ditsche.kawa.style.KawaColor;
import java.io.IOException;
import java.util.List;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;

/** Renders clickable text with a PDF URI action. */
public final class HyperlinkElement implements ContentElement {

  private final String uri;
  private final String text;

  private float fontSize = 11f;
  private int weight = FontWeight.REGULAR;
  private boolean italic = false;
  private KawaColor color = KawaColor.rgb(0, 70, 180);
  private KawaFont font = null;
  private float lineHeight = 1.4f;

  public HyperlinkElement(String uri, String text) {
    this.uri = uri == null ? "" : uri;
    this.text = text == null ? "" : text;
  }

  public HyperlinkElement fontSize(float s) {
    this.fontSize = s;
    return this;
  }

  /** Sets the font weight to an arbitrary CSS-scale value (100–900). */
  public HyperlinkElement weight(int w) {
    this.weight = w;
    return this;
  }

  /** Shortcut: weight 700. */
  public HyperlinkElement bold() {
    this.weight = FontWeight.BOLD;
    return this;
  }

  public HyperlinkElement bold(boolean b) {
    this.weight = b ? FontWeight.BOLD : FontWeight.REGULAR;
    return this;
  }

  /** Shortcut: weight 600. */
  public HyperlinkElement semiBold() {
    this.weight = FontWeight.SEMI_BOLD;
    return this;
  }

  public HyperlinkElement italic() {
    this.italic = true;
    return this;
  }

  public HyperlinkElement italic(boolean i) {
    this.italic = i;
    return this;
  }

  public HyperlinkElement color(KawaColor c) {
    this.color = c;
    return this;
  }

  public HyperlinkElement font(KawaFont f) {
    this.font = f;
    return this;
  }

  public HyperlinkElement lineHeight(float lh) {
    this.lineHeight = lh;
    return this;
  }

  @Override
  public float measure(LayoutContext context) {
    float charsPerLine = context.width() / (fontSize * 0.52f);
    int lines = Math.max(1, (int) Math.ceil(text.length() / charsPerLine));
    return lines * fontSize * lineHeight;
  }

  @Override
  public void render(LayoutContext context, RenderContext renderCtx) {
    renderSlice(context, renderCtx, 0f, Float.MAX_VALUE);
  }

  @Override
  public void renderSlice(
      LayoutContext context, RenderContext renderCtx, float offsetY, float availableHeight) {
    try {
      PDFont pdFont =
          font != null
              ? renderCtx.resolveFont(font, weight, italic)
              : renderCtx.getFont(weight, italic);

      List<String> lines = TextElement.wrapLines(text, context.width(), pdFont, fontSize);
      float pageH = renderCtx.getPage().getMediaBox().getHeight();
      float linePt = fontSize * lineHeight;
      float cursor = 0f;

      for (String line : lines) {
        float lineBottom = cursor + linePt;
        if (lineBottom <= offsetY) {
          cursor = lineBottom;
          continue;
        }
        if (cursor >= offsetY + availableHeight) break;

        float drawY = context.y() + (cursor - offsetY);
        float baseline = pageH - drawY - fontSize;
        float textW = renderCtx.textWidth(line, pdFont, fontSize);

        renderCtx.drawText(line, context.x(), baseline, pdFont, fontSize, color);

        if (!uri.isEmpty()) {
          float pdfY = pageH - drawY - linePt;
          PDAnnotationLink link = new PDAnnotationLink();
          link.setRectangle(new PDRectangle(context.x(), pdfY, textW, linePt));

          PDBorderStyleDictionary border = new PDBorderStyleDictionary();
          border.setWidth(0);
          link.setBorderStyle(border);

          PDActionURI action = new PDActionURI();
          action.setURI(uri);
          link.setAction(action);

          renderCtx.getPage().getAnnotations().add(link);
        }

        cursor = lineBottom;
      }
    } catch (IOException e) {
      throw new KawaRenderException("Failed to render hyperlink: " + text, e);
    }
  }
}
