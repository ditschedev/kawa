package dev.ditsche.kawa.style;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ColorsTest {

  private static void assertRgb(KawaColor color, int r, int g, int b) {
    assertEquals(r, color.getRed());
    assertEquals(g, color.getGreen());
    assertEquals(b, color.getBlue());
  }

  @Test
  void blue50() {
    assertRgb(Colors.BLUE_50, 0xEF, 0xF6, 0xFF);
  }

  @Test
  void blue900() {
    assertRgb(Colors.BLUE_900, 0x1E, 0x3A, 0x8A);
  }

  @Test
  void gray100() {
    assertRgb(Colors.GRAY_100, 0xF3, 0xF4, 0xF6);
  }

  @Test
  void gray200() {
    assertRgb(Colors.GRAY_200, 0xE5, 0xE7, 0xEB);
  }
}
