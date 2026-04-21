package dev.ditsche.kawa.elements;

import dev.ditsche.kawa.layout.LayoutContext;
import dev.ditsche.kawa.renderer.RenderContext;
import dev.ditsche.kawa.style.KawaColor;
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
import org.apache.pdfbox.pdmodel.interactive.form.PDRadioButton;

/**
 * Renders a group of interactive PDF radio buttons.
 *
 * <p>Each field name should be unique within the document. Options are strings used both as display
 * labels and export values.
 *
 * @author Tobias Dittmann
 */
public final class RadioButtonElement implements ContentElement {

  private final String fieldName;
  private final List<String> options;

  // -------------------------------------------------------------------------
  // State
  // -------------------------------------------------------------------------
  private String selected = null;
  private float size = 12f;
  private float labelFontSize = 11f;
  private KawaColor labelColor = KawaColor.rgb(40, 40, 40);
  private KawaColor borderColor = KawaColor.rgb(180, 180, 180);
  private KawaColor backgroundColor = KawaColor.WHITE;
  private KawaColor selectedColor = KawaColor.rgb(37, 99, 235);
  private Orientation orientation = Orientation.VERTICAL;
  private final float itemGap = 6f;
  private final float labelGap = 6f;

  /**
   * @param fieldName unique name for this AcroForm radio button group
   * @param options list of option labels (also used as export values)
   */
  public RadioButtonElement(String fieldName, List<String> options) {
    this.fieldName = fieldName == null ? "radio" : fieldName;
    this.options = options != null ? new ArrayList<>(options) : new ArrayList<>();
  }

  // -------------------------------------------------------------------------
  // Fluent API
  // -------------------------------------------------------------------------

  /** Pre-selects the option matching the given label. */
  public RadioButtonElement selected(String s) {
    this.selected = s;
    return this;
  }

  /** Size of each radio button circle in points. Default: 12. */
  public RadioButtonElement size(float s) {
    this.size = s;
    return this;
  }

  /** Font size of the option labels. Default: 11. */
  public RadioButtonElement labelFontSize(float s) {
    this.labelFontSize = s;
    return this;
  }

  /** Color of the option label text. */
  public RadioButtonElement labelColor(KawaColor c) {
    this.labelColor = c;
    return this;
  }

  /** Color of the radio button border. */
  public RadioButtonElement borderColor(KawaColor c) {
    this.borderColor = c;
    return this;
  }

  /** Background fill color of each radio button circle. */
  public RadioButtonElement backgroundColor(KawaColor c) {
    this.backgroundColor = c;
    return this;
  }

  /** Fill color of the inner dot when an option is selected. */
  public RadioButtonElement selectedColor(KawaColor c) {
    this.selectedColor = c;
    return this;
  }

  /** Arranges options vertically (default). */
  public RadioButtonElement vertical() {
    this.orientation = Orientation.VERTICAL;
    return this;
  }

  /** Arranges options side by side horizontally. */
  public RadioButtonElement horizontal() {
    this.orientation = Orientation.HORIZONTAL;
    return this;
  }

  // -------------------------------------------------------------------------
  // Element contract
  // -------------------------------------------------------------------------

  @Override
  public float measure(LayoutContext context) {
    if (options.isEmpty()) return 0f;
    float rowH = Math.max(size, labelFontSize * 1.3f);
    if (orientation == Orientation.VERTICAL) {
      return options.size() * rowH + (options.size() - 1) * itemGap;
    }
    return rowH;
  }

