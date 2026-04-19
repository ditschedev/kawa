package dev.ditsche.kawa;

import static org.junit.jupiter.api.Assertions.*;

import dev.ditsche.kawa.pdf.PdfDocument;
import dev.ditsche.kawa.pdf.PdfPermissions;
import dev.ditsche.kawa.pdf.PdfSaveMode;
import dev.ditsche.kawa.pdf.PdfSaveOptions;
import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

class PdfDocumentTest {

  private static File samplePdf(String path, String... pageTexts) throws IOException {
    File out = new File(path);
    try (PDDocument pdf = new PDDocument()) {
      for (String pageText : pageTexts) {
        PDPage page = new PDPage(PDRectangle.A4);
        pdf.addPage(page);

        try (PDPageContentStream content = new PDPageContentStream(pdf, page)) {
          content.beginText();
          content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 16);
          content.newLineAtOffset(72, 720);
          content.showText(pageText);
          content.endText();
        }
      }
      pdf.save(out);
    }
    return out;
  }

  private static String pageText(PDDocument pdf, int pageNumber) throws IOException {
    PDFTextStripper stripper = new PDFTextStripper();
    stripper.setStartPage(pageNumber);
    stripper.setEndPage(pageNumber);
    return stripper.getText(pdf).trim();
  }

  @Test
  void mergeAndPartialMerge_includeOnlySelectedPages() throws Exception {
    File first = samplePdf("target/pdf-first.pdf", "FIRST-1", "FIRST-2");
    File second = samplePdf("target/pdf-second.pdf", "SECOND-1", "SECOND-2", "SECOND-3");

    byte[] mergedBytes;
    try (PdfDocument op = PdfDocument.create()) {
      mergedBytes = op.merge(first).merge(second, "2-3").saveBytes();
    }

    try (PDDocument pdf = Loader.loadPDF(mergedBytes)) {
      assertEquals(4, pdf.getNumberOfPages());
      String text = new PDFTextStripper().getText(pdf);
      assertTrue(text.contains("FIRST-1"));
      assertTrue(text.contains("FIRST-2"));
      assertTrue(text.contains("SECOND-2"));
      assertTrue(text.contains("SECOND-3"));
      assertFalse(text.contains("SECOND-1"));
    }
  }

  @Test
  void pages_canReorderPages() throws Exception {
    File source = samplePdf("target/pdf-select-source.pdf", "PAGE-1", "PAGE-2", "PAGE-3");

    byte[] selectedBytes;
    try (PdfDocument op = PdfDocument.load(source)) {
      selectedBytes = op.pages("3,1").saveBytes();
    }

    try (PDDocument selected = Loader.loadPDF(selectedBytes)) {
      assertEquals(2, selected.getNumberOfPages());
      assertEquals("PAGE-3", pageText(selected, 1));
      assertEquals("PAGE-1", pageText(selected, 2));
    }
  }

  @Test
  void encryptAndDecrypt_roundTripWorks() throws Exception {
    File source = samplePdf("target/pdf-encrypt-source.pdf", "SECRET");

    byte[] encryptedBytes;
    try (PdfDocument op = PdfDocument.load(source)) {
      encryptedBytes =
          op.encrypt(
                  "owner-secret",
                  "user-secret",
                  PdfPermissions.builder()
                      .allowPrint(false)
                      .allowModify(false)
                      .allowExtractContent(false)
                      .build())
              .saveBytes();
    }

    assertThrows(IOException.class, () -> Loader.loadPDF(encryptedBytes));

    byte[] decryptedBytes;
    try (PdfDocument encrypted = PdfDocument.load(encryptedBytes, "user-secret")) {
      assertTrue(encrypted.isEncrypted());
      decryptedBytes = encrypted.decrypt().saveBytes();
    }

    try (PDDocument decrypted = Loader.loadPDF(decryptedBytes)) {
      assertFalse(decrypted.isEncrypted());
      assertEquals(1, decrypted.getNumberOfPages());
      assertTrue(new PDFTextStripper().getText(decrypted).contains("SECRET"));
    }
  }

  @Test
  void linearizedSaveMode_isExplicitlyUnsupported() throws Exception {
    File source = samplePdf("target/pdf-linearized-source.pdf", "LINEARIZE");

    try (PdfDocument op = PdfDocument.load(source)) {
      assertThrows(
          UnsupportedOperationException.class,
          () -> op.saveBytes(PdfSaveOptions.builder().saveMode(PdfSaveMode.LINEARIZED).build()));
    }
  }
}
