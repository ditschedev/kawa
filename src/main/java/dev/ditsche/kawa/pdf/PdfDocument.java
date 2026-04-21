package dev.ditsche.kawa.pdf;

import java.io.*;
import java.util.List;
import java.util.Objects;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;

/**
 * Fluent pipeline for PDF post-processing.
 *
 * <pre>{@code
 * // Select pages then merge
 * PdfDocument.load("report.pdf")
 *     .pages("1,3-5")
 *     .merge("appendix.pdf")
 *     .save("output.pdf");
 *
 * // Merge multiple documents with page selection
 * PdfDocument.load("part1.pdf")
 *     .merge("part2.pdf", "2-4")
 *     .encrypt("owner", "user", PdfPermissions.fullAccess())
 *     .saveBytes();
 * }</pre>
 *
 * @author Tobias Dittmann
 */
public final class PdfDocument implements Closeable {

  private PDDocument document;

  private PdfDocument(PDDocument document) {
    this.document = document;
  }

  // -------------------------------------------------------------------------
  // Entry points
  // -------------------------------------------------------------------------

  public static PdfDocument create() {
    return new PdfDocument(new PDDocument());
  }

  public static PdfDocument load(File file) throws IOException {
    return new PdfDocument(Loader.loadPDF(file));
  }

  public static PdfDocument load(File file, String password) throws IOException {
    return new PdfDocument(Loader.loadPDF(file, password));
  }

  public static PdfDocument load(byte[] bytes) throws IOException {
    return new PdfDocument(Loader.loadPDF(bytes));
  }

  public static PdfDocument load(byte[] bytes, String password) throws IOException {
    return new PdfDocument(Loader.loadPDF(bytes, password));
  }

  // -------------------------------------------------------------------------
  // Page scoping
  // -------------------------------------------------------------------------

  private static void copyPages(PDDocument source, PDDocument target, List<Integer> pages)
      throws IOException {
    for (int pageNumber : pages) {
      target.importPage(source.getPage(pageNumber - 1));
    }
  }

  private static void copyDocumentState(PDDocument source, PDDocument target) {
    target.setVersion(source.getVersion());
    if (source.getDocumentId() != null) {
      target.setDocumentId(source.getDocumentId());
    }
    target.setDocumentInformation(copyDocumentInformation(source.getDocumentInformation()));
  }

  // -------------------------------------------------------------------------
  // Merge (append pages from another source)
  // -------------------------------------------------------------------------

  private static PDDocumentInformation copyDocumentInformation(PDDocumentInformation source) {
    PDDocumentInformation copy = new PDDocumentInformation();
    copy.setTitle(source.getTitle());
    copy.setAuthor(source.getAuthor());
    copy.setSubject(source.getSubject());
    copy.setKeywords(source.getKeywords());
    copy.setCreator(source.getCreator());
    copy.setProducer(source.getProducer());
    copy.setCreationDate(source.getCreationDate());
    copy.setModificationDate(source.getModificationDate());
    copy.setTrapped(source.getTrapped());
    for (String key : source.getMetadataKeys()) {
      copy.setCustomMetadataValue(key, source.getCustomMetadataValue(key));
    }
    return copy;
  }

  private static PdfSaveOptions requireSupported(PdfSaveOptions options) {
    PdfSaveOptions effective = options == null ? PdfSaveOptions.standard() : options;
    if (effective.getSaveMode() == PdfSaveMode.LINEARIZED) {
      throw new UnsupportedOperationException(
          "Linearized save is not supported by the current PDFBox-based backend");
    }
    return effective;
  }

  /**
   * Restricts (and optionally reorders) the pages of this document.
   *
   * @param spec page spec, e.g. {@code "1,3-5"} or {@code "3,1"}
   */
  public PdfDocument pages(String spec) throws IOException {
    return pages(PageSelection.parse(spec));
  }

  public PdfDocument pages(PageSelection selection) throws IOException {
    Objects.requireNonNull(selection, "selection");

    PDDocument selected = new PDDocument();
    try {
      copyDocumentState(document, selected);
      copyPages(document, selected, selection.resolve(document.getNumberOfPages()));

      PDDocument previous = document;
      document = selected;
      previous.close();
      return this;
    } catch (IOException | RuntimeException e) {
      selected.close();
      throw e;
    }
  }

