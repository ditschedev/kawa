package dev.ditsche.kawa.elements;

import dev.ditsche.kawa.renderer.DrawContext;

/**
 * Implemented by custom elements to participate in Kawa's layout and rendering pipeline.
 *
 * <p>Implement {@link #measure(float)} to report how much vertical space your element needs, and
 * {@link #render(DrawContext)} to draw it. Both are called with a {@link DrawContext} that exposes
 * the available area and provides drawing helpers.
 *
 * <p>Override {@link #renderSlice(DrawContext, float, float)} if your element can span a page break
 * and needs to render only a vertical slice of itself. The default implementation renders the full
 * element regardless of the slice window, which is correct for most cases.
 *
 * @see CustomElement
 * @see DrawContext
 *
 * @author Tobias Dittmann
 */
public interface ElementRenderer {

  /**
   * Returns the total height required by this element given the available width.
   *
   * <p>This method is called before rendering to determine page breaks. It must be fast and
   * side-effect free — it may be called multiple times.
   *
   * @param availableWidth the horizontal space available in points
   * @return required height in points
   */
  float measure(float availableWidth);

  /**
   * Renders this element.
   *
   * <p>The drawing area starts at ({@link DrawContext#getX()}, {@link DrawContext#getY()}) and
   * extends for {@link DrawContext#getWidth()} points horizontally. All coordinates use a top-left
   * origin.
   *
   * @param ctx the drawing context
   * @throws Exception if rendering fails
   */
  void render(DrawContext ctx) throws Exception;

  /**
   * Renders a vertical slice of this element.
   *
   * <p>Called when an element spans a page boundary. {@code offsetY} is how many points into the
   * element the current page starts, and {@code availableHeight} is how many points remain on the
   * current page.
   *
   * <p>The default implementation ignores the slice window and calls {@link #render(DrawContext)},
   * which is fine for most single-page elements. Override this method if your element needs to
   * render different content depending on which portion is visible.
   *
   * @param ctx the drawing context
   * @param offsetY vertical offset into the element in points
   * @param availableHeight remaining vertical space on the current page in points
   * @throws Exception if rendering fails
   */
  default void renderSlice(DrawContext ctx, float offsetY, float availableHeight) throws Exception {
    render(ctx);
  }
}
