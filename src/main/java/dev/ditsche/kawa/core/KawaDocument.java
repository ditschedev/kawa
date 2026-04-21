package dev.ditsche.kawa.core;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Contract for reusable document classes.
 *
 * <p>Implementations define layout in {@link #compose(PageDefinition)} and may customize document
 * settings and post-generation behavior.
 *
 * @author Tobias Dittmann
 */
public interface KawaDocument {

  /**
   * Defines the page structure for the document.
   *
   * @param page the page definition to configure
   */
  void compose(PageDefinition page);

  /**
   * Applies document-level configuration before composition.
   *
   * <p>The default implementation does nothing.
   *
   * @param settings mutable document settings
   */
  default void configure(DocumentSettings settings) {
    // no-op by default
  }

  /**
   * Called after the PDF has been written successfully.
   *
   * <p>The default implementation does nothing.
   *
   * @param pageCount number of physical pages generated
   */
  default void onAfterGeneration(int pageCount) {
    // no-op by default
  }

  // -------------------------------------------------------------------------
  // Generation — default implementations, no need to override
  // -------------------------------------------------------------------------

  /** Generates the PDF and writes it to the given file path. */
  default void generatePdf(String filePath) throws IOException {
    generatePdf(new File(filePath));
  }

  /** Generates the PDF and writes it to the given file. */
  default void generatePdf(File file) throws IOException {
    KawaRenderer renderer = buildRenderer();
    renderer.renderToFile(file);
  }

  /** Generates the PDF and writes it to the given output stream. */
  default void generatePdf(OutputStream outputStream) throws IOException {
    KawaRenderer renderer = buildRenderer();
    renderer.renderToStream(outputStream);
  }

  /** Generates the PDF and returns the raw bytes. */
  default byte[] generatePdfBytes() throws IOException {
    KawaRenderer renderer = buildRenderer();
    return renderer.renderToBytes();
  }

  // -------------------------------------------------------------------------
  // Internal
  // -------------------------------------------------------------------------

  private KawaRenderer buildRenderer() {
    DocumentSettings settings = new DocumentSettings();
    configure(settings);

    PageDefinition page = new PageDefinition();
    compose(page);

    return new KawaRenderer(page, settings, this::onAfterGeneration);
  }
}
