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
import org.apache.pdfbox.pdmodel.interactive.form.PDPushButton;

/**
 * Renders an interactive PDF push button.
 *
 * <p>Each field name should be unique within the document.
 *
 * @author Tobias Dittmann
 */
public final class ButtonElement implements ContentElement {

  private final String fieldName;
  private final String label;

  // -------------------------------------------------------------------------
  // State
  // -------------------------------------------------------------------------
  private float buttonHeight = 24f;
  private float fontSize = 11f;
  private KawaColor backgroundColor = KawaColor.rgb(59, 130, 246);
  private KawaColor textColor = KawaColor.WHITE;
  private KawaColor borderColor = KawaColor.rgb(37, 99, 235);
  private float borderWidth = 1f;

  /**
   * @param fieldName unique name for this AcroForm field within the document
   * @param label visible button text
   */
  public ButtonElement(String fieldName, String label) {
    this.fieldName = fieldName == null ? "button" : fieldName;
    this.label = label == null ? "" : label;
  }

  // -------------------------------------------------------------------------
  // Fluent API
  // -------------------------------------------------------------------------

  /** Sets the height of the button in points. Default: 24. */
  public ButtonElement height(float h) {
    this.buttonHeight = h;
    return this;
  }

  /** Sets the height of the button using an explicit unit. */
  public ButtonElement height(float h, Unit unit) {
    return height(unit.toPoints(h));
  }

  /** Font size for the button label. Default: 11. */
  public ButtonElement fontSize(float s) {
    this.fontSize = s;
    return this;
  }

  /** Background fill color of the button. */
  public ButtonElement backgroundColor(KawaColor c) {
    this.backgroundColor = c;
    return this;
  }

  /** Color of the button label text. */
  public ButtonElement textColor(KawaColor c) {
    this.textColor = c;
    return this;
  }

  /** Color of the button border. */
  public ButtonElement borderColor(KawaColor c) {
    this.borderColor = c;
    return this;
  }

  /** Thickness of the drawn border in points. Default: 1. */
  public ButtonElement borderWidth(float w) {
    this.borderWidth = w;
    return this;
  }

  /** Sets the border width using an explicit unit. */
  public ButtonElement borderWidth(float w, Unit unit) {
    return borderWidth(unit.toPoints(w));
  }

  // -------------------------------------------------------------------------
  // Element contract
  // -------------------------------------------------------------------------

  @Override
  public float measure(LayoutContext context) {
    return buttonHeight;
  }

  @Override
  public void render(LayoutContext context, RenderContext renderCtx) {
    try {
      float pageH = renderCtx.getPage().getMediaBox().getHeight();
      float fieldBottom = pageH - context.y() - buttonHeight;
      float fieldX = context.x();
      float fieldW = context.width();

      renderCtx.drawRect(fieldX, fieldBottom, fieldW, buttonHeight, backgroundColor);

      var cs = renderCtx.getContentStream();
      cs.setStrokingColor(borderColor.toAwtColor());
      cs.setLineWidth(borderWidth);
      cs.addRect(fieldX, fieldBottom, fieldW, buttonHeight);
      cs.stroke();

      if (!label.isEmpty()) {
        float textW = renderCtx.textWidth(label, renderCtx.getRegularFont(), fontSize);
        float textX = fieldX + (fieldW - textW) / 2f;
        float textY = fieldBottom + (buttonHeight - fontSize) / 2f;
        renderCtx.drawText(label, textX, textY, renderCtx.getRegularFont(), fontSize, textColor);
      }

      PDDocument doc = renderCtx.getDocument();
      PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
      if (acroForm == null) {
        acroForm = new PDAcroForm(doc);
        acroForm.setNeedAppearances(true);
        doc.getDocumentCatalog().setAcroForm(acroForm);
      }
      if (acroForm.getDefaultResources() == null) {
        PDResources dr = new PDResources();
        dr.put(COSName.getPDFName("Helv"), new PDType1Font(Standard14Fonts.FontName.HELVETICA));
        acroForm.setDefaultResources(dr);
      }

      PDPushButton button = new PDPushButton(acroForm);
      button.setPartialName(fieldName);

      PDAnnotationWidget widget = button.getWidgets().get(0);
      widget.setRectangle(new PDRectangle(fieldX, fieldBottom, fieldW, buttonHeight));
      widget.setPage(renderCtx.getPage());
      widget.setPrinted(true);

      renderCtx.getPage().getAnnotations().add(widget);
      acroForm.getFields().add(button);

    } catch (IOException e) {
      throw new KawaRenderException("Failed to render button: " + fieldName, e);
    }
  }
}
