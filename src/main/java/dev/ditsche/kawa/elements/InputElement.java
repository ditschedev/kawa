package dev.ditsche.kawa.elements;

import dev.ditsche.kawa.layout.LayoutContext;
import dev.ditsche.kawa.renderer.RenderContext;
import dev.ditsche.kawa.style.KawaColor;
import dev.ditsche.kawa.units.Unit;
import java.io.IOException;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;

/**
 * Renders an interactive AcroForm PDF text input field.
 *
 *
 * @author Tobias Dittmann
 */
public final class InputElement implements ContentElement {

  private final String fieldName;

  // -------------------------------------------------------------------------
  // State
  // -------------------------------------------------------------------------
  private String label = null;
  private String value = "";
  private float fieldHeight = 22f;
  private float fontSize = 11f;
  private boolean multiline = false;
  private KawaColor borderColor = KawaColor.rgb(180, 180, 180);
  private KawaColor backgroundColor = KawaColor.WHITE;
  private float borderWidth = 1f;
  private Style style = Style.BOX;
  private float labelFontSize = 10f;
  private KawaColor labelColor = KawaColor.rgb(80, 80, 80);
  private final float labelGap = 4f;

  /**
   * @param fieldName unique name for this AcroForm field within the document
   */
  public InputElement(String fieldName) {
    this.fieldName = fieldName == null ? "field" : fieldName;
  }

  // -------------------------------------------------------------------------
  // Constructor
  // -------------------------------------------------------------------------

  /** Draws a small label above the input box. */
  public InputElement label(String l) {
    this.label = l;
    return this;
  }

  // -------------------------------------------------------------------------
  // Fluent API
  // -------------------------------------------------------------------------

  /** Pre-fills the field with the given text. */
  public InputElement value(String v) {
    this.value = v == null ? "" : v;
    return this;
  }

  /** Sets the height of the input box in points. Default: 22. */
  public InputElement height(float h) {
    this.fieldHeight = h;
    return this;
  }

  /**
   * Sets the height of the input box using an explicit unit.
   *
   * @param h the height value
   * @return this input element
   */
  public InputElement height(float h, Unit unit) {
    return height(unit.toPoints(h));
  }

  /** Font size for text typed into the field. Default: 11. */
  public InputElement fontSize(float s) {
    this.fontSize = s;
    return this;
  }

  /** Switches the field to multiline mode, keeping the current height. */
  public InputElement multiline() {
    this.multiline = true;
    return this;
  }

  /** Switches to multiline mode and sets the field height to {@code h} points. */
  public InputElement multiline(float h) {
    this.multiline = true;
    this.fieldHeight = h;
    return this;
  }

  /**
   * Switches to multiline mode and sets the field height using an explicit unit.
   *
   * @param h the height value
   * @return this input element
   */
  public InputElement multiline(float h, Unit unit) {
    return multiline(unit.toPoints(h));
  }

  /** Color of the field border. */
  public InputElement borderColor(KawaColor c) {
    this.borderColor = c;
    return this;
  }

  /** Background fill color of the field. */
  public InputElement backgroundColor(KawaColor c) {
    this.backgroundColor = c;
    return this;
  }

  /** Thickness of the drawn border in points. Default: 1. */
  public InputElement borderWidth(float w) {
    this.borderWidth = w;
    return this;
  }

  /**
   * Sets the border width using an explicit unit.
   *
   * @param w the border width
   * @return this input element
   */
  public InputElement borderWidth(float w, Unit unit) {
    return borderWidth(unit.toPoints(w));
  }

  /** Sets the border style (BOX or UNDERLINE). */
  public InputElement style(Style s) {
    this.style = s;
    return this;
  }

  /** Shorthand for {@code style(Style.UNDERLINE)}. */
  public InputElement underline() {
    this.style = Style.UNDERLINE;
    return this;
  }

  /** Font size of the label text. Default: 10. */
  public InputElement labelFontSize(float s) {
    this.labelFontSize = s;
    return this;
  }

  /** Color of the label text. */
  public InputElement labelColor(KawaColor c) {
    this.labelColor = c;
    return this;
  }

  @Override
  public float measure(LayoutContext context) {
    float total = fieldHeight;
    if (label != null) total += labelFontSize * 1.3f + labelGap;
    return total;
  }

  // -------------------------------------------------------------------------
  // Element contract
  // -------------------------------------------------------------------------

  @Override
  public void render(LayoutContext context, RenderContext renderCtx) {
    try {
      float pageH = renderCtx.getPage().getMediaBox().getHeight();
      float cursorY = context.y();

      // --- Optional label ---
      if (label != null) {
        float baselineY = pageH - cursorY - labelFontSize;
        renderCtx.drawText(
            label, context.x(), baselineY, renderCtx.getRegularFont(), labelFontSize, labelColor);
        cursorY += labelFontSize * 1.3f + labelGap;
      }

      // PDF coordinate origin is bottom-left
      float fieldBottom = pageH - cursorY - fieldHeight;
      float fieldX = context.x();
      float fieldW = context.width();

      // --- Background fill ---
      renderCtx.drawRect(fieldX, fieldBottom, fieldW, fieldHeight, backgroundColor);

      // --- Border ---
      var cs = renderCtx.getContentStream();
      cs.setStrokingColor(borderColor.toAwtColor());
      cs.setLineWidth(borderWidth);
      if (style == Style.UNDERLINE) {
        cs.moveTo(fieldX, fieldBottom);
        cs.lineTo(fieldX + fieldW, fieldBottom);
        cs.stroke();
      } else {
        cs.addRect(fieldX, fieldBottom, fieldW, fieldHeight);
        cs.stroke();
      }

      // --- AcroForm widget ---
      PDDocument doc = renderCtx.getDocument();
      PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
      if (acroForm == null) {
        acroForm = new PDAcroForm(doc);
        acroForm.setNeedAppearances(true);
        doc.getDocumentCatalog().setAcroForm(acroForm);
      }

      // Ensure Helvetica is available as "Helv" in the default resource dict
      if (acroForm.getDefaultResources() == null) {
        PDResources dr = new PDResources();
        dr.put(COSName.getPDFName("Helv"), new PDType1Font(Standard14Fonts.FontName.HELVETICA));
        acroForm.setDefaultResources(dr);
      }

      PDTextField field = new PDTextField(acroForm);
      field.setPartialName(fieldName);
      field.setDefaultAppearance("/Helv " + (int) fontSize + " Tf 0 g");
      if (multiline) field.setMultiline(true);
      if (!value.isEmpty()) field.setValue(value);

      PDAnnotationWidget widget = field.getWidgets().get(0);
      widget.setRectangle(new PDRectangle(fieldX, fieldBottom, fieldW, fieldHeight));
      widget.setPage(renderCtx.getPage());
      widget.setPrinted(true);

      renderCtx.getPage().getAnnotations().add(widget);
      acroForm.getFields().add(field);

    } catch (IOException e) {
      throw new KawaRenderException("Failed to render input field: " + fieldName, e);
    }
  }

  /** Visual border style of the input box. */
  public enum Style {
    /** Full rectangular border on all four sides. */
    BOX,
    /** Only a bottom border — minimal, signature-line look. */
    UNDERLINE
  }
}