  /** Appends all pages of {@code file}. */
  public PdfDocument merge(File file) throws IOException {
    return merge(file, null, PageSelection.all());
  }

  /**
   * Appends selected pages of {@code file}.
   *
   * @param pageSpec page spec, e.g. {@code "2-4"}
   */
  public PdfDocument merge(File file, String pageSpec) throws IOException {
    return merge(file, null, PageSelection.parse(pageSpec));
  }

  public PdfDocument merge(File file, PageSelection selection) throws IOException {
    return merge(file, null, selection);
  }

  /**
   * Appends pages of a password-protected {@code file}.
   *
   * @param pageSpec page spec, e.g. {@code "2-4"}, or {@code null} for all pages
   */
  public PdfDocument merge(File file, String password, String pageSpec) throws IOException {
    PageSelection selection =
        pageSpec == null ? PageSelection.all() : PageSelection.parse(pageSpec);
    return merge(file, password, selection);
  }

  // -------------------------------------------------------------------------
  // Security
  // -------------------------------------------------------------------------

  public PdfDocument merge(File file, String password, PageSelection selection) throws IOException {
    Objects.requireNonNull(file, "file");
    Objects.requireNonNull(selection, "selection");

    try (PDDocument source =
        password == null ? Loader.loadPDF(file) : Loader.loadPDF(file, password)) {
      copyPages(source, document, selection.resolve(source.getNumberOfPages()));
    }
    return this;
  }

  /** Appends all pages of another {@code PdfOperation}. */
  public PdfDocument merge(PdfDocument other) throws IOException {
    Objects.requireNonNull(other, "other");
    copyPages(
        other.document, document, PageSelection.all().resolve(other.document.getNumberOfPages()));
    return this;
  }

  // -------------------------------------------------------------------------
  // Info
  // -------------------------------------------------------------------------

  /** Appends selected pages of another {@code PdfOperation}. */
  public PdfDocument merge(PdfDocument other, String pageSpec) throws IOException {
    return merge(other, PageSelection.parse(pageSpec));
  }

  public PdfDocument merge(PdfDocument other, PageSelection selection) throws IOException {
    Objects.requireNonNull(other, "other");
    Objects.requireNonNull(selection, "selection");
    copyPages(other.document, document, selection.resolve(other.document.getNumberOfPages()));
    return this;
  }

  // -------------------------------------------------------------------------
  // Save (terminal)
  // -------------------------------------------------------------------------

  public PdfDocument encrypt(String ownerPassword, String userPassword, PdfPermissions permissions)
      throws IOException {
    if (ownerPassword == null || ownerPassword.isBlank()) {
      throw new IllegalArgumentException("ownerPassword must not be blank");
    }

    PdfPermissions effective = permissions == null ? PdfPermissions.fullAccess() : permissions;
    StandardProtectionPolicy policy =
        new StandardProtectionPolicy(
            ownerPassword,
            userPassword == null ? "" : userPassword,
            effective.toAccessPermission());
    policy.setEncryptionKeyLength(256);
    policy.setPreferAES(true);
    document.protect(policy);
    return this;
  }

  public PdfDocument decrypt() {
    document.setAllSecurityToBeRemoved(true);
    return this;
  }

  public int pageCount() {
    return document.getNumberOfPages();
  }

  public boolean isEncrypted() {
    return document.isEncrypted();
  }

  public void save(File file) throws IOException {
    save(file, PdfSaveOptions.standard());
  }

  public void save(File file, PdfSaveOptions options) throws IOException {
    Objects.requireNonNull(file, "file");
    document.save(file, requireSupported(options).getCompressParameters());
  }

  public void save(OutputStream outputStream) throws IOException {
    save(outputStream, PdfSaveOptions.standard());
  }

  // -------------------------------------------------------------------------
  // Internal helpers
  // -------------------------------------------------------------------------

  public void save(OutputStream outputStream, PdfSaveOptions options) throws IOException {
    Objects.requireNonNull(outputStream, "outputStream");
    document.save(outputStream, requireSupported(options).getCompressParameters());
  }

  public byte[] saveBytes() throws IOException {
    return saveBytes(PdfSaveOptions.standard());
  }

  public byte[] saveBytes(PdfSaveOptions options) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    save(out, options);
    return out.toByteArray();
  }

  @Override
  public void close() throws IOException {
    document.close();
  }
}
