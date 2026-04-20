package dev.ditsche.kawa.core;

import dev.ditsche.kawa.elements.ColumnElement;
import dev.ditsche.kawa.elements.Element;
import dev.ditsche.kawa.units.Unit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Defines the structure of pages in a document.
 *
 * <p>A page definition acts as a template for page size, margins, background, header, footer,
 * overlay, and content. The paginator uses this template to produce the required number of physical
 * PDF pages.
 */
public class PageDefinition {

  // --- Page geometry ---
  private PageSize size = PageSize.A4;
  private float marginTop = 40f;
  private float marginRight = 40f;
  private float marginBottom = 40f;
  private float marginLeft = 40f;
  private boolean fitContentToSinglePage = false;

  // --- Slots ---
  private BiConsumer<ColumnElement, PageContext> backgroundBuilder;
  private BiConsumer<ColumnElement, PageContext> headerBuilder;
  private BiConsumer<ColumnElement, PageContext> footerBuilder;
  private BiConsumer<ColumnElement, PageContext> overlayBuilder;
  private Element content;

  // -------------------------------------------------------------------------
  // Geometry
  // -------------------------------------------------------------------------

  public PageDefinition size(PageSize s) {
    this.size = s;
    return this;
  }

  /** Sets the same margin on all four sides in points. */
  public PageDefinition margin(float all) {
    this.marginTop = this.marginRight = this.marginBottom = this.marginLeft = all;
    return this;
  }

  public PageDefinition margin(float all, Unit unit) {
    return margin(unit.toPoints(all));
  }

  public PageDefinition marginTop(float v) {
    this.marginTop = v;
    return this;
  }

  public PageDefinition marginTop(float v, Unit unit) {
    return marginTop(unit.toPoints(v));
  }

  public PageDefinition marginRight(float v) {
    this.marginRight = v;
    return this;
  }

  public PageDefinition marginRight(float v, Unit unit) {
    return marginRight(unit.toPoints(v));
  }

  public PageDefinition marginBottom(float v) {
    this.marginBottom = v;
    return this;
  }

  public PageDefinition marginBottom(float v, Unit unit) {
    return marginBottom(unit.toPoints(v));
  }

  public PageDefinition marginLeft(float v) {
    this.marginLeft = v;
    return this;
  }

  public PageDefinition marginLeft(float v, Unit unit) {
    return marginLeft(unit.toPoints(v));
  }

  /** Sets the left and right margins in points. */
  public PageDefinition marginX(float v) {
    this.marginLeft = this.marginRight = v;
    return this;
  }

  public PageDefinition marginX(float v, Unit unit) {
    return marginX(unit.toPoints(v));
  }

  /** Sets the top and bottom margins in points. */
  public PageDefinition marginY(float v) {
    this.marginTop = this.marginBottom = v;
    return this;
  }

  public PageDefinition marginY(float v, Unit unit) {
    return marginY(unit.toPoints(v));
  }

  /**
   * Renders the document onto a single physical page whose height grows to fit header, content, and
   * footer. Useful for receipts and other roll-paper formats.
   */
  public PageDefinition fitContentToSinglePage() {
    this.fitContentToSinglePage = true;
    return this;
  }

  // -------------------------------------------------------------------------
  // Background
  // -------------------------------------------------------------------------

  /** Static background — rendered behind all page content and independent of margins. */
  public PageDefinition background(Consumer<ColumnElement> builder) {
    this.backgroundBuilder = (col, ctx) -> builder.accept(col);
    return this;
  }

  /** Dynamic background — receives the current {@link PageContext}. */
  public PageDefinition background(BiConsumer<ColumnElement, PageContext> builder) {
    this.backgroundBuilder = builder;
    return this;
  }

  // -------------------------------------------------------------------------
  // Header
  // -------------------------------------------------------------------------

  /** Static header — same on every page. */
  public PageDefinition header(Consumer<ColumnElement> builder) {
    this.headerBuilder = (col, ctx) -> builder.accept(col);
    return this;
  }

  /** Dynamic header — receives the current {@link PageContext}. */
  public PageDefinition header(BiConsumer<ColumnElement, PageContext> builder) {
    this.headerBuilder = builder;
    return this;
  }

  // -------------------------------------------------------------------------
  // Footer
  // -------------------------------------------------------------------------

  /** Static footer — same on every page. */
  public PageDefinition footer(Consumer<ColumnElement> builder) {
    this.footerBuilder = (col, ctx) -> builder.accept(col);
    return this;
  }

  /** Dynamic footer — receives the current {@link PageContext}. */
  public PageDefinition footer(BiConsumer<ColumnElement, PageContext> builder) {
    this.footerBuilder = builder;
    return this;
  }

  // -------------------------------------------------------------------------
  // Overlay
  // -------------------------------------------------------------------------

  /** Static overlay — rendered above all page content and independent of margins. */
  public PageDefinition overlay(Consumer<ColumnElement> builder) {
    this.overlayBuilder = (col, ctx) -> builder.accept(col);
    return this;
  }

  /** Dynamic overlay — receives the current {@link PageContext}. */
  public PageDefinition overlay(BiConsumer<ColumnElement, PageContext> builder) {
    this.overlayBuilder = builder;
    return this;
  }

  // -------------------------------------------------------------------------
  // Content
  // -------------------------------------------------------------------------

  /** Sets the content element directly. */
  public PageDefinition content(Element element) {
    this.content = element;
    return this;
  }

  /** Builds the content as a column using a lambda. */
  public PageDefinition content(Consumer<ColumnElement> builder) {
    ColumnElement col = new ColumnElement();
    builder.accept(col);
    this.content = col;
    return this;
  }

  // -------------------------------------------------------------------------
  // Package-private accessors — used by Paginator
  // -------------------------------------------------------------------------

  PageSize getSize() {
    return size;
  }

  float getMarginTop() {
    return marginTop;
  }

  float getMarginRight() {
    return marginRight;
  }

  float getMarginBottom() {
    return marginBottom;
  }

  float getMarginLeft() {
    return marginLeft;
  }

  Element getContent() {
    return content;
  }

  BiConsumer<ColumnElement, PageContext> getBackgroundBuilder() {
    return backgroundBuilder;
  }

  BiConsumer<ColumnElement, PageContext> getHeaderBuilder() {
    return headerBuilder;
  }

  BiConsumer<ColumnElement, PageContext> getFooterBuilder() {
    return footerBuilder;
  }

  BiConsumer<ColumnElement, PageContext> getOverlayBuilder() {
    return overlayBuilder;
  }

  boolean isFitContentToSinglePage() {
    return fitContentToSinglePage;
  }

  float contentWidth() {
    return size.width - marginLeft - marginRight;
  }

  float contentHeight() {
    return size.height - marginTop - marginBottom;
  }
}
