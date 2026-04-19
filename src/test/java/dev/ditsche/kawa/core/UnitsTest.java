package dev.ditsche.kawa.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.ditsche.kawa.elements.QrCodeElement;
import dev.ditsche.kawa.style.StyleSheet;
import dev.ditsche.kawa.units.Unit;
import org.junit.jupiter.api.Test;

class UnitsTest {

  @Test
  void pageSizeCustom_acceptsMetricUnits() {
    PageSize size = PageSize.custom(21f, 29.7f, Unit.CM);

    assertEquals(595.28f, size.width, 0.2f);
    assertEquals(841.89f, size.height, 0.2f);
  }

  @Test
  void pageDefinitionMargins_acceptUnitLengths() {
    PageDefinition page =
        new PageDefinition()
            .marginX(2f, Unit.CM)
            .marginTop(10f, Unit.MM)
            .marginBottom(0.5f, Unit.IN);

    assertEquals(56.69f, page.getMarginLeft(), 0.05f);
    assertEquals(56.69f, page.getMarginRight(), 0.05f);
    assertEquals(28.35f, page.getMarginTop(), 0.05f);
    assertEquals(36f, page.getMarginBottom(), 0.05f);
  }

  @Test
  void styleSheetPadding_acceptsUnitLengths() {
    StyleSheet style = new StyleSheet().paddingH(1f, Unit.CM).paddingV(5f, Unit.MM);

    assertEquals(28.35f, style.getPaddingLeft(), 0.05f);
    assertEquals(28.35f, style.getPaddingRight(), 0.05f);
    assertEquals(14.17f, style.getPaddingTop(), 0.05f);
    assertEquals(14.17f, style.getPaddingBottom(), 0.05f);
  }

  @Test
  void sizeOperations_acceptPixelAndMetricUnits() {
    QrCodeElement qr = new QrCodeElement("https://example.com").size(96f, Unit.PX);
    assertEquals(72f, qr.measure(null), 0.05f);
  }
}
