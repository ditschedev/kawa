package io.kawa.font;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Loads a font from an absolute or relative file-system path. */
record FileFontSource(String path) implements FontSource {
  @Override
  public byte[] load() throws IOException {
    return Files.readAllBytes(Path.of(path));
  }
}
