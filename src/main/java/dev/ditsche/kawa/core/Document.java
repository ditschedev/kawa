package dev.ditsche.kawa.core;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;

/**
 * Fluent entry point for building documents inline.
 *
 * <p>This type is intended for direct document composition without a dedicated report class.
 *
 * @author Tobias Dittmann
 */
public final class Document {

  private final DocumentSettings settings = new DocumentSettings();
  private PageDefinition pageDef;

  private Document() {}

  // -------------------------------------------------------------------------
  // Factory
  // -------------------------------------------------------------------------

  /**
   * Creates a document using the provided builder.
   *
   * @param builder the document builder
   * @return the created document
   */
  public static Document create(Consumer<Document> builder) {
    Document doc = new Document();
    builder.accept(doc);
    return doc;
  }

  // -------------------------------------------------------------------------
  // Configuration
  // -------------------------------------------------------------------------

  public Document title(String title) {
    settings.title(title);
    return this;
  }

  public Document author(String author) {
    settings.author(author);
    return this;
  }

  public Document subject(String subject) {
    settings.subject(subject);
    return this;
  }

  // -------------------------------------------------------------------------
  // Page definition
  // -------------------------------------------------------------------------

  /**
   * Configures the page definition for this document.
   *
   * @param builder the page definition builder
   * @return this document
   */
  public Document page(Consumer<PageDefinition> builder) {
    this.pageDef = new PageDefinition();
    builder.accept(this.pageDef);
    return this;
  }

  // -------------------------------------------------------------------------
  // Output
  // -------------------------------------------------------------------------

  public void generatePdf(String filePath) throws IOException {
    generatePdf(new File(filePath));
  }

  public void generatePdf(File file) throws IOException {
    buildRenderer().renderToFile(file);
  }

  public void generatePdf(OutputStream outputStream) throws IOException {
    buildRenderer().renderToStream(outputStream);
  }

  public byte[] generatePdfBytes() throws IOException {
    return buildRenderer().renderToBytes();
  }

  // -------------------------------------------------------------------------
  // Internal
  // -------------------------------------------------------------------------

  private KawaRenderer buildRenderer() {
    if (pageDef == null) {
      throw new IllegalStateException(
          "No page definition found. Call doc.page(...) before generating.");
    }
    return new KawaRenderer(pageDef, settings, pageCount -> {});
  }
}
