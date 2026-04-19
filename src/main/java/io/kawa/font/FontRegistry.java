package io.kawa.font;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

/** Caches embedded fonts per PDF document. */
public final class FontRegistry {

  private final PDDocument document;
  private final Map<FontSource, PDFont> cache = new HashMap<>();

  public FontRegistry(PDDocument document) {
    this.document = document;
  }

  /**
   * Resolves the PDF font for the given font family, weight, and italic axis. The nearest
   * registered weight is selected automatically.
   *
   * @throws IOException if the font bytes cannot be loaded or embedded
   */
  public PDFont resolve(KawaFont kawaFont, int weight, boolean italic) throws IOException {
    FontSource source = kawaFont.resolveSource(weight, italic);

    PDFont cached = cache.get(source);
    if (cached != null) return cached;

    byte[] bytes = source.load();
    PDFont font = PDType0Font.load(document, new ByteArrayInputStream(bytes), true);
    cache.put(source, font);
    return font;
  }
}
