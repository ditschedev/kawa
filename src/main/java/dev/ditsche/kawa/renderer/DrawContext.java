package dev.ditsche.kawa.renderer;

import dev.ditsche.kawa.elements.ElementRenderer;
import dev.ditsche.kawa.layout.LayoutContext;
import dev.ditsche.kawa.style.KawaColor;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;

/**
 * Simplified drawing API passed to {@link ElementRenderer} implementations.
 *
 * <p>All coordinates use a top-left origin consistent with {@link LayoutContext}. The conversion to
 * PDFBox's bottom-left coordinate system is handled internally.
 */
public final class DrawContext {

  private final LayoutContext layout;
  private final RenderContext render;
  private final float pageHeight;

  public DrawContext(LayoutContext layout, RenderContext render) {
    this.layout = layout;
    this.render = render;
    this.pageHeight = render.getPage().getMediaBox().getHeight();
  }

  // -------------------------------------------------------------------------
  // Available space (top-left origin)
  // -------------------------------------------------------------------------

  /** Left edge of the available area in points. */
  public float getX() {
    return layout.x();
  }

  /** Top edge of the available area in points. */
  public float getY() {
    return layout.y();
  }

  /** Available width in points. */
  public float getWidth() {
    return layout.width();
  }

  /** Available height in points. May be very large during the measure pass. */
  public float getHeight() {
    return layout.height();
  }

  // -------------------------------------------------------------------------
  // Drawing (coordinates in top-left space)
  // -------------------------------------------------------------------------

  /**
   * Draws a single line of text.
   *
   * @param text the text to draw
   * @param x left edge in points (top-left origin)
   * @param y top edge of the text baseline in points (top-left origin)
   * @param fontSize font size in points
   * @param color text color
   */
  public void drawText(String text, float x, float y, float fontSize, KawaColor color)
      throws IOException {
    render.drawText(text, x, pageHeight - y, render.getRegularFont(), fontSize, color);
  }

  /**
   * Draws a single line of text in black.
   *
   * @param text the text to draw
   * @param x left edge in points
   * @param y top edge of the text baseline in points
   * @param fontSize font size in points
   */
  public void drawText(String text, float x, float y, float fontSize) throws IOException {
    drawText(text, x, y, fontSize, KawaColor.BLACK);
  }

  /**
   * Draws a filled rectangle.
   *
   * @param x left edge in points
   * @param y top edge in points
   * @param width rectangle width in points
   * @param height rectangle height in points
   * @param color fill color
   */
  public void drawRect(float x, float y, float width, float height, KawaColor color)
      throws IOException {
    render.drawRect(x, pageHeight - y - height, width, height, color);
  }

  /**
   * Draws a straight line.
   *
   * @param x1 start x in points
   * @param y1 start y in points
   * @param x2 end x in points
   * @param y2 end y in points
   * @param color line color
   * @param lineWidth line thickness in points
   */
  public void drawLine(float x1, float y1, float x2, float y2, KawaColor color, float lineWidth)
      throws IOException {
    render.drawLine(x1, pageHeight - y1, x2, pageHeight - y2, color, lineWidth);
  }

  /**
   * Measures the width of a string using the default font at the given size.
   *
   * @param text the string to measure
   * @param fontSize font size in points
   * @return width in points
   */
  public float measureText(String text, float fontSize) throws IOException {
    return render.textWidth(text, render.getRegularFont(), fontSize);
  }

  // -------------------------------------------------------------------------
  // Font access
  // -------------------------------------------------------------------------

  /** Returns the default Helvetica regular font. */
  public PDFont getDefaultFont() {
    return render.getRegularFont();
  }

  /** Returns the default Helvetica bold font. */
  public PDFont getBoldFont() {
    return render.getBoldFont();
  }

  // -------------------------------------------------------------------------
  // Escape hatches
  // -------------------------------------------------------------------------

  /**
   * Returns the raw PDFBox content stream for operations not covered by the helper methods above.
   * Coordinates in PDFBox use a bottom-left origin.
   */
  public PDPageContentStream getContentStream() {
    return render.getContentStream();
  }

  /**
   * Returns the underlying {@link RenderContext} for full access to fonts, the PDDocument, and the
   * current page.
   */
  public RenderContext getRenderContext() {
    return render;
  }
}
