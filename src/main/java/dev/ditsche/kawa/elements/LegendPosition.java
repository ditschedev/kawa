package dev.ditsche.kawa.elements;

/**
 * Where the series legend is rendered in a chart element.
 *
 * @author Tobias Dittmann
 */
public enum LegendPosition {
  /** Draws the series name in a box to the right of the chart area. This is the default for all chart types. */
  TOP_RIGHT,
  /** Draws the series name at the bottom of the chart area. */
  BOTTOM,
  /** Draws the series name directly inside each bar (bar charts only). */
  ON_BAR,
  /** Does not draw the series legend. */
  NONE
}
