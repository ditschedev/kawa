package dev.ditsche.kawa.elements;

import dev.ditsche.kawa.layout.LayoutContext;
import dev.ditsche.kawa.renderer.RenderContext;

/**
 * Base abstraction for all composable PDF elements.
 *
 * <p>Elements participate in a measurement phase and a render phase.
 *
 * @author Tobias Dittmann
 */
public sealed interface Element permits LayoutElement, ContentElement {

  /**
   * Measures the total height required by this element.
   *
   * @param context the available layout space
   * @return required height in points
   */
  float measure(LayoutContext context);

  /**
   * Renders this element into the PDF at the given position.
   *
   * @param context the available layout space
   * @param renderCtx the render context
   */
  void render(LayoutContext context, RenderContext renderCtx);

  /**
   * Renders a vertical slice of this element.
   *
   * <p>The default implementation renders the full element.
   *
   * @param context layout context on the current physical page
   * @param renderCtx render context
   * @param offsetY vertical offset into the element
   * @param availableHeight remaining height on the current page
   */
  default void renderSlice(
      LayoutContext context, RenderContext renderCtx, float offsetY, float availableHeight) {
    render(context, renderCtx);
  }
}
