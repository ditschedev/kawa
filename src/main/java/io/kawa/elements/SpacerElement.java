package io.kawa.elements;

import io.kawa.layout.LayoutContext;
import io.kawa.renderer.RenderContext;
import io.kawa.units.Unit;

/** Adds a fixed amount of vertical space. */
public final class SpacerElement implements ContentElement {

  private final float height;

  /** Creates a spacer with the given height in points. */
  public SpacerElement(float height) {
    this.height = Math.max(0f, height);
  }

  /** Creates a spacer with the given height in the specified unit. */
  public SpacerElement(float height, Unit unit) {
    this(unit.toPoints(height));
  }

  @Override
  public float measure(LayoutContext context) {
    return height;
  }

  @Override
  public void render(LayoutContext context, RenderContext renderCtx) {
    // intentionally blank — space is created by the measured height
  }
}
