package io.kawa.font;

import java.io.IOException;
import java.io.InputStream;

/** Loads a font from the classpath (e.g. {@code "/fonts/Inter-Regular.ttf"}). */
record ResourceFontSource(String path) implements FontSource {
  @Override
  public byte[] load() throws IOException {
    try (InputStream is = ResourceFontSource.class.getResourceAsStream(path)) {
      if (is == null) throw new IOException("Font resource not found on classpath: " + path);
      return is.readAllBytes();
    }
  }
}
