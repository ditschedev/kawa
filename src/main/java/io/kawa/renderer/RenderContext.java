package io.kawa.renderer;

import io.kawa.font.FontRegistry;
import io.kawa.font.KawaFont;
import io.kawa.style.KawaColor;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

/**
 * Wraps the PDFBox content stream and provides helper drawing methods. Passed down to elements
 * during the render phase.
 */
public class RenderContext implements AutoCloseable {

  private final PDDocument document;
  private final PDPage page;
  private final FontRegistry fontRegistry;
  // Fallback fonts (Standard 14 — always available, no embedding needed)
  private final PDFont fontRegular;
  private final PDFont fontBold;
  private final PDFont fontItalic;
  private final PDFont fontBoldItalic;
  private final PDPageContentStream contentStream;

  public RenderContext(PDDocument document, PDPage page, FontRegistry fontRegistry)
      throws IOException {
    this.document = document;
    this.page = page;
    this.fontRegistry = fontRegistry;
    this.contentStream =
        new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);
    this.fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    this.fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    this.fontItalic = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);
    this.fontBoldItalic = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD_OBLIQUE);
  }

  // -------------------------------------------------------------------------
  // Text
  // -------------------------------------------------------------------------

  /**
   * Draws a single line of text at (x, y) in PDF coordinate space. Note: PDFBox uses bottom-left
   * origin; callers should convert from top-left.
   */
  public void drawText(String text, float x, float y, PDFont font, float fontSize, KawaColor color)
      throws IOException {
    contentStream.beginText();
    contentStream.setFont(font, fontSize);
    contentStream.setNonStrokingColor(color.toAwtColor());
    contentStream.newLineAtOffset(x, y);
    contentStream.showText(text);
    contentStream.endText();
  }

  /** Measures the width of a string at the given font size. */
  public float textWidth(String text, PDFont font, float fontSize) throws IOException {
    return font.getStringWidth(text) / 1000f * fontSize;
  }

  // -------------------------------------------------------------------------
  // Shapes
  // -------------------------------------------------------------------------

  public void drawRect(float x, float y, float width, float height, KawaColor fillColor)
      throws IOException {
    contentStream.setNonStrokingColor(fillColor.toAwtColor());
    contentStream.addRect(x, y, width, height);
    contentStream.fill();
  }

  public void drawLine(float x1, float y1, float x2, float y2, KawaColor color, float lineWidth)
      throws IOException {
    contentStream.setStrokingColor(color.toAwtColor());
    contentStream.setLineWidth(lineWidth);
    contentStream.moveTo(x1, y1);
    contentStream.lineTo(x2, y2);
    contentStream.stroke();
  }

  // -------------------------------------------------------------------------
  // Font access
  // -------------------------------------------------------------------------

  /**
   * Resolves the {@link PDFont} for a custom {@link KawaFont} at the given weight and italic axis.
   * The nearest registered weight is selected automatically.
   *
   * @throws IOException if the font bytes cannot be loaded or embedded
   */
  public PDFont resolveFont(KawaFont kawaFont, int weight, boolean italic) throws IOException {
    return fontRegistry.resolve(kawaFont, weight, italic);
  }

  /**
   * Returns the built-in Helvetica fallback for the given weight and italic axis. Weights &ge; 700
   * map to the bold variant.
   */
  public PDFont getFont(int weight, boolean italic) {
    boolean heavy = weight >= 700;
    if (heavy && italic) return fontBoldItalic;
    if (heavy) return fontBold;
    if (italic) return fontItalic;
    return fontRegular;
  }

  public PDFont getRegularFont() {
    return fontRegular;
  }

  public PDFont getBoldFont() {
    return fontBold;
  }

  public PDFont getItalicFont() {
    return fontItalic;
  }

  public PDFont getBoldItalicFont() {
    return fontBoldItalic;
  }

  public PDDocument getDocument() {
    return document;
  }

  public PDPage getPage() {
    return page;
  }

  public PDPageContentStream getContentStream() {
    return contentStream;
  }

  // -------------------------------------------------------------------------
  // Lifecycle
  // -------------------------------------------------------------------------

  @Override
  public void close() throws IOException {
    contentStream.close();
  }
}
