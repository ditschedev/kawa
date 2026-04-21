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
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDListBox;

/**
 * Renders an interactive PDF list box (scrollable choice field).
 *
 * <p>Users can select one item from a scrollable list. Each field name should be unique within the
 * document.
 *
 * @author Tobias Dittmann
 */
public final class ChoiceElement implements ContentElement {

  private final String fieldName;
  private final List<String> options;

  // -------------------------------------------------------------------------
  // State
  // -------------------------------------------------------------------------
  private String selected = null;
  private float fieldHeight = 80f;
  private float fontSize = 11f;
  private KawaColor borderColor = KawaColor.rgb(180, 180, 180);
  private KawaColor backgroundColor = KawaColor.WHITE;
  private KawaColor textColor = KawaColor.rgb(40, 40, 40);
  private KawaColor selectionColor = KawaColor.rgb(37, 99, 235);
  private float borderWidth = 1f;
  private String label = null;
  private float labelFontSize = 10f;
  private KawaColor labelColor = KawaColor.rgb(80, 80, 80);
  private final float labelGap = 4f;

  /**
   * @param fieldName unique name for this AcroForm field within the document
   * @param options list of selectable option labels
   */
  public ChoiceElement(String fieldName, List<String> options) {
    this.fieldName = fieldName == null ? "choice" : fieldName;
    this.options = options != null ? new ArrayList<>(options) : new ArrayList<>();
  }

  // -------------------------------------------------------------------------
  // Fluent API
  // -------------------------------------------------------------------------

  /** Pre-selects the option matching this label. */
  public ChoiceElement selected(String s) {
    this.selected = s;
    return this;
  }

  /** Sets the height of the list box in points. Default: 80. */
  public ChoiceElement height(float h) {
    this.fieldHeight = h;
    return this;
  }

  /** Sets the height of the list box using an explicit unit. */
  public ChoiceElement height(float h, Unit unit) {
    return height(unit.toPoints(h));
  }

  /** Font size for the option items. Default: 11. */
  public ChoiceElement fontSize(float s) {
    this.fontSize = s;
    return this;
  }

  /** Color of the field border. */
  public ChoiceElement borderColor(KawaColor c) {
    this.borderColor = c;
    return this;
  }

  /** Background fill color of the list box. */
  public ChoiceElement backgroundColor(KawaColor c) {
    this.backgroundColor = c;
    return this;
  }

  /** Color of the option item text. */
  public ChoiceElement textColor(KawaColor c) {
    this.textColor = c;
    return this;
  }

  /** Highlight color of the selected item row. */
  public ChoiceElement selectionColor(KawaColor c) {
    this.selectionColor = c;
    return this;
  }

  /** Thickness of the drawn border in points. Default: 1. */
  public ChoiceElement borderWidth(float w) {
    this.borderWidth = w;
    return this;
  }

  /** Sets the border width using an explicit unit. */
  public ChoiceElement borderWidth(float w, Unit unit) {
    return borderWidth(unit.toPoints(w));
  }

  /** Draws a small label above the list box. */
  public ChoiceElement label(String l) {
    this.label = l;
    return this;
  }

  /** Font size of the label text. Default: 10. */
  public ChoiceElement labelFontSize(float s) {
    this.labelFontSize = s;
    return this;
  }

  /** Color of the label text. */
  public ChoiceElement labelColor(KawaColor c) {
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

      // Draw options as a visual preview
      float lineH = fontSize * 1.5f;
      float textX = fieldX + 4f;
      float textY = fieldBottom + fieldHeight - fontSize - 4f;
      for (String opt : options) {
        if (textY < fieldBottom + 2f) break;
        if (opt.equals(selected)) {
          renderCtx.drawRect(fieldX, textY - (lineH - fontSize) / 2f, fieldW, lineH, selectionColor);
          renderCtx.drawText(
              opt, textX, textY, renderCtx.getRegularFont(), fontSize, KawaColor.WHITE);
        } else {
          renderCtx.drawText(opt, textX, textY, renderCtx.getRegularFont(), fontSize, textColor);
        }
        textY -= lineH;
      }

      var cs = renderCtx.getContentStream();
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

      PDListBox listBox = new PDListBox(acroForm);
      listBox.setPartialName(fieldName);
      listBox.setDefaultAppearance("/Helv " + (int) fontSize + " Tf 0 g");
      listBox.setOptions(new ArrayList<>(options));
      if (selected != null && options.contains(selected)) listBox.setValue(selected);

      PDAnnotationWidget widget = listBox.getWidgets().get(0);
      widget.setRectangle(new PDRectangle(fieldX, fieldBottom, fieldW, fieldHeight));
      widget.setPage(renderCtx.getPage());
      widget.setPrinted(true);

      renderCtx.getPage().getAnnotations().add(widget);
      acroForm.getFields().add(listBox);
      acroForm.refreshAppearances(List.<PDField>of(listBox));

    } catch (IOException e) {
      throw new KawaRenderException("Failed to render choice field: " + fieldName, e);
    }
  }
}
