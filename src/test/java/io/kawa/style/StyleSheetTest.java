package io.kawa.style;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class StyleSheetTest {

  @Test
  void padding_setsAllSides() {
    StyleSheet s = StyleSheet.of(ss -> ss.padding(10f));
    assertEquals(10f, s.getPaddingTop());
    assertEquals(10f, s.getPaddingRight());
    assertEquals(10f, s.getPaddingBottom());
    assertEquals(10f, s.getPaddingLeft());
    assertEquals(20f, s.totalPaddingH());
    assertEquals(20f, s.totalPaddingV());
  }

  @Test
  void paddingHV_setsCorrectSides() {
    StyleSheet s = StyleSheet.of(ss -> ss.paddingH(8f).paddingV(4f));
    assertEquals(4f, s.getPaddingTop());
    assertEquals(8f, s.getPaddingRight());
    assertEquals(4f, s.getPaddingBottom());
    assertEquals(8f, s.getPaddingLeft());
  }

  @Test
  void border_setsAllSides() {
    StyleSheet s = StyleSheet.of(ss -> ss.border(1f, Colors.GRAY_300));
    assertEquals(1f, s.getBorderTopWidth());
    assertEquals(1f, s.getBorderRightWidth());
    assertEquals(1f, s.getBorderBottomWidth());
    assertEquals(1f, s.getBorderLeftWidth());
    assertEquals(Colors.GRAY_300, s.getBorderTopColor());
    assertEquals(Colors.GRAY_300, s.getBorderLeftColor());
  }

  @Test
  void borderBottom_onlySetsBottomSide() {
    StyleSheet s = StyleSheet.of(ss -> ss.borderBottom(2f, Colors.BLUE_200));
    assertEquals(0f, s.getBorderTopWidth());
    assertEquals(2f, s.getBorderBottomWidth());
    assertEquals(Colors.BLUE_200, s.getBorderBottomColor());
    assertNull(s.getBorderTopColor());
  }

  @Test
  void background_isStored() {
    StyleSheet s = StyleSheet.of(ss -> ss.background(Colors.BLUE_50));
    assertEquals(Colors.BLUE_50, s.getBackground());
  }

  @Test
  void textStyle_nestedConfig() {
    StyleSheet s =
        StyleSheet.of(ss -> ss.textStyle(ts -> ts.bold().color(Colors.BLUE_900).fontSize(10f)));
    assertNotNull(s.getTextStyle());
    assertEquals(700, s.getTextStyle().getWeight());
    assertEquals(Colors.BLUE_900, s.getTextStyle().getColor());
    assertEquals(10f, s.getTextStyle().getFontSize());
  }
}
