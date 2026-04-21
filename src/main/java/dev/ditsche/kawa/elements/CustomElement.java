package dev.ditsche.kawa.elements;

import dev.ditsche.kawa.layout.LayoutContext;
import dev.ditsche.kawa.renderer.DrawContext;
import dev.ditsche.kawa.renderer.RenderContext;

/**
 * Wraps a user-supplied {@link ElementRenderer} so that custom drawing logic can participate in
 * Kawa's layout and pagination pipeline.
 *
 * <p>Use {@link #CustomElement(ElementRenderer)} directly, or call {@code
 * c.item().custom(renderer)} on a {@link ColumnElement}.
 *
 * @see ElementRenderer
 * @see DrawContext
 *
 * @author Tobias Dittmann
 */
public final class CustomElement implements ContentElement {

  private final ElementRenderer renderer;

  public CustomElement(ElementRenderer renderer) {
    if (renderer == null) throw new IllegalArgumentException("renderer must not be null");
    this.renderer = renderer;
  }

  @Override
  public float measure(LayoutContext context) {
    return renderer.measure(context.width());
  }

  @Override
  public void render(LayoutContext context, RenderContext renderCtx) {
    try {
      renderer.render(new DrawContext(context, renderCtx));
    } catch (Exception e) {
      throw new KawaRenderException("Custom element rendering failed", e);
    }
  }

  @Override
  public void renderSlice(
      LayoutContext context, RenderContext renderCtx, float offsetY, float availableHeight) {
    try {
      renderer.renderSlice(new DrawContext(context, renderCtx), offsetY, availableHeight);
    } catch (Exception e) {
      throw new KawaRenderException("Custom element rendering failed", e);
    }
  }
}
