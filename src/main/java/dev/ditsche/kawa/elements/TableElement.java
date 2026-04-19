package dev.ditsche.kawa.elements;

import dev.ditsche.kawa.font.KawaFont;
import dev.ditsche.kawa.layout.LayoutContext;
import dev.ditsche.kawa.renderer.RenderContext;
import dev.ditsche.kawa.style.KawaColor;
import dev.ditsche.kawa.style.StyleSheet;
import dev.ditsche.kawa.units.Unit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Renders a table with configurable columns, rows, padding, and borders. */
public non-sealed class TableElement implements LayoutElement {

  // -------------------------------------------------------------------------
  // Column definition
  // -------------------------------------------------------------------------

  private final List<ColDef> colDefs = new ArrayList<>();
  private final List<Row> dataRows = new ArrayList<>();
  private Row headerRow = null;

  // -------------------------------------------------------------------------
  // Cell — a layout element that holds a ContentElement or ColumnElement
  // -------------------------------------------------------------------------
  private float cellPaddingH = 6f;

  // -------------------------------------------------------------------------
  // Row
  // -------------------------------------------------------------------------
  private float cellPaddingV = 5f;
  private KawaColor borderColor = KawaColor.rgb(200, 200, 200);

  // -------------------------------------------------------------------------
  // TableElement state
  // -------------------------------------------------------------------------
  private float borderWidth = 0.5f;
  private boolean drawBorders = true;
  private KawaColor headerBackground = KawaColor.rgb(240, 240, 240);
  private KawaColor alternateRowColor = null;

  public TableElement() {}

  public TableElement(Consumer<TableElement> builder) {
    builder.accept(this);
  }

  public TableElement columns(Consumer<ColumnBuilder> builder) {
    ColumnBuilder cb = new ColumnBuilder();
    builder.accept(cb);
    colDefs.addAll(cb.defs);
    return this;
  }

  public TableElement header(Consumer<RowBuilder> builder) {
    headerRow = new Row();
    builder.accept(new RowBuilder(headerRow));
    return this;
  }

  public TableElement row(Consumer<RowBuilder> builder) {
    Row row = new Row();
    builder.accept(new RowBuilder(row));
    dataRows.add(row);
    return this;
  }

  public TableElement cellPadding(float all) {
    cellPaddingH = cellPaddingV = all;
    return this;
  }

  public TableElement cellPaddingH(float h) {
    cellPaddingH = h;
    return this;
  }

  public TableElement cellPaddingV(float v) {
    cellPaddingV = v;
    return this;
  }

  // -------------------------------------------------------------------------
  // Configuration
  // -------------------------------------------------------------------------

  /**
   * Sets cell padding on both axes using an explicit unit.
   *
   * @param all the padding value
   * @return this table
   */
  public TableElement cellPadding(float all, Unit unit) {
    return cellPadding(unit.toPoints(all));
  }

  /**
   * Sets horizontal cell padding using an explicit unit.
   *
   * @param h the horizontal padding value
   * @return this table
   */
  public TableElement cellPaddingH(float h, Unit unit) {
    return cellPaddingH(unit.toPoints(h));
  }

  /**
   * Sets vertical cell padding using an explicit unit.
   *
   * @param v the vertical padding value
   * @return this table
   */
  public TableElement cellPaddingV(float v, Unit unit) {
    return cellPaddingV(unit.toPoints(v));
  }

  public TableElement borderColor(KawaColor c) {
    borderColor = c;
    return this;
  }

  public TableElement borderWidth(float w) {
    borderWidth = w;
    return this;
  }

  public TableElement noBorders() {
    drawBorders = false;
    return this;
  }

  public TableElement headerBackground(KawaColor c) {
    headerBackground = c;
    return this;
  }

  public TableElement alternateRowColor(KawaColor c) {
    alternateRowColor = c;
    return this;
  }

  @Override
  public float measure(LayoutContext context) {
    float[] widths = resolveWidths(context.width());
    float total = 0f;
    if (headerRow != null) total += computeRowHeight(headerRow, widths);
    for (Row row : dataRows) total += computeRowHeight(row, widths);
    return total;
  }

  @Override
  public void render(LayoutContext context, RenderContext renderCtx) {
    renderSlice(context, renderCtx, 0f, Float.MAX_VALUE);
  }

  @Override
  public void renderSlice(
      LayoutContext context, RenderContext renderCtx, float offsetY, float availableHeight) {
    try {
      float[] widths = resolveWidths(context.width());
      float pageH = renderCtx.getPage().getMediaBox().getHeight();
      float cursor = 0f;

      // --- Header row ---
      if (headerRow != null) {
        float rowH = computeRowHeight(headerRow, widths);
        float rowBottom = cursor + rowH;
        if (rowBottom > offsetY && cursor < offsetY + availableHeight) {
          float drawY = context.y() + (cursor - offsetY);
          renderRow(
              headerRow, widths, context.x(), drawY, rowH, headerBackground, renderCtx, pageH);
        }
        cursor = rowBottom;
      }

      // --- Data rows ---
      for (int i = 0; i < dataRows.size(); i++) {
        Row row = dataRows.get(i);
        float rowH = computeRowHeight(row, widths);
        float rowBottom = cursor + rowH;

        if (rowBottom <= offsetY) {
          cursor = rowBottom;
          continue;
        }
        if (cursor >= offsetY + availableHeight) break;

        KawaColor bg =
            row.background != null
                ? row.background
                : (alternateRowColor != null && i % 2 == 1 ? alternateRowColor : null);
        float drawY = context.y() + (cursor - offsetY);
        renderRow(row, widths, context.x(), drawY, rowH, bg, renderCtx, pageH);
        cursor = rowBottom;
      }

      // --- Outer border ---
      if (drawBorders) {
        float tableHeight = Math.min(cursor - offsetY, availableHeight);
        renderCtx.drawLine(
            context.x(),
            pageH - context.y(),
            context.x() + context.width(),
            pageH - context.y(),
            borderColor,
            borderWidth);
        renderCtx.drawLine(
            context.x(),
            pageH - (context.y() + tableHeight),
            context.x() + context.width(),
            pageH - (context.y() + tableHeight),
            borderColor,
            borderWidth);
        renderCtx.drawLine(
            context.x(),
            pageH - context.y(),
            context.x(),
            pageH - (context.y() + tableHeight),
            borderColor,
            borderWidth);
        renderCtx.drawLine(
            context.x() + context.width(),
            pageH - context.y(),
            context.x() + context.width(),
            pageH - (context.y() + tableHeight),
            borderColor,
            borderWidth);
      }

    } catch (IOException e) {
      throw new KawaRenderException("Failed to render table", e);
    }
  }

  /** Computes the height of a row from the tallest cell content. */
  private float computeRowHeight(Row row, float[] widths) {
    float maxH = 0f;
    for (int c = 0; c < Math.min(widths.length, row.cells.size()); c++) {
      Cell cell = row.cells.get(c);
      Element content = cell.getContent();
      if (content != null) {
        float ph = cell.getCellPaddingH() != null ? cell.getCellPaddingH() : cellPaddingH;
        float pv = cell.getCellPaddingV() != null ? cell.getCellPaddingV() : cellPaddingV;
        float contentW = Math.max(0f, widths[c] - ph * 2);
        LayoutContext measureCtx = new LayoutContext(0, 0, contentW, Float.MAX_VALUE);
        maxH = Math.max(maxH, content.measure(measureCtx) + pv * 2);
      }
    }
    // Guarantee a minimum sensible height even for empty rows
    return maxH > 0f ? maxH : cellPaddingV * 2 + 14f;
  }

  private void renderRow(
      Row row,
      float[] widths,
      float startX,
      float topY,
      float rowHeight,
      KawaColor background,
      RenderContext ctx,
      float pageH)
      throws IOException {

    float pdfTop = pageH - topY;
    float pdfBottom = pdfTop - rowHeight;
    float xCursor = startX;

    for (int c = 0; c < widths.length; c++) {
      float colWidth = widths[c];
      float cRight = xCursor + colWidth;
      Cell cell = c < row.cells.size() ? row.cells.get(c) : null;

      // Cell background: per-cell > row/alternating > nothing
      KawaColor cellBg =
          (cell != null && cell.getCellBackground() != null)
              ? cell.getCellBackground()
              : background;
      if (cellBg != null) {
        ctx.drawRect(xCursor, pdfBottom, colWidth, rowHeight, cellBg);
      }

      // Table-level border lines
      if (drawBorders) {
        ctx.drawLine(cRight, pdfTop, cRight, pdfBottom, borderColor, borderWidth);
        ctx.drawLine(xCursor, pdfBottom, cRight, pdfBottom, borderColor, borderWidth);
        ctx.drawLine(xCursor, pdfTop, cRight, pdfTop, borderColor, borderWidth);
      }

      // Per-cell border overrides
      if (cell != null) {
        if (cell.borderTopW != null && cell.borderTopC != null)
          ctx.drawLine(xCursor, pdfTop, cRight, pdfTop, cell.borderTopC, cell.borderTopW);
        if (cell.borderBottomW != null && cell.borderBottomC != null)
          ctx.drawLine(
              xCursor, pdfBottom, cRight, pdfBottom, cell.borderBottomC, cell.borderBottomW);
        if (cell.borderLeftW != null && cell.borderLeftC != null)
          ctx.drawLine(xCursor, pdfTop, xCursor, pdfBottom, cell.borderLeftC, cell.borderLeftW);
        if (cell.borderRightW != null && cell.borderRightC != null)
          ctx.drawLine(cRight, pdfTop, cRight, pdfBottom, cell.borderRightC, cell.borderRightW);
      }

      // Render cell content via the element's own render method
      if (cell != null && cell.getContent() != null) {
        float ph = cell.getCellPaddingH() != null ? cell.getCellPaddingH() : cellPaddingH;
        float pv = cell.getCellPaddingV() != null ? cell.getCellPaddingV() : cellPaddingV;
        float contentX = xCursor + ph;
        float contentY = topY + pv;
        float contentW = Math.max(0f, colWidth - ph * 2);
        float contentH = Math.max(0f, rowHeight - pv * 2);

        LayoutContext contentCtx = new LayoutContext(contentX, contentY, contentW, contentH);
        cell.getContent().render(contentCtx, ctx);
      }

      xCursor += colWidth;
    }
  }

  private float[] resolveWidths(float total) {
    float fixedSum = 0f;
    float weightSum = 0f;
    for (ColDef d : colDefs) {
      if (d.mode == ColMode.FIXED) fixedSum += d.value;
      else weightSum += d.value;
    }
    float remaining = Math.max(0f, total - fixedSum);
    float[] w = new float[colDefs.size()];
    for (int i = 0; i < colDefs.size(); i++) {
      ColDef d = colDefs.get(i);
      w[i] =
          d.mode == ColMode.FIXED
              ? d.value
              : (weightSum > 0 ? (d.value / weightSum) * remaining : 0f);
    }
    return w;
  }

  // -------------------------------------------------------------------------
  // Element contract
  // -------------------------------------------------------------------------

  private enum ColMode {
    FIXED,
    RELATIVE
  }

  public static class ColDef {
    final ColMode mode;
    final float value;

    ColDef(ColMode mode, float value) {
      this.mode = mode;
      this.value = value;
    }
  }

  public static class ColumnBuilder {
    final List<ColDef> defs = new ArrayList<>();

    public ColumnBuilder fixed(float pts) {
      defs.add(new ColDef(ColMode.FIXED, pts));
      return this;
    }

    public ColumnBuilder fixed(float value, Unit unit) {
      return fixed(unit.toPoints(value));
    }

    public ColumnBuilder relative(float w) {
      defs.add(new ColDef(ColMode.RELATIVE, w));
      return this;
    }

    public ColumnBuilder relative() {
      defs.add(new ColDef(ColMode.RELATIVE, 1));
      return this;
    }
  }

  // -------------------------------------------------------------------------
  // Private helpers
  // -------------------------------------------------------------------------

  /** Represents a single table cell. */
  public static class Cell {

    // Content — either a ContentElement leaf or a ColumnElement stack
    private Element content;

    // Per-cell layout overrides (null = inherit table-level default)
    private KawaColor cellBackground = null;
    private Float cellPaddingH = null;
    private Float cellPaddingV = null;

    // Per-cell border overrides (null = inherit table-level border setting)
    private Float borderTopW = null;
    private KawaColor borderTopC = null;
    private Float borderBottomW = null;
    private KawaColor borderBottomC = null;
    private Float borderLeftW = null;
    private KawaColor borderLeftC = null;
    private Float borderRightW = null;
    private KawaColor borderRightC = null;

    /** Creates a cell whose content is a {@link TextElement} with the given text. */
    public Cell(String text) {
      this.content = new TextElement(text == null ? "" : text);
    }

    /** Creates a cell with an empty {@link TextElement} as content. */
    public Cell() {
      this.content = new TextElement("");
    }

    // --- Typed content setters ---

    /** Replaces the current content with the given {@link ContentElement}. */
    public Cell content(ContentElement el) {
      this.content = el;
      return this;
    }

    /** Replaces the current content with a {@link ColumnElement} for stacked items. */
    public Cell content(ColumnElement col) {
      this.content = col;
      return this;
    }

    // --- Text convenience proxies (only effective when content is a TextElement) ---

    /**
     * Replaces the current content with a new {@link TextElement} carrying the given text. Any
     * previously applied text styles are lost.
     */
    public Cell text(String t) {
      this.content = new TextElement(t == null ? "" : t);
      return this;
    }

    /** Applies bold to the internal {@link TextElement}; no-op for other content types. */
    public Cell bold() {
      if (content instanceof TextElement te) te.bold();
      return this;
    }

    /** Applies a color to the internal {@link TextElement}; no-op for other content types. */
    public Cell color(KawaColor c) {
      if (content instanceof TextElement te) te.color(c);
      return this;
    }

    /** Sets font size on the internal {@link TextElement}; no-op for other content types. */
    public Cell fontSize(float s) {
      if (content instanceof TextElement te) te.fontSize(s);
      return this;
    }

    /** Sets center alignment on the internal {@link TextElement}; no-op for other content types. */
    public Cell centerAlign() {
      if (content instanceof TextElement te) te.centerAlign();
      return this;
    }

    /** Sets right alignment on the internal {@link TextElement}; no-op for other content types. */
    public Cell rightAlign() {
      if (content instanceof TextElement te) te.rightAlign();
      return this;
    }

    /** Sets left alignment on the internal {@link TextElement}; no-op for other content types. */
    public Cell leftAlign() {
      if (content instanceof TextElement te) te.align(TextElement.Align.LEFT);
      return this;
    }

    /** Sets a custom font on the internal {@link TextElement}; no-op for other content types. */
    public Cell font(KawaFont f) {
      if (content instanceof TextElement te) te.font(f);
      return this;
    }

    // --- Layout overrides ---
    public Cell background(KawaColor c) {
      this.cellBackground = c;
      return this;
    }

    public Cell padding(float all) {
      this.cellPaddingH = this.cellPaddingV = all;
      return this;
    }

    public Cell paddingH(float h) {
      this.cellPaddingH = h;
      return this;
    }

    public Cell paddingV(float v) {
      this.cellPaddingV = v;
      return this;
    }

    /**
     * Sets both horizontal and vertical padding using an explicit unit.
     *
     * @param all the padding value
     * @return this cell
     */
    public Cell padding(float all, Unit unit) {
      return padding(unit.toPoints(all));
    }

    /**
     * Sets horizontal padding using an explicit unit.
     *
     * @param h the horizontal padding value
     * @return this cell
     */
    public Cell paddingH(float h, Unit unit) {
      return paddingH(unit.toPoints(h));
    }

    /**
     * Sets vertical padding using an explicit unit.
     *
     * @param v the vertical padding value
     * @return this cell
     */
    public Cell paddingV(float v, Unit unit) {
      return paddingV(unit.toPoints(v));
    }

    // --- Per-side borders ---
    public Cell borderTop(float w, KawaColor c) {
      borderTopW = w;
      borderTopC = c;
      return this;
    }

    public Cell borderBottom(float w, KawaColor c) {
      borderBottomW = w;
      borderBottomC = c;
      return this;
    }

    public Cell borderLeft(float w, KawaColor c) {
      borderLeftW = w;
      borderLeftC = c;
      return this;
    }

    public Cell borderRight(float w, KawaColor c) {
      borderRightW = w;
      borderRightC = c;
      return this;
    }

    /**
     * Applies a {@link StyleSheet} to this cell. Background, padding, and border values are taken
     * from the sheet. Text style is applied to the internal {@link TextElement} if present.
     */
    public Cell style(StyleSheet s) {
      if (s.getBackground() != null) cellBackground = s.getBackground();
      float sph = (s.getPaddingLeft() + s.getPaddingRight()) / 2f;
      float spv = (s.getPaddingTop() + s.getPaddingBottom()) / 2f;
      if (sph > 0f) cellPaddingH = sph;
      if (spv > 0f) cellPaddingV = spv;
      if (s.getBorderTopWidth() > 0f) {
        borderTopW = s.getBorderTopWidth();
        borderTopC = s.getBorderTopColor();
      }
      if (s.getBorderBottomWidth() > 0f) {
        borderBottomW = s.getBorderBottomWidth();
        borderBottomC = s.getBorderBottomColor();
      }
      if (s.getBorderLeftWidth() > 0f) {
        borderLeftW = s.getBorderLeftWidth();
        borderLeftC = s.getBorderLeftColor();
      }
      if (s.getBorderRightWidth() > 0f) {
        borderRightW = s.getBorderRightWidth();
        borderRightC = s.getBorderRightColor();
      }
      if (s.getTextStyle() != null && content instanceof TextElement te) {
        s.getTextStyle().applyTo(te);
      }
      return this;
    }

    // --- Accessors ---
    public Element getContent() {
      return content;
    }

    public KawaColor getCellBackground() {
      return cellBackground;
    }

    public Float getCellPaddingH() {
      return cellPaddingH;
    }

    public Float getCellPaddingV() {
      return cellPaddingV;
    }

    // --- TextElement delegation helpers (only meaningful when content is a TextElement) ---

    /** Returns the text of the internal {@link TextElement}, or {@code ""} otherwise. */
    public String getText() {
      return content instanceof TextElement te ? te.getText() : "";
    }

    /** Returns {@code true} if the internal {@link TextElement} is bold. */
    public boolean isBold() {
      return content instanceof TextElement te && te.isBold();
    }

    /** Returns the color of the internal {@link TextElement}, or {@code null} otherwise. */
    public KawaColor getColor() {
      return content instanceof TextElement te ? te.getColor() : null;
    }
  }

  public static class Row {
    final List<Cell> cells = new ArrayList<>();
    KawaColor background = null;

    public Cell cell(String text) {
      Cell c = new Cell(text);
      cells.add(c);
      return c;
    }

    /** Creates an empty cell; set content via {@link Cell#text(String)} or {@link Cell#content}. */
    public Cell cell() {
      Cell c = new Cell();
      cells.add(c);
      return c;
    }

    public Row background(KawaColor bg) {
      this.background = bg;
      return this;
    }
  }

  public static class RowBuilder {
    private final Row row;

    RowBuilder(Row row) {
      this.row = row;
    }

    public Cell cell(String text) {
      return row.cell(text);
    }

    /** Creates an empty cell; set content via {@link Cell#text(String)} or {@link Cell#content}. */
    public Cell cell() {
      return row.cell();
    }

    public RowBuilder background(KawaColor bg) {
      row.background(bg);
      return this;
    }
  }
}
