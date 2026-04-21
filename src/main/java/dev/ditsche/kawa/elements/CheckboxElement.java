package dev.ditsche.kawa.elements;

import dev.ditsche.kawa.layout.LayoutContext;
import dev.ditsche.kawa.renderer.RenderContext;
import dev.ditsche.kawa.style.KawaColor;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

/**
 * Renders an interactive PDF checkbox field with an optional text label.
 *
 * <p>Each field name should be unique within the document.
 *
 * @author Tobias Dittmann
 */
public final class CheckboxElement implements ContentElement {

  private final String fieldName;

  // -------------------------------------------------------------------------
  // State
  // -------------------------------------------------------------------------
  private String label = null;
  private boolean checked = false;
  private float size = 14f;
  private float labelFontSize = 11f;
  private KawaColor labelColor = KawaColor.rgb(40, 40, 40);
  private KawaColor borderColor = KawaColor.rgb(180, 180, 180);
  private KawaColor backgroundColor = KawaColor.WHITE;
  private KawaColor checkColor = KawaColor.rgb(30, 30, 30);
  private final float labelGap = 6f;

  /**
   * @param fieldName unique name for this AcroForm field within the document
   */
  public CheckboxElement(String fieldName) {
    this.fieldName = fieldName == null ? "checkbox" : fieldName;
  }

  // -------------------------------------------------------------------------
  // Fluent API
  // -------------------------------------------------------------------------

  /** Draws a label to the right of the checkbox. */
  public CheckboxElement label(String l) {
    this.label = l;
    return this;
  }

  /** Pre-checks the checkbox. */
  public CheckboxElement checked() {
    this.checked = true;
    return this;
  }

  /** Sets the checked state explicitly. */
  public CheckboxElement checked(boolean c) {
    this.checked = c;
    return this;
  }

  /** Size of the checkbox square in points. Default: 14. */
  public CheckboxElement size(float s) {
    this.size = s;
    return this;
  }

  /** Font size of the label text. Default: 11. */
  public CheckboxElement labelFontSize(float s) {
    this.labelFontSize = s;
    return this;
  }

  /** Color of the label text. */
  public CheckboxElement labelColor(KawaColor c) {
    this.labelColor = c;
    return this;
  }

  /** Color of the checkbox border. */
  public CheckboxElement borderColor(KawaColor c) {
    this.borderColor = c;
    return this;
  }

  /** Background fill color of the checkbox. */
  public CheckboxElement backgroundColor(KawaColor c) {
    this.backgroundColor = c;
    return this;
  }

  /** Color of the check mark drawn when checked. */
  public CheckboxElement checkColor(KawaColor c) {
    this.checkColor = c;
    return this;
  }

  // -------------------------------------------------------------------------
  // Element contract
  // -------------------------------------------------------------------------

  @Override
  public float measure(LayoutContext context) {
    float labelH = (label != null) ? labelFontSize * 1.3f : 0f;
    return Math.max(size, labelH);
  }

  @Override
  public void render(LayoutContext context, RenderContext renderCtx) {
    try {
      float pageH = renderCtx.getPage().getMediaBox().getHeight();
      float rowH = measure(context);
      float checkBottom = pageH - context.y() - rowH + (rowH - size) / 2f;
      float checkX = context.x();

      renderCtx.drawRect(checkX, checkBottom, size, size, backgroundColor);

      var cs = renderCtx.getContentStream();
      cs.setStrokingColor(borderColor.toAwtColor());
      cs.setLineWidth(1f);
      cs.addRect(checkX, checkBottom, size, size);
      cs.stroke();

      if (checked) {
        float pad = size * 0.2f;
        cs.setStrokingColor(checkColor.toAwtColor());
        cs.setLineWidth(1.5f);
        // Draw X mark
        cs.moveTo(checkX + pad, checkBottom + pad);
        cs.lineTo(checkX + size - pad, checkBottom + size - pad);
        cs.stroke();
        cs.moveTo(checkX + size - pad, checkBottom + pad);
        cs.lineTo(checkX + pad, checkBottom + size - pad);
        cs.stroke();
      }

      if (label != null) {
        float textY = checkBottom + (size - labelFontSize) / 2f;
        renderCtx.drawText(
            label,
            checkX + size + labelGap,
            textY,
            renderCtx.getRegularFont(),
            labelFontSize,
            labelColor);
      }

      PDDocument doc = renderCtx.getDocument();
      PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
      if (acroForm == null) {
        acroForm = new PDAcroForm(doc);
        acroForm.setNeedAppearances(true);
        doc.getDocumentCatalog().setAcroForm(acroForm);
      }
      PDResources dr = acroForm.getDefaultResources();
      if (dr == null) {
        dr = new PDResources();
        acroForm.setDefaultResources(dr);
      }
      dr.put(COSName.getPDFName("Helv"), new PDType1Font(Standard14Fonts.FontName.HELVETICA));
      dr.put(COSName.getPDFName("ZaDb"), new PDType1Font(Standard14Fonts.FontName.ZAPF_DINGBATS));

      PDCheckBox checkField = new PDCheckBox(acroForm);
      checkField.setPartialName(fieldName);

      PDAnnotationWidget widget = checkField.getWidgets().get(0);
      widget.setRectangle(new PDRectangle(checkX, checkBottom, size, size));
      widget.setPage(renderCtx.getPage());
      widget.setPrinted(true);
      widget.setAppearanceState(checked ? checkField.getOnValue() : "Off");

      renderCtx.getPage().getAnnotations().add(widget);
      acroForm.getFields().add(checkField);
      if (checked) checkField.check();
      acroForm.refreshAppearances(List.<PDField>of(checkField));

    } catch (IOException e) {
      throw new KawaRenderException("Failed to render checkbox: " + fieldName, e);
    }
  }
}
