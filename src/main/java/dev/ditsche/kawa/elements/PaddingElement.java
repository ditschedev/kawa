package dev.ditsche.kawa.elements;

import dev.ditsche.kawa.layout.LayoutContext;
import dev.ditsche.kawa.renderer.RenderContext;
import dev.ditsche.kawa.units.Unit;

/**
 * Wraps another element with configurable padding on each side.
 *
 * @author Tobias Dittmann
 */
public non-sealed class PaddingElement implements LayoutElement {

  private final Element child;
  private float top = 0f;
  private float right = 0f;
  private float bottom = 0f;
  private float left = 0f;

  public PaddingElement(Element child) {
    this.child = child;
  }

  public PaddingElement all(float value) {
    this.top = this.right = this.bottom = this.left = value;
    return this;
  }

  public PaddingElement all(float value, Unit unit) {
    return all(unit.toPoints(value));
  }

  public PaddingElement horizontal(float value) {
    this.left = this.right = value;
    return this;
  }

  public PaddingElement horizontal(float value, Unit unit) {
    return horizontal(unit.toPoints(value));
  }

  public PaddingElement vertical(float value) {
    this.top = this.bottom = value;
    return this;
  }

  public PaddingElement vertical(float value, Unit unit) {
    return vertical(unit.toPoints(value));
  }

  public PaddingElement top(float v) {
    this.top = v;
    return this;
  }

  public PaddingElement top(float v, Unit unit) {
    return top(unit.toPoints(v));
  }

  public PaddingElement right(float v) {
    this.right = v;
    return this;
  }

  public PaddingElement right(float v, Unit unit) {
    return right(unit.toPoints(v));
  }

  public PaddingElement bottom(float v) {
    this.bottom = v;
    return this;
  }

  public PaddingElement bottom(float v, Unit unit) {
    return bottom(unit.toPoints(v));
  }

  public PaddingElement left(float v) {
    this.left = v;
    return this;
  }

  public PaddingElement left(float v, Unit unit) {
    return left(unit.toPoints(v));
  }

  @Override
  public float measure(LayoutContext context) {
    LayoutContext inner = innerContext(context);
    return top + child.measure(inner) + bottom;
  }

  @Override
  public void render(LayoutContext context, RenderContext renderCtx) {
    child.render(innerContext(context).moveDown(top), renderCtx);
  }

  private LayoutContext innerContext(LayoutContext context) {
    return context.withHorizontalInsets(left, right);
  }
}
