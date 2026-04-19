package io.kawa.font;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class ResourceFontSourceTest {

  @Test
  void loadReturnsBytesFromClasspath() throws IOException {
    byte[] loaded = new ResourceFontSource("/test-font.bin").load();

    assertNotNull(loaded);
    assertTrue(loaded.length > 0);
  }

  @Test
  void loadThrowsForMissingResource() {
    ResourceFontSource source = new ResourceFontSource("/nonexistent-font.ttf");

    IOException ex = assertThrows(IOException.class, source::load);
    assertTrue(ex.getMessage().contains("not found on classpath"));
  }

  @Test
  void equalityBasedOnPath() {
    ResourceFontSource a = new ResourceFontSource("/fonts/regular.ttf");
    ResourceFontSource b = new ResourceFontSource("/fonts/regular.ttf");
    ResourceFontSource c = new ResourceFontSource("/fonts/bold.ttf");

    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
    assertNotEquals(a, c);
  }

  @Test
  void pathAccessorReturnsConstructorValue() {
    ResourceFontSource source = new ResourceFontSource("/fonts/test.ttf");
    assertEquals("/fonts/test.ttf", source.path());
  }
}
