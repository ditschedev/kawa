package io.kawa;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import org.junit.jupiter.api.Test;

/**
 * Rendering smoke test for the full {@link QuoteReport} example document. Exercises document
 * composition, multi-page layout, headers/footers, and a wide range of elements in combination.
 */
class QuoteReportTest {

  @Test
  void generatesSampleQuote() throws Exception {
    File out = new File("target/quote-sample.pdf");
    QuoteReport.sample().generatePdf(out);
    assertTrue(out.exists());
  }
}
