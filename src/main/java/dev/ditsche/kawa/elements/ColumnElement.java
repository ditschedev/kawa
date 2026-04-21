package dev.ditsche.kawa.elements;

import dev.ditsche.kawa.layout.LayoutContext;
import dev.ditsche.kawa.renderer.RenderContext;
import dev.ditsche.kawa.style.KawaColor;
import dev.ditsche.kawa.style.StyleSheet;
import dev.ditsche.kawa.units.Unit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Arranges child elements vertically with optional spacing.
 *
 * @author Tobias Dittmann
 */
public non-sealed class ColumnElement implements LayoutElement {

  private final List<Element> children = new ArrayList<>();
  private float spacing = 4f; // gap between items in points
  private StyleSheet style = null;

  public ColumnElement() {}

  public ColumnElement(Consumer<ColumnElement> builder) {
    builder.accept(this);
  }

  // -------------------------------------------------------------------------
  // Fluent builder
  // -------------------------------------------------------------------------

  /** Adds an arbitrary element as the next item. */
  public ColumnElement add(Element element) {
    children.add(element);
    return this;
  }

  /** Convenience: adds a text element and returns a reference for further styling. */
  public TextElement text(String content) {
    TextElement el = new TextElement(content);
    children.add(el);
    return el;
  }

  /** Convenience: starts building a nested column. */
  public ColumnElement column(Consumer<ColumnElement> builder) {
    ColumnElement nested = new ColumnElement(builder);
    children.add(nested);
    return this;
  }

  /** Returns a {@link SlotBuilder} — lets callers add any element to the next slot. */
  public SlotBuilder item() {
    return new SlotBuilder(this);
  }

  public ColumnElement spacing(float gap) {
    this.spacing = gap;
    return this;
  }

  public ColumnElement spacing(float gap, Unit unit) {
    return spacing(unit.toPoints(gap));
  }

  /**
   * Returns the content-space Y offsets at which direct {@link PageBreakElement} children force a
   * page transition. Accounts for element heights and inter-element spacing, so the returned offset
   * is the start position of the first child that should appear on the next page.
   */
  public List<Float> findForcedBreakOffsets(float contentWidth) {
    List<Float> offsets = new ArrayList<>();
    float cursor = 0f;
    for (int i = 0; i < children.size(); i++) {
      Element child = children.get(i);
      float childH = child.measure(new LayoutContext(0, 0, contentWidth, Float.MAX_VALUE));
      float gap = (i < children.size() - 1) ? spacing : 0f;
      if (child instanceof PageBreakElement) {
        offsets.add(cursor + childH + gap);
      }
      cursor += childH + gap;
    }
    return offsets;
  }

  /**
   * Applies a {@link StyleSheet} to this column: background, padding, and per-side borders are
   * rendered around the column's children.
   */
  public ColumnElement style(StyleSheet s) {
    this.style = s;
    return this;
  }

  // -------------------------------------------------------------------------
  // Element contract
  // -------------------------------------------------------------------------

  @Override
  public float measure(LayoutContext context) {
    if (style != null) {
      LayoutContext inner = innerContext(context, context.y());
      return measureChildren(inner) + style.totalPaddingV();
    }
    return measureChildren(context);
  }

  private float measureChildren(LayoutContext ctx) {
    float total = 0f;
    for (int i = 0; i < children.size(); i++) {
      total += children.get(i).measure(ctx);
      if (i < children.size() - 1) total += spacing;
    }
    return total;
  }

  private LayoutContext innerContext(LayoutContext context, float y) {
    float pl = style.getPaddingLeft();
    float pt = style.getPaddingTop();
    float innerW = Math.max(0f, context.width() - style.totalPaddingH());
    return new LayoutContext(context.x() + pl, y + pt, innerW, context.height());
  }

  @Override
  public void render(LayoutContext context, RenderContext renderCtx) {
    renderSlice(context, renderCtx, 0f, Float.MAX_VALUE);
  }

  /**
   * Renders only the vertical slice of this column that falls within [offsetY, offsetY +
   * availableHeight] in content space.
   *
   * <p>Children fully above the slice are skipped. Children fully below are skipped. Children
   * straddling a page boundary are sliced recursively.
   */
  @Override
  public void renderSlice(
      LayoutContext context, RenderContext renderCtx, float offsetY, float availableHeight) {
    if (style != null) {
      renderSliceWithStyle(context, renderCtx, offsetY, availableHeight);
      return;
    }
    renderChildrenSlice(context, renderCtx, offsetY, availableHeight);
  }

  private void renderSliceWithStyle(
      LayoutContext context, RenderContext renderCtx, float offsetY, float availableHeight) {
    try {
      float pageH = renderCtx.getPage().getMediaBox().getHeight();
      float totalH = measure(context);
      float visibleH = Math.min(totalH - offsetY, availableHeight);
      if (visibleH <= 0f) return;

      // Background
      KawaColor bg = style.getBackground();
      if (bg != null) {
        float pdfY = pageH - context.y() - visibleH;
        renderCtx.drawRect(context.x(), pdfY, context.width(), visibleH, bg);
      }

      // Borders
      float x1 = context.x(), x2 = context.x() + context.width();
      float pdfTop = pageH - context.y();
      float pdfBottom = pageH - context.y() - visibleH;
      if (offsetY <= 0f && style.getBorderTopWidth() > 0 && style.getBorderTopColor() != null)
        renderCtx.drawLine(
            x1, pdfTop, x2, pdfTop, style.getBorderTopColor(), style.getBorderTopWidth());
      if (offsetY + visibleH >= totalH
          && style.getBorderBottomWidth() > 0
          && style.getBorderBottomColor() != null)
        renderCtx.drawLine(
            x1,
            pdfBottom,
            x2,
            pdfBottom,
            style.getBorderBottomColor(),
            style.getBorderBottomWidth());
      if (style.getBorderLeftWidth() > 0 && style.getBorderLeftColor() != null)
        renderCtx.drawLine(
            x1, pdfTop, x1, pdfBottom, style.getBorderLeftColor(), style.getBorderLeftWidth());
      if (style.getBorderRightWidth() > 0 && style.getBorderRightColor() != null)
        renderCtx.drawLine(
            x2, pdfTop, x2, pdfBottom, style.getBorderRightColor(), style.getBorderRightWidth());

      // Children with padding inset and slice adjustment
      float pt = style.getPaddingTop();
      float childStart = pt;
      float childEnd = totalH - style.getPaddingBottom();
      float visChildTop = Math.max(offsetY, childStart);
      float visChildBottom = Math.min(offsetY + availableHeight, childEnd);

      if (visChildTop < visChildBottom) {
        float childOffsetY = visChildTop - childStart;
        float childAvailable = visChildBottom - visChildTop;
        float childPageY = context.y() + (visChildTop - offsetY);
        LayoutContext innerCtx = innerContext(context, childPageY - pt);
        renderChildrenSlice(innerCtx, renderCtx, childOffsetY, childAvailable);
      }
    } catch (IOException e) {
      throw new KawaRenderException("Failed to render styled column", e);
    }
  }

  private void renderChildrenSlice(
      LayoutContext context, RenderContext renderCtx, float offsetY, float availableHeight) {
    float cursor = 0f;
    float pageTop = offsetY;
    float pageBottom = offsetY + availableHeight;

    for (int i = 0; i < children.size(); i++) {
      Element child = children.get(i);
      float childHeight = child.measure(context);
      float childTop = cursor;
      float childBottom = cursor + childHeight;

      // Skip children entirely above the slice window
      if (childBottom <= pageTop) {
        cursor = childBottom + (i < children.size() - 1 ? spacing : 0f);
        continue;
      }

      // Stop if child starts below the slice window
      if (childTop >= pageBottom) break;

      // Where on the current page this child's top lands
      float drawY = context.y() + (childTop - pageTop);
      float childOffsetY = Math.max(0f, pageTop - childTop);
      float childAvailable = Math.min(childBottom, pageBottom) - Math.max(childTop, pageTop);

      LayoutContext childCtx =
          new LayoutContext(context.x(), drawY, context.width(), childAvailable);
      child.renderSlice(childCtx, renderCtx, childOffsetY, childAvailable);

      cursor = childBottom + (i < children.size() - 1 ? spacing : 0f);
    }
  }

  // -------------------------------------------------------------------------
  // SlotBuilder — returned by item(), allows adding any element type
  // -------------------------------------------------------------------------

  public static class SlotBuilder {
    private final ColumnElement parent;

    SlotBuilder(ColumnElement parent) {
      this.parent = parent;
    }

    public TextElement text(String content) {
      return parent.text(content);
    }

    public ColumnElement column(Consumer<ColumnElement> builder) {
      parent.column(builder);
      return parent;
    }

    public RowElement row(Consumer<RowElement> builder) {
      RowElement el = new RowElement(builder);
      parent.add(el);
      return el;
    }

    public TableElement table(Consumer<TableElement> builder) {
      TableElement el = new TableElement(builder);
      parent.add(el);
      return el;
    }

    public ImageElement image(ImageElement image) {
      parent.add(image);
      return image;
    }

    public CustomElement custom(ElementRenderer renderer) {
      CustomElement el = new CustomElement(renderer);
      parent.add(el);
      return el;
    }

    public BarChartElement barChart(Consumer<BarChartElement> builder) {
      BarChartElement el = new BarChartElement(builder);
      parent.add(el);
      return el;
    }

    public LineChartElement lineChart(Consumer<LineChartElement> builder) {
      LineChartElement el = new LineChartElement(builder);
      parent.add(el);
      return el;
    }

    public PieChartElement pieChart(Consumer<PieChartElement> builder) {
      PieChartElement el = new PieChartElement(builder);
      parent.add(el);
      return el;
    }
  }
}
