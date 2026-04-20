package dev.ditsche.kawa.core;

import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.ditsche.kawa.elements.PageBreakElement;
import dev.ditsche.kawa.elements.SpacerElement;
import java.io.File;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

class PageSlotTest {

  @Test
  void backgroundAndOverlayRenderAroundContent() throws Exception {
    File out = new File("target/page-slots-layering.pdf");

    Document.create(
            doc ->
                doc.page(
                    page ->
                        page.size(PageSize.A4)
                            .margin(80)
                            .background(bg -> bg.text("BACKGROUND"))
                            .content(c -> c.text("CONTENT"))
                            .overlay(overlay -> overlay.text("OVERLAY"))))
        .generatePdf(out);

    try (PDDocument pdf = Loader.loadPDF(out)) {
      String text = new PDFTextStripper().getText(pdf);
      assertTrue(text.indexOf("BACKGROUND") < text.indexOf("CONTENT"));
      assertTrue(text.indexOf("CONTENT") < text.indexOf("OVERLAY"));
    }
  }

  @Test
  void backgroundAndOverlayReceivePageContext() throws Exception {
    File out = new File("target/page-slots-context.pdf");

    Document.create(
            doc ->
                doc.page(
                    page ->
                        page.size(PageSize.A4)
                            .background((bg, ctx) -> bg.text("BG " + ctx.pageOf()))
                            .content(
                                c -> {
                                  c.text("Page one");
                                  c.add(new PageBreakElement());
                                  c.text("Page two");
                                })
                            .overlay(
                                (overlay, ctx) -> {
                                  overlay.add(new SpacerElement(30));
                                  overlay.text("OV " + ctx.pageOf());
                                })))
        .generatePdf(out);

    try (PDDocument pdf = Loader.loadPDF(out)) {
      PDFTextStripper stripper = new PDFTextStripper();
      stripper.setStartPage(1);
      stripper.setEndPage(1);
      String firstPage = stripper.getText(pdf);
      assertTrue(firstPage.contains("BG 1 / 2"));
      assertTrue(firstPage.contains("OV 1 / 2"));

      stripper.setStartPage(2);
      stripper.setEndPage(2);
      String secondPage = stripper.getText(pdf);
      assertTrue(secondPage.contains("BG 2 / 2"));
      assertTrue(secondPage.contains("OV 2 / 2"));
    }
  }
}
