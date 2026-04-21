package dev.ditsche.kawa.font;

import java.io.IOException;

/**
 * Internal abstraction over a font byte source. Implementations are package-private; users interact
 * only via {@link KawaFont}.
 *
 * @author Tobias Dittmann
 */
sealed interface FontSource permits FileFontSource, ResourceFontSource {
  /** Loads and returns the raw font bytes (TTF/OTF). */
  byte[] load() throws IOException;
}