  @Override
  public void render(LayoutContext context, RenderContext renderCtx) {
    try {
      float pageH = renderCtx.getPage().getMediaBox().getHeight();
      float rowH = Math.max(size, labelFontSize * 1.3f);

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

      PDRadioButton radioButton = new PDRadioButton(acroForm);
      radioButton.setPartialName(fieldName);

      List<PDAnnotationWidget> widgets = new ArrayList<>();
      List<String> exportValues = new ArrayList<>();

      float cursorX = context.x();
      float cursorY = context.y();

      for (String option : options) {
        boolean isSelected = option.equals(selected);
        float btnBottom = pageH - cursorY - rowH + (rowH - size) / 2f;

        drawCircle(renderCtx, cursorX + size / 2f, btnBottom + size / 2f, size / 2f);
        if (isSelected) {
          float innerR = size * 0.28f;
          drawFilledCircle(
              renderCtx, cursorX + size / 2f, btnBottom + size / 2f, innerR, selectedColor);
        }

        float textY = btnBottom + (size - labelFontSize) / 2f;
        renderCtx.drawText(
            option,
            cursorX + size + labelGap,
            textY,
            renderCtx.getRegularFont(),
            labelFontSize,
            labelColor);

        PDAnnotationWidget widget = new PDAnnotationWidget();
        widget.setRectangle(new PDRectangle(cursorX, btnBottom, size, size));
        widget.setPage(renderCtx.getPage());
        widget.setPrinted(true);
        widget.setAppearanceState(isSelected ? option : "Off");
        widgets.add(widget);
        exportValues.add(option);
        renderCtx.getPage().getAnnotations().add(widget);

        if (orientation == Orientation.VERTICAL) {
          cursorY += rowH + itemGap;
        } else {
          float labelW;
          try {
            labelW = renderCtx.textWidth(option, renderCtx.getRegularFont(), labelFontSize);
          } catch (IOException ex) {
            labelW = option.length() * labelFontSize * 0.55f;
          }
          cursorX += size + labelGap + labelW + itemGap * 2;
        }
      }

      radioButton.setWidgets(widgets);
      radioButton.setExportValues(exportValues);
      if (selected != null && exportValues.contains(selected)) {
        radioButton.setValue(selected);
      }
      acroForm.getFields().add(radioButton);
      acroForm.refreshAppearances(List.<PDField>of(radioButton));

    } catch (IOException e) {
      throw new KawaRenderException("Failed to render radio buttons: " + fieldName, e);
    }
  }

  private void drawCircle(RenderContext ctx, float cx, float cy, float r) throws IOException {
    float k = 0.5523f;
    var cs = ctx.getContentStream();
    cs.setNonStrokingColor(backgroundColor.toAwtColor());
    cs.setStrokingColor(borderColor.toAwtColor());
    cs.setLineWidth(1f);
    cs.moveTo(cx - r, cy);
    cs.curveTo(cx - r, cy + k * r, cx - k * r, cy + r, cx, cy + r);
    cs.curveTo(cx + k * r, cy + r, cx + r, cy + k * r, cx + r, cy);
    cs.curveTo(cx + r, cy - k * r, cx + k * r, cy - r, cx, cy - r);
    cs.curveTo(cx - k * r, cy - r, cx - r, cy - k * r, cx - r, cy);
    cs.fillAndStroke();
  }

  private void drawFilledCircle(RenderContext ctx, float cx, float cy, float r, KawaColor fill)
      throws IOException {
    float k = 0.5523f;
    var cs = ctx.getContentStream();
    cs.setNonStrokingColor(fill.toAwtColor());
    cs.moveTo(cx - r, cy);
    cs.curveTo(cx - r, cy + k * r, cx - k * r, cy + r, cx, cy + r);
    cs.curveTo(cx + k * r, cy + r, cx + r, cy + k * r, cx + r, cy);
    cs.curveTo(cx + r, cy - k * r, cx + k * r, cy - r, cx, cy - r);
    cs.curveTo(cx - k * r, cy - r, cx - r, cy - k * r, cx - r, cy);
    cs.fill();
  }

  /** Layout direction for the radio button group. */
  public enum Orientation {
    /** Each option occupies its own row. */
    VERTICAL,
    /** All options are arranged side by side on one row. */
    HORIZONTAL
  }
}
