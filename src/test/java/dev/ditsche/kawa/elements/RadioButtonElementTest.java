package dev.ditsche.kawa.elements;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.ditsche.kawa.core.Document;
import dev.ditsche.kawa.core.PageSize;
import dev.ditsche.kawa.layout.LayoutContext;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Test;

class RadioButtonElementTest {

  private static final LayoutContext CTX = new LayoutContext(0, 0, 500, Float.MAX_VALUE);
  private static final List<String> OPTIONS = List.of("Option A", "Option B", "Option C");

  @Test
  void measure_vertical_threeOptions() {
    // size=12, labelFontSize=11 → rowH=max(12, 11*1.3)=14.3; total = 3*14.3 + 2*6 = 54.9
    RadioButtonElement el =
        new RadioButtonElement("group", OPTIONS).size(12).labelFontSize(11).vertical();
    float rowH = Math.max(12f, 11f * 1.3f);
    float expected = 3 * rowH + 2 * 6f;
    assertEquals(expected, el.measure(CTX), 0.01f);
  }

  @Test
  void measure_horizontal_returnsOneRowHeight() {
    RadioButtonElement el =
        new RadioButtonElement("group", OPTIONS).size(12).labelFontSize(11).horizontal();
    float expected = Math.max(12f, 11f * 1.3f);
    assertEquals(expected, el.measure(CTX), 0.01f);
  }

  @Test
  void measure_emptyOptions_returnsZero() {
    assertEquals(0f, new RadioButtonElement("group", List.of()).measure(CTX), 0.01f);
  }

  @Test
  void generatesPdf() throws Exception {
    File out = new File("target/element-radiobutton.pdf");
    Document.create(
            doc ->
                doc.page(
                    page ->
                        page.size(PageSize.A4)
                            .margin(50)
                            .content(
                                c -> {
                                  c.add(
                                      new RadioButtonElement(
                                              "size", List.of("Small", "Medium", "Large"))
                                          .selected("Medium"));
                                  c.add(new SpacerElement(12));
                                  c.add(
                                      new RadioButtonElement(
                                              "color", List.of("Red", "Green", "Blue"))
                                          .horizontal()
                                          .selected("Blue"));
                                })))
        .generatePdf(out);
    assertTrue(out.exists() && out.length() > 0);
  }
}
