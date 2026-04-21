package dev.ditsche.kawa.core;

import dev.ditsche.kawa.units.Unit;

/**
 * Standard page sizes in PDF user units (points, 1 pt = 1/72 inch). Predefined portrait sizes are
 * available as constants.
 *
 * @author Tobias Dittmann
 */
public final class PageSize {

  public static final PageSize A4 = new PageSize(595.28f, 841.89f);
  public static final PageSize A3 = new PageSize(841.89f, 1190.55f);
  public static final PageSize A5 = new PageSize(419.53f, 595.28f);
  public static final PageSize LETTER = new PageSize(612f, 792f);
  public static final PageSize LEGAL = new PageSize(612f, 1008f);

  public final float width;
  public final float height;

  private PageSize(float width, float height) {
    this.width = width;
    this.height = height;
  }

  /** Creates a custom page size in points. */
  public static PageSize custom(float width, float height) {
    return new PageSize(width, height);
  }

  /** Creates a custom page size in the specified unit. */
  public static PageSize custom(float width, float height, Unit unit) {
    return new PageSize(unit.toPoints(width), unit.toPoints(height));
  }

  /** Returns a landscape variant of this page size (width and height swapped). */
  public PageSize landscape() {
    return new PageSize(height, width);
  }
}
