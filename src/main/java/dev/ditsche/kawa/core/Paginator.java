package dev.ditsche.kawa.core;

import dev.ditsche.kawa.elements.ColumnElement;
import dev.ditsche.kawa.elements.Element;
import dev.ditsche.kawa.elements.PageBreakElement;
import dev.ditsche.kawa.font.FontRegistry;
import dev.ditsche.kawa.layout.LayoutContext;
import dev.ditsche.kawa.renderer.RenderContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

/**
 * Internal pagination engine.
 *
 * <p>The paginator measures content, determines page breaks, and renders each page.
 */
class Paginator {
  private static final float HEADER_FOOTER_GAP = 8f;

  private final PageDefinition def;
  private final FontRegistry fontRegistry;

  Paginator(PageDefinition def, FontRegistry fontRegistry) {
    this.def = def;
    this.fontRegistry = fontRegistry;
  }

  /** Renders all physical pages into {@code pdDocument}. Returns the number of pages produced. */
  int paginate(PDDocument pdDocument) throws IOException {
    Element content = def.getContent();
    if (content == null) {
      // No content → single blank page with header/footer
      renderPhysicalPage(pdDocument, 0f, 0f, 1, 1, def.getSize().height);
      return 1;
    }

    // --- Pass 1: measure total content height & calculate page count ---
    float pageContentWidth = def.contentWidth();
    LayoutContext measureCtx = new LayoutContext(0, 0, pageContentWidth, Float.MAX_VALUE);
    float totalContentHeight = content.measure(measureCtx);

    if (def.isFitContentToSinglePage()) {
      renderPhysicalPage(
          pdDocument, 0f, totalContentHeight, 1, 1, singlePageHeight(totalContentHeight));
      return 1;
    }

    List<Float> pageBreaks = calculatePageBreaks(pdDocument, totalContentHeight);
    int totalPages = pageBreaks.size();

    // --- Pass 2: render each physical page ---
    for (int i = 0; i < totalPages; i++) {
      float sliceStart = pageBreaks.get(i);
      float sliceEnd = (i + 1 < totalPages) ? pageBreaks.get(i + 1) : totalContentHeight;
      float sliceHeight = sliceEnd - sliceStart;
      renderPhysicalPage(
          pdDocument, sliceStart, sliceHeight, i + 1, totalPages, def.getSize().height);
    }

    return totalPages;
  }

  // -------------------------------------------------------------------------
  // Private helpers
  // -------------------------------------------------------------------------

  /**
   * Determines where each page starts (in content-space Y coordinates). Respects forced page breaks
   * declared by {@link PageBreakElement} children of the root {@link ColumnElement}.
   */
  private List<Float> calculatePageBreaks(PDDocument pdDocument, float totalContentHeight)
      throws IOException {

    List<Float> forcedBreaks =
        (def.getContent() instanceof ColumnElement col)
            ? col.findForcedBreakOffsets(def.contentWidth())
            : List.of();

    List<Float> breaks = new ArrayList<>();
    float cursor = 0f;

    while (cursor < totalContentHeight) {
      breaks.add(cursor);
      float available = availableContentHeight(pdDocument, breaks.size());
      float nextCursor = cursor + available;

      // Snap to the earliest forced break that falls within this page's slice
      float snap = -1f;
      for (float fb : forcedBreaks) {
        if (fb > cursor && fb < nextCursor) {
          snap = fb;
          break;
        }
      }
      cursor = snap >= 0f ? snap : nextCursor;
    }

    return breaks.isEmpty() ? List.of(0f) : breaks;
  }

  /**
   * Returns how many points of content fit on a given physical page, accounting for the space
   * consumed by header and footer.
   */
  private float availableContentHeight(PDDocument pdDocument, int pageNumber) throws IOException {
    PageSize size = def.getSize();
    float fullHeight = size.height - def.getMarginTop() - def.getMarginBottom();

    // Measure header height (if any)
    float headerHeight = 0f;
    if (def.getHeaderBuilder() != null) {
      ColumnElement headerEl = new ColumnElement();
      def.getHeaderBuilder().accept(headerEl, new PageContext(pageNumber, -1));
      LayoutContext hCtx = new LayoutContext(0, 0, def.contentWidth(), Float.MAX_VALUE);
      headerHeight = headerEl.measure(hCtx);
    }

    // Measure footer height (if any)
    float footerHeight = 0f;
    if (def.getFooterBuilder() != null) {
      ColumnElement footerEl = new ColumnElement();
      def.getFooterBuilder().accept(footerEl, new PageContext(pageNumber, -1));
      LayoutContext fCtx = new LayoutContext(0, 0, def.contentWidth(), Float.MAX_VALUE);
      footerHeight = footerEl.measure(fCtx);
    }

    float gap =
        (headerHeight > 0 ? HEADER_FOOTER_GAP : 0f) + (footerHeight > 0 ? HEADER_FOOTER_GAP : 0f);
    return fullHeight - headerHeight - footerHeight - gap;
  }

