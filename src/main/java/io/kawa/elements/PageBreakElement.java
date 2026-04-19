package io.kawa.elements;

import io.kawa.layout.LayoutContext;
import io.kawa.renderer.RenderContext;

/** Forces following content onto a new page during pagination. */
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
