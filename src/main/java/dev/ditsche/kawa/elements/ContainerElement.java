package dev.ditsche.kawa.elements;

import dev.ditsche.kawa.layout.LayoutContext;
import dev.ditsche.kawa.renderer.RenderContext;
import dev.ditsche.kawa.style.KawaColor;
import dev.ditsche.kawa.style.StyleSheet;
import java.io.IOException;
import java.util.function.Consumer;

/** Wraps an element with background, padding, and border styling. */
public non-sealed class ContainerElement implements LayoutElement {

  private final StyleSheet style;
  private Element child;

  public ContainerElement(StyleSheet style) {
    this.style = style;
  }

  /** Factory that builds the StyleSheet inline. */
  public static ContainerElement of(Consumer<StyleSheet> config) {
    return new ContainerElement(StyleSheet.of(config));
  }

  /** Factory that uses an existing StyleSheet. */
  public static ContainerElement of(StyleSheet style) {
    return new ContainerElement(style);
  }

  /** Sets the wrapped child element. Returns {@code this} for chaining. */
  public ContainerElement containing(Element child) {
    this.child = child;
    return this;
  }

  // -------------------------------------------------------------------------
  // Element contract
  // -------------------------------------------------------------------------

  @Override
  public float measure(LayoutContext context) {
    float innerW = Math.max(0f, context.width() - style.totalPaddingH());
    LayoutContext innerCtx =
        new LayoutContext(
            context.x() + style.getPaddingLeft(),
            context.y() + style.getPaddingTop(),
            innerW,
            context.height());
    float childH = child != null ? child.measure(innerCtx) : 0f;
    return childH + style.totalPaddingV();
  }

  @Override
  public void render(LayoutContext context, RenderContext renderCtx) {
    renderSlice(context, renderCtx, 0f, Float.MAX_VALUE);
  }

  /**
   * Renders the visible slice of this container.
   *
   * @param offsetY vertical offset from the top of the container in points
   * @param availableHeight visible height in points
   */
  @Override
  public void renderSlice(
      LayoutContext context, RenderContext renderCtx, float offsetY, float availableHeight) {
    try {
      float pageH = renderCtx.getPage().getMediaBox().getHeight();
      float totalH = measure(context);
      float visibleH = Math.min(totalH - offsetY, availableHeight);
      if (visibleH <= 0f) return;

      // Draw background
      KawaColor bg = style.getBackground();
      if (bg != null) {
        float pdfY = pageH - context.y() - visibleH;
        renderCtx.drawRect(context.x(), pdfY, context.width(), visibleH, bg);
      }

      // Draw borders
      drawBorders(context, renderCtx, pageH, visibleH, offsetY, totalH);

      // Render child (with inset and slice adjustment)
      if (child != null) {
        float pt = style.getPaddingTop();
        float pl = style.getPaddingLeft();
        float pr = style.getPaddingRight();

        // Child occupies [pt, totalH - pb] in container-space
        float childStart = pt;
        float childEnd = totalH - style.getPaddingBottom();

        float visibleChildTop = Math.max(offsetY, childStart);
        float visibleChildBottom = Math.min(offsetY + availableHeight, childEnd);

        if (visibleChildTop < visibleChildBottom) {
          float childOffsetY = visibleChildTop - childStart;
          float childAvailable = visibleChildBottom - visibleChildTop;
          float childPageY = context.y() + (visibleChildTop - offsetY);
          float innerW = Math.max(0f, context.width() - pl - pr);

          LayoutContext childCtx =
              new LayoutContext(context.x() + pl, childPageY, innerW, childAvailable);
          child.renderSlice(childCtx, renderCtx, childOffsetY, childAvailable);
        }
      }

    } catch (IOException e) {
      throw new KawaRenderException("Failed to render container", e);
    }
  }

  // -------------------------------------------------------------------------
  // Private helpers
  // -------------------------------------------------------------------------

  private void drawBorders(
      LayoutContext ctx,
      RenderContext renderCtx,
      float pageH,
      float visibleH,
      float offsetY,
      float totalH)
      throws IOException {
    float x1 = ctx.x();
    float x2 = ctx.x() + ctx.width();
    float pdfTop = pageH - ctx.y(); // PDF coords: top edge
    float pdfBottom = pageH - ctx.y() - visibleH; // PDF coords: bottom edge

    // Top border — only when we are at the very top of the container
    if (offsetY <= 0f && style.getBorderTopWidth() > 0 && style.getBorderTopColor() != null) {
      renderCtx.drawLine(
          x1, pdfTop, x2, pdfTop, style.getBorderTopColor(), style.getBorderTopWidth());
    }

    // Bottom border — only when we are at the very bottom
    if (offsetY + visibleH >= totalH
        && style.getBorderBottomWidth() > 0
        && style.getBorderBottomColor() != null) {
      renderCtx.drawLine(
          x1, pdfBottom, x2, pdfBottom, style.getBorderBottomColor(), style.getBorderBottomWidth());
    }

    // Left/right borders run the full visible height
    if (style.getBorderLeftWidth() > 0 && style.getBorderLeftColor() != null) {
      renderCtx.drawLine(
          x1, pdfTop, x1, pdfBottom, style.getBorderLeftColor(), style.getBorderLeftWidth());
    }

    if (style.getBorderRightWidth() > 0 && style.getBorderRightColor() != null) {
      renderCtx.drawLine(
          x2, pdfTop, x2, pdfBottom, style.getBorderRightColor(), style.getBorderRightWidth());
    }
  }
}
