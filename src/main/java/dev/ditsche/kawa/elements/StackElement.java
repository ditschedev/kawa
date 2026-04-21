package dev.ditsche.kawa.elements;

import dev.ditsche.kawa.layout.LayoutContext;
import dev.ditsche.kawa.renderer.RenderContext;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Renders multiple elements on top of each other in the same box.
 *
 * @author Tobias Dittmann
 */
public non-sealed class StackElement implements LayoutElement {

  private final List<Element> layers = new ArrayList<>();

  public StackElement() {}

  public StackElement(Consumer<StackElement> builder) {
    builder.accept(this);
  }

  // -------------------------------------------------------------------------
  // Builder API
  // -------------------------------------------------------------------------

  /** Adds an element as the next layer (rendered on top of all previous layers). */
  public StackElement layer(Element element) {
    layers.add(element);
    return this;
  }

  /** Adds a column layer built by the supplied lambda. */
  public StackElement layer(Consumer<ColumnElement> builder) {
    layers.add(new ColumnElement(builder));
    return this;
  }

  // -------------------------------------------------------------------------
  // Element contract
  // -------------------------------------------------------------------------

  @Override
  public float measure(LayoutContext context) {
    float max = 0f;
    for (Element layer : layers) max = Math.max(max, layer.measure(context));
    return max;
  }

  @Override
  public void render(LayoutContext context, RenderContext renderCtx) {
    for (Element layer : layers) layer.render(context, renderCtx);
  }

  @Override
  public void renderSlice(
      LayoutContext context, RenderContext renderCtx, float offsetY, float availableHeight) {
    for (Element layer : layers) layer.renderSlice(context, renderCtx, offsetY, availableHeight);
  }
}
