package dev.ditsche.kawa.pdf;

import org.apache.pdfbox.pdfwriter.compress.CompressParameters;

/**
 * Save options for PDF post-processing output.
 *
 * @author Tobias Dittmann
 */
public final class PdfSaveOptions {

  private final PdfSaveMode saveMode;
  private final CompressParameters compressParameters;

  private PdfSaveOptions(PdfSaveMode saveMode, CompressParameters compressParameters) {
    this.saveMode = saveMode;
    this.compressParameters = compressParameters;
  }

  public static PdfSaveOptions standard() {
    return builder().build();
  }

  public static Builder builder() {
    return new Builder();
  }

  public PdfSaveMode getSaveMode() {
    return saveMode;
  }

  public CompressParameters getCompressParameters() {
    return compressParameters;
  }

  public static final class Builder {
    private PdfSaveMode saveMode = PdfSaveMode.STANDARD;
    private CompressParameters compressParameters = CompressParameters.DEFAULT_COMPRESSION;

    public Builder saveMode(PdfSaveMode saveMode) {
      this.saveMode = saveMode;
      return this;
    }

    public Builder compress(boolean compress) {
      this.compressParameters =
          compress ? CompressParameters.DEFAULT_COMPRESSION : CompressParameters.NO_COMPRESSION;
      return this;
    }

    public Builder objectStreamSize(int objectStreamSize) {
      this.compressParameters = new CompressParameters(objectStreamSize);
      return this;
    }

    public PdfSaveOptions build() {
      return new PdfSaveOptions(saveMode, compressParameters);
    }
  }
}