  private float singlePageHeight(float totalContentHeight) {
    float headerHeight = measureSlotHeight(def.getHeaderBuilder(), 1);
    float footerHeight = measureSlotHeight(def.getFooterBuilder(), 1);
    float gap =
        (headerHeight > 0 ? HEADER_FOOTER_GAP : 0f) + (footerHeight > 0 ? HEADER_FOOTER_GAP : 0f);
    float neededHeight =
        def.getMarginTop()
            + headerHeight
            + totalContentHeight
            + footerHeight
            + gap
            + def.getMarginBottom();
    return Math.max(def.getSize().height, neededHeight);
  }

  private float measureSlotHeight(
      java.util.function.BiConsumer<ColumnElement, PageContext> builder, int pageNumber) {
    if (builder == null) {
      return 0f;
    }
    ColumnElement slot = new ColumnElement();
    builder.accept(slot, new PageContext(pageNumber, -1));
    LayoutContext ctx = new LayoutContext(0, 0, def.contentWidth(), Float.MAX_VALUE);
    return slot.measure(ctx);
  }

  /** Renders a single physical page: background, header, content slice, footer, overlay. */
  private void renderPhysicalPage(
      PDDocument pdDocument,
      float contentOffsetY,
      float sliceHeight,
      int pageNumber,
      int totalPages,
      float pageHeight)
      throws IOException {
    PageSize size = def.getSize();
    PageContext ctx = new PageContext(pageNumber, totalPages);

    PDPage pdPage = new PDPage(new PDRectangle(size.width, pageHeight));
    pdDocument.addPage(pdPage);

    try (RenderContext renderCtx = new RenderContext(pdDocument, pdPage, fontRegistry)) {
      renderFullPageSlot(def.getBackgroundBuilder(), ctx, renderCtx, size.width, pageHeight);

      float ml = def.getMarginLeft();
      float mt = def.getMarginTop();
      float cw = def.contentWidth();
      float cursorY = mt; // tracks top-down position in page space

      // --- Header ---
      float headerHeight = 0f;
      if (def.getHeaderBuilder() != null) {
        ColumnElement headerEl = new ColumnElement();
        def.getHeaderBuilder().accept(headerEl, ctx);
        LayoutContext hCtx = new LayoutContext(ml, cursorY, cw, Float.MAX_VALUE);
        headerHeight = headerEl.measure(hCtx);
        headerEl.render(hCtx, renderCtx);
        cursorY += headerHeight + HEADER_FOOTER_GAP;
      }

      // --- Content slice ---
      if (def.getContent() != null && sliceHeight > 0) {
        LayoutContext contentCtx = new LayoutContext(ml, cursorY, cw, sliceHeight);
        def.getContent().renderSlice(contentCtx, renderCtx, contentOffsetY, sliceHeight);
      }

      // --- Footer ---
      if (def.getFooterBuilder() != null) {
        ColumnElement footerEl = new ColumnElement();
        def.getFooterBuilder().accept(footerEl, ctx);
        LayoutContext fMeasureCtx = new LayoutContext(ml, 0, cw, Float.MAX_VALUE);
        float footerHeight = footerEl.measure(fMeasureCtx);
        // Footer is pinned to bottom of page
        float footerY = pageHeight - def.getMarginBottom() - footerHeight;
        LayoutContext fCtx = new LayoutContext(ml, footerY, cw, footerHeight);
        footerEl.render(fCtx, renderCtx);
      }

      renderFullPageSlot(def.getOverlayBuilder(), ctx, renderCtx, size.width, pageHeight);
    }
  }

  private void renderFullPageSlot(
      BiConsumer<ColumnElement, PageContext> builder,
      PageContext pageCtx,
      RenderContext renderCtx,
      float pageWidth,
      float pageHeight) {
    if (builder == null) {
      return;
    }
    ColumnElement slot = new ColumnElement();
    builder.accept(slot, pageCtx);
    slot.render(new LayoutContext(0, 0, pageWidth, pageHeight), renderCtx);
  }
}
