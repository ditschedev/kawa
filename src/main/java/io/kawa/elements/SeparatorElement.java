package io.kawa.elements;

import io.kawa.layout.LayoutContext;
import io.kawa.renderer.RenderContext;
import io.kawa.style.KawaColor;
import io.kawa.units.Unit;
import java.io.IOException;

/** Draws a horizontal line across the available content width. */
public final class SeparatorElement implements ContentElement {

  private float lineWidth = 0.5f;
  private KawaColor color = KawaColor.rgb(200, 200, 200);
  private float marginTop = 4f;
  private float marginBottom = 4f;

  public SeparatorElement lineWidth(float w) {
    this.lineWidth = w;
    return this;
  }

  public SeparatorElement lineWidth(float w, Unit unit) {
    return lineWidth(unit.toPoints(w));
  }

  public SeparatorElement color(KawaColor c) {
    this.color = c;
    return this;
  }

  public SeparatorElement marginTop(float m) {
    this.marginTop = m;
    return this;
  }

  public SeparatorElement marginTop(float m, Unit unit) {
    return marginTop(unit.toPoints(m));
  }

  public SeparatorElement marginBottom(float m) {
    this.marginBottom = m;
    return this;
  }

  public SeparatorElement marginBottom(float m, Unit unit) {
    return marginBottom(unit.toPoints(m));
  }

  /** Sets the same margin above and below the line. */
  public SeparatorElement marginV(float m) {
    this.marginTop = this.marginBottom = m;
    return this;
  }

  public SeparatorElement marginV(float m, Unit unit) {
    return marginV(unit.toPoints(m));
  }

  @Override
  public float measure(LayoutContext context) {
    return marginTop + lineWidth + marginBottom;
  }

  @Override
  public void render(LayoutContext context, RenderContext renderCtx) {
    try {
      float pageH = renderCtx.getPage().getMediaBox().getHeight();
      float lineY = pageH - context.y() - marginTop;
      renderCtx.drawLine(
          context.x(), lineY, context.x() + context.width(), lineY, color, lineWidth);
    } catch (IOException e) {
      throw new KawaRenderException("Failed to render separator", e);
    }
  }
}
