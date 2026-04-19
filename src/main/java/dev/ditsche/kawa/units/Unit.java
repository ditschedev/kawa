package dev.ditsche.kawa.units;

/**
 * Length units for PDF layout. 1 pt = 1/72 inch (PDF user-space unit).
 *
 * <p>Pass a unit alongside a numeric value to any method that accepts one:
 *
 * <pre>{@code
 * new SpacerElement(5, Unit.MM)
 * .padding(1, Unit.CM)
 * .margin(0.5f, Unit.IN)
 * }</pre>
 */
public enum Unit {
  /** PDF points — the native unit. 1 pt = 1/72 in. */
  PT(1f),
  /** Inches. 1 in = 72 pt. */
  IN(72f),
  /** Centimetres. 1 cm = 72/2.54 pt. */
  CM(72f / 2.54f),
  /** Millimetres. 1 mm = 72/25.4 pt. */
  MM(72f / 25.4f),
  /** CSS pixels at 96 dpi. 1 px = 72/96 pt = 0.75 pt. */
  PX(72f / 96f);

  private final float pointsPerUnit;

  Unit(float pointsPerUnit) {
    this.pointsPerUnit = pointsPerUnit;
  }

  /** Converts {@code value} in this unit to PDF points. */
  public float toPoints(float value) {
    return value * pointsPerUnit;
  }
}
