package io.kawa.font;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class KawaFontTest {

  // Dummy sources — paths don't need to exist; we only test resolution logic.
  private static final FontSource THIN = new FileFontSource("thin");
  private static final FontSource REGULAR = new FileFontSource("regular");
  private static final FontSource SEMI_BOLD = new FileFontSource("semibold");
  private static final FontSource BOLD = new FileFontSource("bold");
  private static final FontSource ITALIC = new FileFontSource("italic");
  private static final FontSource BOLD_ITALIC = new FileFontSource("bold-italic");

  // -------------------------------------------------------------------------
  // Builder validation
  // -------------------------------------------------------------------------

  @Test
  void buildWithNoUprightSourceThrows() {
    assertThrows(IllegalArgumentException.class, () -> KawaFont.builder().build());
  }

  @Test
  void buildWithUprightSourceSucceeds() {
    assertDoesNotThrow(() -> KawaFont.builder().regular(REGULAR).build());
  }

  // -------------------------------------------------------------------------
  // Exact weight resolution
  // -------------------------------------------------------------------------

  @Test
  void resolvesExactWeightMatch() {
    KawaFont font = KawaFont.builder().weight(400, REGULAR).weight(700, BOLD).build();

    assertSame(REGULAR, font.resolveSource(400, false));
    assertSame(BOLD, font.resolveSource(700, false));
  }

  // -------------------------------------------------------------------------
  // Nearest-weight resolution
  // -------------------------------------------------------------------------

  @Test
  void resolvesNearestLowerWeightWhenCloser() {
    // 400 and 700 registered; 500 is 100 away from 400, 200 away from 700 → 400
    KawaFont font = KawaFont.builder().weight(400, REGULAR).weight(700, BOLD).build();
    assertSame(REGULAR, font.resolveSource(500, false));
  }

  @Test
  void resolvesNearestHigherWeightWhenCloser() {
    // 400 and 700 registered; 600 is 200 away from 400, 100 away from 700 → 700
    KawaFont font = KawaFont.builder().weight(400, REGULAR).weight(700, BOLD).build();
    assertSame(BOLD, font.resolveSource(600, false));
  }

  @Test
  void tieBreaksToHeavierWeight() {
    // 400 and 600 registered; 500 is equidistant → returns the heavier (600)
    KawaFont font = KawaFont.builder().weight(400, REGULAR).weight(600, SEMI_BOLD).build();
    assertSame(SEMI_BOLD, font.resolveSource(500, false));
  }

  @Test
  void resolvesBelowLowestRegisteredWeight() {
    KawaFont font = KawaFont.builder().weight(400, REGULAR).weight(700, BOLD).build();
    assertSame(REGULAR, font.resolveSource(100, false));
  }

  @Test
  void resolvesAboveHighestRegisteredWeight() {
    KawaFont font = KawaFont.builder().weight(400, REGULAR).weight(700, BOLD).build();
    assertSame(BOLD, font.resolveSource(900, false));
  }

  @Test
  void resolvesOnlySingleSourceForAnyWeight() {
    KawaFont font = KawaFont.builder().weight(400, REGULAR).build();

    assertSame(REGULAR, font.resolveSource(100, false));
    assertSame(REGULAR, font.resolveSource(400, false));
    assertSame(REGULAR, font.resolveSource(900, false));
  }

  // -------------------------------------------------------------------------
  // Italic resolution
  // -------------------------------------------------------------------------

  @Test
  void italicFallsBackToUprightWhenNoItalicSourcesRegistered() {
    KawaFont font = KawaFont.builder().weight(400, REGULAR).build();
    assertSame(REGULAR, font.resolveSource(400, true));
  }

  @Test
  void resolvesItalicSourceWhenAvailable() {
    KawaFont font =
        KawaFont.builder().weight(400, REGULAR).weightItalic(400, ITALIC).build();

    assertSame(ITALIC, font.resolveSource(400, true));
    assertSame(REGULAR, font.resolveSource(400, false));
  }

  @Test
  void italicUsesNearestWeightFromItalicMap() {
    KawaFont font =
        KawaFont.builder()
            .weight(400, REGULAR)
            .weight(700, BOLD)
            .weightItalic(400, ITALIC)
            .weightItalic(700, BOLD_ITALIC)
            .build();

    assertSame(ITALIC, font.resolveSource(400, true));
    assertSame(BOLD_ITALIC, font.resolveSource(700, true));
    // 500 → closer to 400 italic
    assertSame(ITALIC, font.resolveSource(500, true));
  }

  @Test
  void italicFallsBackToUprightForWeightsMissingFromItalicMap() {
    // Only upright registered — any italic request should use upright nearest
    KawaFont font =
        KawaFont.builder().weight(400, REGULAR).weight(700, BOLD).build();

    assertSame(REGULAR, font.resolveSource(400, true));
    assertSame(BOLD, font.resolveSource(700, true));
  }

  // -------------------------------------------------------------------------
  // Quick factories
  // -------------------------------------------------------------------------

  @Test
  void ofFileCreatesRegularFileFontSource() {
    KawaFont font = KawaFont.ofFile("/path/to/font.ttf");
    FontSource source = font.resolveSource(FontWeight.REGULAR, false);

    assertInstanceOf(FileFontSource.class, source);
    assertEquals("/path/to/font.ttf", ((FileFontSource) source).path());
  }

  @Test
  void ofResourceCreatesRegularResourceFontSource() {
    KawaFont font = KawaFont.ofResource("/fonts/regular.ttf");
    FontSource source = font.resolveSource(FontWeight.REGULAR, false);

    assertInstanceOf(ResourceFontSource.class, source);
    assertEquals("/fonts/regular.ttf", ((ResourceFontSource) source).path());
  }

  // -------------------------------------------------------------------------
  // Builder named shortcuts
  // -------------------------------------------------------------------------

  @Test
  void namedWeightShortcutsMapToCorrectWeights() {
    KawaFont font =
        KawaFont.builder()
            .thin(THIN)
            .regular(REGULAR)
            .semiBold(SEMI_BOLD)
            .bold(BOLD)
            .build();

    assertSame(THIN, font.resolveSource(FontWeight.THIN, false));
    assertSame(REGULAR, font.resolveSource(FontWeight.REGULAR, false));
    assertSame(SEMI_BOLD, font.resolveSource(FontWeight.SEMI_BOLD, false));
    assertSame(BOLD, font.resolveSource(FontWeight.BOLD, false));
  }
}
