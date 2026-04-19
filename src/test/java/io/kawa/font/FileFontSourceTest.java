package io.kawa.font;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileFontSourceTest {

  @TempDir Path tempDir;

  @Test
  void loadReturnsBytesFromFile() throws IOException {
    byte[] data = {0x00, 0x01, 0x00, 0x00, 0x41, 0x42};
    Path file = tempDir.resolve("font.ttf");
    Files.write(file, data);

    byte[] loaded = new FileFontSource(file.toString()).load();

    assertArrayEquals(data, loaded);
  }

  @Test
  void loadThrowsForMissingFile() {
    FileFontSource source = new FileFontSource(tempDir.resolve("missing.ttf").toString());
    assertThrows(IOException.class, source::load);
  }

  @Test
  void equalityBasedOnPath() {
    FileFontSource a = new FileFontSource("/fonts/regular.ttf");
    FileFontSource b = new FileFontSource("/fonts/regular.ttf");
    FileFontSource c = new FileFontSource("/fonts/bold.ttf");

    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
    assertNotEquals(a, c);
  }

  @Test
  void pathAccessorReturnsConstructorValue() {
    FileFontSource source = new FileFontSource("/some/path.ttf");
    assertEquals("/some/path.ttf", source.path());
  }
}
