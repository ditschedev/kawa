package dev.ditsche.kawa.core;

import dev.ditsche.kawa.font.FontRegistry;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.IntConsumer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;

/** Internal renderer that builds a PDF document and writes it to an output target. */
class KawaRenderer {

  private final PageDefinition pageDef;
  private final DocumentSettings settings;
  private final IntConsumer afterGenerationHook;

  KawaRenderer(PageDefinition pageDef, DocumentSettings settings, IntConsumer afterGenerationHook) {
    this.pageDef = pageDef;
    this.settings = settings;
    this.afterGenerationHook = afterGenerationHook;

    // Disable PDFBox logging to avoid spamming the console with warnings about missing fonts, etc.
    java.util.logging.Logger
      .getLogger("org.apache.pdfbox").setLevel(java.util.logging.Level.OFF);
  }

  void renderToFile(File file) throws IOException {
    try (PDDocument pdDoc = buildDocument()) {
      pdDoc.save(file);
    }
  }

  void renderToStream(OutputStream out) throws IOException {
    try (PDDocument pdDoc = buildDocument()) {
      pdDoc.save(out);
    }
  }

  byte[] renderToBytes() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    renderToStream(baos);
    return baos.toByteArray();
  }

  // -------------------------------------------------------------------------
  // Internal
  // -------------------------------------------------------------------------

  private PDDocument buildDocument() throws IOException {
    PDDocument pdDoc = new PDDocument();

    // Apply metadata
    PDDocumentInformation info = pdDoc.getDocumentInformation();
    info.setTitle(settings.getTitle());
    info.setAuthor(settings.getAuthor());
    info.setSubject(settings.getSubject());
    info.setCreator(settings.getCreator());

    // Paginate
    FontRegistry fontRegistry = new FontRegistry(pdDoc);
    Paginator paginator = new Paginator(pageDef, fontRegistry);
    int pageCount = paginator.paginate(pdDoc);

    // Post-hook
    afterGenerationHook.accept(pageCount);

    return pdDoc;
  }
}
