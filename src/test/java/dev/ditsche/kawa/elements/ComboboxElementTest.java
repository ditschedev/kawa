package dev.ditsche.kawa.elements;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.ditsche.kawa.core.Document;
import dev.ditsche.kawa.core.PageSize;
import dev.ditsche.kawa.layout.LayoutContext;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Test;

class ComboboxElementTest {

  private static final LayoutContext CTX = new LayoutContext(0, 0, 500, Float.MAX_VALUE);
  private static final List<String> OPTIONS = List.of("Draft", "Review", "Approved", "Rejected");

  @Test
  void measure_noLabel_returnsFieldHeightOnly() {
    assertEquals(22f, new ComboboxElement("status", OPTIONS).measure(CTX), 0.01f);
  }

  @Test
  void measure_withLabel_includesLabelHeightAndGap() {
    ComboboxElement el = new ComboboxElement("status", OPTIONS).label("Status").labelFontSize(10);
    float expected = 10 * 1.3f + 4f + 22f;
    assertEquals(expected, el.measure(CTX), 0.01f);
  }

  @Test
  void measure_customHeight_returnsConfiguredHeight() {
    assertEquals(30f, new ComboboxElement("status", OPTIONS).height(30).measure(CTX), 0.01f);
  }

  @Test
  void generatesPdf() throws Exception {
    File out = new File("target/element-combobox.pdf");
    Document.create(
            doc ->
                doc.page(
                    page ->
                        page.size(PageSize.A4)
                            .margin(50)
                            .content(
                                c -> {
                                  c.add(
                                      new ComboboxElement(
                                              "status",
                                              List.of("Draft", "Review", "Approved", "Rejected"))
                                          .label("Document Status")
                                          .selected("Review"));
                                  c.add(new SpacerElement(10));
                                  c.add(
                                      new ComboboxElement(
                                              "lang",
                                              List.of("English", "German", "French", "Spanish"))
                                          .label("Language"));
                                })))
        .generatePdf(out);
    assertTrue(out.exists() && out.length() > 0);
  }
}
