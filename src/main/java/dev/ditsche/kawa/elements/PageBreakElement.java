package dev.ditsche.kawa.elements;

import dev.ditsche.kawa.layout.LayoutContext;
import dev.ditsche.kawa.renderer.RenderContext;

/**
 * Forces following content onto a new page during pagination.
 *
 * @author Tobias Dittmann
 */
public final class PageBreakElement implements ContentElement {

  @Override
  public float measure(LayoutContext context) {
    return 0f;
  }

  @Override
  public void render(LayoutContext context, RenderContext renderCtx) {
    // intentionally blank
  }
}
