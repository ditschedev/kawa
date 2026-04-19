package dev.ditsche.kawa.elements;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.ditsche.kawa.core.Document;
import dev.ditsche.kawa.core.PageSize;
import dev.ditsche.kawa.layout.LayoutContext;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChoiceElementTest {

  private static final LayoutContext CTX = new LayoutContext(0, 0, 500, Float.MAX_VALUE);
  private static final List<String> OPTIONS = List.of("Apple", "Banana", "Cherry", "Date");

  @Test
  void measure_noLabel_returnsFieldHeightOnly() {
    assertEquals(80f, new ChoiceElement("fruit", OPTIONS).measure(CTX), 0.01f);
  }

  @Test
  void measure_withLabel_includesLabelHeightAndGap() {
    ChoiceElement el = new ChoiceElement("fruit", OPTIONS).label("Fruit").labelFontSize(10);
    float expected = 10 * 1.3f + 4f + 80f;
    assertEquals(expected, el.measure(CTX), 0.01f);
  }

  @Test
  void measure_customHeight_returnsConfiguredHeight() {
    assertEquals(120f, new ChoiceElement("items", OPTIONS).height(120).measure(CTX), 0.01f);
  }

  @Test
  void generatesPdf() throws Exception {
    File out = new File("target/element-choice.pdf");
    Document.create(
            doc ->
                doc.page(
                    page ->
                        page.size(PageSize.A4)
                            .margin(50)
                            .content(
                                c -> {
                                  c.add(
                                      new ChoiceElement(
                                              "fruit",
                                              List.of("Apple", "Banana", "Cherry", "Date"))
                                          .label("Select a fruit")
                                          .selected("Banana")
                                          .height(100));
                                  c.add(new SpacerElement(12));
                                  c.add(
                                      new ChoiceElement(
                                              "country", List.of("Germany", "France", "Spain"))
                                          .height(70));
                                })))
        .generatePdf(out);
    assertTrue(out.exists() && out.length() > 0);
  }
}
