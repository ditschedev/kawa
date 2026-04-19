package dev.ditsche.kawa.elements;

import dev.ditsche.kawa.layout.LayoutContext;
import dev.ditsche.kawa.renderer.RenderContext;
import dev.ditsche.kawa.style.KawaColor;
import dev.ditsche.kawa.units.Unit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDComboBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

/**
 * Renders an interactive PDF combo box (dropdown) field.
 *
 * <p>Users pick one item from a drop-down list. Each field name should be unique within the
 * document.
 */
public final class ComboboxElement implements ContentElement {

  private final String fieldName;
  private final List<String> options;

  // -------------------------------------------------------------------------
  // State
  // -------------------------------------------------------------------------
  private String selected = null;
  private float fieldHeight = 22f;
  private float fontSize = 11f;
  private KawaColor borderColor = KawaColor.rgb(180, 180, 180);
  private KawaColor backgroundColor = KawaColor.WHITE;
  private KawaColor textColor = KawaColor.rgb(40, 40, 40);
  private float borderWidth = 1f;
  private String label = null;
  private float labelFontSize = 10f;
  private KawaColor labelColor = KawaColor.rgb(80, 80, 80);
  private final float labelGap = 4f;

  /**
   * @param fieldName unique name for this AcroForm field within the document
   * @param options list of selectable option labels
   */
  public ComboboxElement(String fieldName, List<String> options) {
    this.fieldName = fieldName == null ? "combo" : fieldName;
    this.options = options != null ? new ArrayList<>(options) : new ArrayList<>();
  }

  // -------------------------------------------------------------------------
  // Fluent API
  // -------------------------------------------------------------------------

  /** Pre-selects the option matching this label. */
  public ComboboxElement selected(String s) {
    this.selected = s;
    return this;
  }

  /** Sets the height of the combo box in points. Default: 22. */
  public ComboboxElement height(float h) {
    this.fieldHeight = h;
    return this;
  }

  /** Sets the height of the combo box using an explicit unit. */
  public ComboboxElement height(float h, Unit unit) {
    return height(unit.toPoints(h));
  }

  /** Font size for the selected item text. Default: 11. */
  public ComboboxElement fontSize(float s) {
    this.fontSize = s;
    return this;
  }

  /** Color of the field border. */
  public ComboboxElement borderColor(KawaColor c) {
    this.borderColor = c;
    return this;
  }

  /** Background fill color of the combo box. */
  public ComboboxElement backgroundColor(KawaColor c) {
    this.backgroundColor = c;
    return this;
  }

  /** Color of the displayed text inside the field. */
  public ComboboxElement textColor(KawaColor c) {
    this.textColor = c;
    return this;
  }

  /** Thickness of the drawn border in points. Default: 1. */
  public ComboboxElement borderWidth(float w) {
    this.borderWidth = w;
    return this;
  }

  /** Sets the border width using an explicit unit. */
  public ComboboxElement borderWidth(float w, Unit unit) {
    return borderWidth(unit.toPoints(w));
  }

  /** Draws a small label above the combo box. */
  public ComboboxElement label(String l) {
    this.label = l;
    return this;
  }

  /** Font size of the label text. Default: 10. */
  public ComboboxElement labelFontSize(float s) {
    this.labelFontSize = s;
    return this;
  }

  /** Color of the label text. */
  public ComboboxElement labelColor(KawaColor c) {
    this.labelColor = c;
    return this;
  }

  // -------------------------------------------------------------------------
  // Element contract
  // -------------------------------------------------------------------------

  @Override
  public float measure(LayoutContext context) {
    float total = fieldHeight;
    if (label != null) total += labelFontSize * 1.3f + labelGap;
    return total;
  }

  @Override
  public void render(LayoutContext context, RenderContext renderCtx) {
    try {
      float pageH = renderCtx.getPage().getMediaBox().getHeight();
      float cursorY = context.y();

      if (label != null) {
        float baselineY = pageH - cursorY - labelFontSize;
        renderCtx.drawText(
            label,
            context.x(),
            baselineY,
            renderCtx.getRegularFont(),
            labelFontSize,
            labelColor);
        cursorY += labelFontSize * 1.3f + labelGap;
      }

      float fieldBottom = pageH - cursorY - fieldHeight;
      float fieldX = context.x();
      float fieldW = context.width();

      renderCtx.drawRect(fieldX, fieldBottom, fieldW, fieldHeight, backgroundColor);

      // Dropdown arrow indicator: gray panel + chevron lines (no fill operator)
      float arrowW = 18f;
      float arrowX = fieldX + fieldW - arrowW;
      renderCtx.drawRect(arrowX, fieldBottom, arrowW, fieldHeight, KawaColor.rgb(240, 240, 240));

      var cs = renderCtx.getContentStream();
      cs.setStrokingColor(KawaColor.rgb(180, 180, 180).toAwtColor());
      cs.setLineWidth(1f);
      cs.moveTo(arrowX, fieldBottom + 1f);
      cs.lineTo(arrowX, fieldBottom + fieldHeight - 1f);
      cs.stroke();

      float arrCx = arrowX + arrowW / 2f;
      float arrCy = fieldBottom + fieldHeight / 2f;
      cs.setStrokingColor(KawaColor.rgb(100, 100, 100).toAwtColor());
      cs.setLineWidth(1.2f);
      cs.moveTo(arrCx - 3.5f, arrCy + 1.5f);
      cs.lineTo(arrCx, arrCy - 1.5f);
      cs.lineTo(arrCx + 3.5f, arrCy + 1.5f);
      cs.stroke();

      // Show selected value as visual hint (only when explicitly set)
      if (selected != null && !selected.isEmpty()) {
        float textY = fieldBottom + (fieldHeight - fontSize) / 2f;
        renderCtx.drawText(
            selected, fieldX + 4f, textY, renderCtx.getRegularFont(), fontSize, textColor);
      }

      cs.setStrokingColor(borderColor.toAwtColor());
      cs.setLineWidth(borderWidth);
      cs.addRect(fieldX, fieldBottom, fieldW, fieldHeight);
      cs.stroke();

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

      PDComboBox comboBox = new PDComboBox(acroForm);
      comboBox.setPartialName(fieldName);
      comboBox.setDefaultAppearance("/Helv " + (int) fontSize + " Tf 0 g");
      comboBox.setOptions(new ArrayList<>(options));
      if (selected != null && options.contains(selected)) comboBox.setValue(selected);

      PDAnnotationWidget widget = comboBox.getWidgets().get(0);
      widget.setRectangle(new PDRectangle(fieldX, fieldBottom, fieldW, fieldHeight));
      widget.setPage(renderCtx.getPage());
      widget.setPrinted(true);

      renderCtx.getPage().getAnnotations().add(widget);
      acroForm.getFields().add(comboBox);
      acroForm.refreshAppearances(List.<PDField>of(comboBox));

    } catch (IOException e) {
      throw new KawaRenderException("Failed to render combobox: " + fieldName, e);
    }
  }
}
