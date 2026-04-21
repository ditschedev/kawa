package dev.ditsche.kawa.elements;

import dev.ditsche.kawa.layout.LayoutContext;
import dev.ditsche.kawa.renderer.RenderContext;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Arranges elements in rows and columns with configurable column widths.
 *
 * @author Tobias Dittmann
 */
public non-sealed class GridElement implements LayoutElement {

  // -------------------------------------------------------------------------
  // Column definition
  // -------------------------------------------------------------------------

  private final List<ColDef> colDefs = new ArrayList<>();
  private final List<GridRow> rows = new ArrayList<>();
  private float columnGap = 0f;

  // -------------------------------------------------------------------------
  // Row / cell
  // -------------------------------------------------------------------------
  private float rowGap = 0f;

  // -------------------------------------------------------------------------
  // State
  // -------------------------------------------------------------------------

  public GridElement() {}

  public GridElement(Consumer<GridElement> builder) {
    builder.accept(this);
  }

  public GridElement columns(Consumer<ColumnBuilder> builder) {
    ColumnBuilder cb = new ColumnBuilder();
    builder.accept(cb);
    colDefs.addAll(cb.defs);
    return this;
  }

  public GridElement row(Consumer<GridRow> builder) {
    GridRow row = new GridRow();
    builder.accept(row);
    rows.add(row);
    return this;
  }

  public GridElement columnGap(float gap) {
    this.columnGap = gap;
    return this;
  }

  public GridElement rowGap(float gap) {
    this.rowGap = gap;
    return this;
  }

  // -------------------------------------------------------------------------
  // Builder API
  // -------------------------------------------------------------------------

  @Override
  public float measure(LayoutContext context) {
    float[] widths = resolveWidths(context.width());
    float total = 0f;
    for (int r = 0; r < rows.size(); r++) {
      total += rowHeight(rows.get(r), widths, context);
      if (r < rows.size() - 1) total += rowGap;
    }
    return total;
  }

  @Override
  public void render(LayoutContext context, RenderContext renderCtx) {
    float[] widths = resolveWidths(context.width());
    float cursorY = context.y();

    for (int r = 0; r < rows.size(); r++) {
      GridRow row = rows.get(r);
      float rh = rowHeight(row, widths, context);
      float xPos = context.x();

      for (int c = 0; c < Math.min(row.cells.size(), widths.length); c++) {
        LayoutContext cellCtx = new LayoutContext(xPos, cursorY, widths[c], rh);
        row.cells.get(c).render(cellCtx, renderCtx);
        xPos += widths[c] + (c < widths.length - 1 ? columnGap : 0f);
      }

      cursorY += rh + (r < rows.size() - 1 ? rowGap : 0f);
    }
  }

  private float rowHeight(GridRow row, float[] widths, LayoutContext ctx) {
    float max = 0f;
    for (int c = 0; c < Math.min(row.cells.size(), widths.length); c++) {
      LayoutContext cellCtx = new LayoutContext(0, 0, widths[c], Float.MAX_VALUE);
      max = Math.max(max, row.cells.get(c).measure(cellCtx));
    }
    return max;
  }

  private float[] resolveWidths(float totalWidth) {
    if (colDefs.isEmpty()) return new float[0];
    float totalSpacing = columnGap * Math.max(0, colDefs.size() - 1);
    float fixedTotal = 0f;
    float weightTotal = 0f;
    for (ColDef d : colDefs) {
      if (d.mode() == ColMode.FIXED) fixedTotal += d.value();
      else weightTotal += d.value();
    }
    float remaining = Math.max(0f, totalWidth - totalSpacing - fixedTotal);
    float[] widths = new float[colDefs.size()];
    for (int i = 0; i < colDefs.size(); i++) {
      ColDef d = colDefs.get(i);
      widths[i] =
          d.mode() == ColMode.FIXED
              ? d.value()
              : (weightTotal > 0 ? (d.value() / weightTotal) * remaining : 0f);
    }
    return widths;
  }

  // -------------------------------------------------------------------------
  // Element contract
  // -------------------------------------------------------------------------

  private enum ColMode {
    FIXED,
    RELATIVE
  }

  private record ColDef(ColMode mode, float value) {}

  // -------------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------------

  public static class ColumnBuilder {
    final List<ColDef> defs = new ArrayList<>();

    public ColumnBuilder fixed(float pts) {
      defs.add(new ColDef(ColMode.FIXED, pts));
      return this;
    }

    public ColumnBuilder relative(float w) {
      defs.add(new ColDef(ColMode.RELATIVE, w));
      return this;
    }
  }

  public static class GridRow {
    final List<ColumnElement> cells = new ArrayList<>();

    public GridRow cell(Consumer<ColumnElement> builder) {
      ColumnElement col = new ColumnElement(builder);
      cells.add(col);
      return this;
    }

    public GridRow cell(ColumnElement col) {
      cells.add(col);
      return this;
    }
  }
}
