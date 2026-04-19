package dev.ditsche.kawa.elements;

import dev.ditsche.kawa.layout.LayoutContext;
import dev.ditsche.kawa.renderer.RenderContext;
import dev.ditsche.kawa.units.Unit;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Arranges child elements horizontally in fixed or relative columns. */
public non-sealed class RowElement implements LayoutElement {

  // -------------------------------------------------------------------------
  // Column descriptor
  // -------------------------------------------------------------------------

  private final List<Column> columns = new ArrayList<>();
  private float spacing = 0f; // horizontal gap between columns

  // -------------------------------------------------------------------------
  // State
  // -------------------------------------------------------------------------

  public RowElement() {}

  public RowElement(Consumer<RowElement> builder) {
    builder.accept(this);
  }

  /** Adds a column with an exact width in points. */
  public RowElement fixedColumn(float width, Element element) {
    columns.add(new Column(SizeMode.FIXED, width, element));
    return this;
  }

  /** Adds a column with an exact width in the specified unit. */
  public RowElement fixedColumn(float width, Unit unit, Element element) {
    return fixedColumn(unit.toPoints(width), element);
  }

  // -------------------------------------------------------------------------
  // Column builders
  // -------------------------------------------------------------------------

  /** Adds a column with an exact width; content built via lambda. */
  public RowElement fixedColumn(float width, Consumer<ColumnElement> builder) {
    return fixedColumn(width, new ColumnElement(builder));
  }

  /** Adds a column with an exact width in the specified unit; content built via lambda. */
  public RowElement fixedColumn(float width, Unit unit, Consumer<ColumnElement> builder) {
    return fixedColumn(unit.toPoints(width), new ColumnElement(builder));
  }

  /** Adds a column that takes a proportional share of the remaining space. */
  public RowElement relativeColumn(float weight, Element element) {
    columns.add(new Column(SizeMode.RELATIVE, weight, element));
    return this;
  }

  /** Adds a relative column; content built via lambda. */
  public RowElement relativeColumn(float weight, Consumer<ColumnElement> builder) {
    return relativeColumn(weight, new ColumnElement(builder));
  }

  /** Shorthand: relative column with weight 1. */
  public RowElement fillColumn(Element element) {
    return relativeColumn(1f, element);
  }

  /** Shorthand: fill column with lambda. */
  public RowElement fillColumn(Consumer<ColumnElement> builder) {
    return relativeColumn(1f, new ColumnElement(builder));
  }

  /** Horizontal gap between columns in points. */
  public RowElement spacing(float gap) {
    this.spacing = gap;
    return this;
  }

  public RowElement spacing(float gap, Unit unit) {
    return spacing(unit.toPoints(gap));
  }

  @Override
  public float measure(LayoutContext context) {
    float[] widths = resolveWidths(context.width());
    float maxHeight = 0f;
    float xOffset = 0f;
    for (int i = 0; i < columns.size(); i++) {
      float colWidth = widths[i];
      LayoutContext colCtx =
          new LayoutContext(context.x() + xOffset, context.y(), colWidth, context.height());
      maxHeight = Math.max(maxHeight, columns.get(i).element.measure(colCtx));
      xOffset += colWidth + (i < columns.size() - 1 ? spacing : 0f);
    }
    return maxHeight;
  }

  @Override
  public void render(LayoutContext context, RenderContext renderCtx) {
    renderSlice(context, renderCtx, 0f, Float.MAX_VALUE);
  }

  // -------------------------------------------------------------------------
  // Element contract
  // -------------------------------------------------------------------------

  @Override
  public void renderSlice(
      LayoutContext context, RenderContext renderCtx, float offsetY, float availableHeight) {
    float[] widths = resolveWidths(context.width());
    float xOffset = 0f;
    for (int i = 0; i < columns.size(); i++) {
      float colWidth = widths[i];
      LayoutContext colCtx =
          new LayoutContext(context.x() + xOffset, context.y(), colWidth, availableHeight);
      columns.get(i).element.renderSlice(colCtx, renderCtx, offsetY, availableHeight);
      xOffset += colWidth + (i < columns.size() - 1 ? spacing : 0f);
    }
  }

  /** Resolves each column width in points. */
  private float[] resolveWidths(float totalWidth) {
    float[] widths = new float[columns.size()];
    float totalSpacing = spacing * Math.max(0, columns.size() - 1);
    float fixedTotal = 0f;
    float weightTotal = 0f;

    for (Column col : columns) {
      if (col.mode == SizeMode.FIXED) fixedTotal += col.value;
      else weightTotal += col.value;
    }

    float remaining = Math.max(0f, totalWidth - totalSpacing - fixedTotal);

    for (int i = 0; i < columns.size(); i++) {
      Column col = columns.get(i);
      widths[i] =
          col.mode == SizeMode.FIXED
              ? col.value
              : (weightTotal > 0 ? (col.value / weightTotal) * remaining : 0f);
    }
    return widths;
  }

  private enum SizeMode {
    FIXED,
    RELATIVE
  }

  // -------------------------------------------------------------------------
  // Width resolution
  // -------------------------------------------------------------------------

  /**
   * @param value points for FIXED, weight for RELATIVE
   */
  private record Column(SizeMode mode, float value, Element element) {}
}
