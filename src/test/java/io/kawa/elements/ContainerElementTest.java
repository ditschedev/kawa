package io.kawa.elements;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.kawa.core.Document;
import io.kawa.core.PageSize;
import io.kawa.layout.LayoutContext;
import io.kawa.style.Colors;
import io.kawa.style.StyleSheet;
import java.io.File;
import org.junit.jupiter.api.Test;

class ContainerElementTest {

  @Test
  void measure_includesVerticalPadding() {
    StyleSheet style = StyleSheet.of(s -> s.padding(10f));
    TextElement child = new TextElement("Hello").fontSize(11f);
    ContainerElement container = ContainerElement.of(style).containing(child);

    LayoutContext ctx = new LayoutContext(0, 0, 200, 1000);
    float childH = child.measure(new LayoutContext(10, 10, 180, 1000));
    assertEquals(childH + 20f, container.measure(ctx), 0.01f);
  }

  @Test
  void measure_noChild_returnsPaddingOnly() {
    ContainerElement container = ContainerElement.of(StyleSheet.of(s -> s.paddingV(6f)));
    assertEquals(12f, container.measure(new LayoutContext(0, 0, 200, 1000)), 0.01f);
  }

  @Test
  void measure_reducesInnerWidthForChild() {
    StyleSheet style = StyleSheet.of(s -> s.paddingH(20f)); // 40pt total horizontal

    TextElement child = new TextElement("word ".repeat(30).trim()).fontSize(11f);
    LayoutContext outerCtx = new LayoutContext(0, 0, 200, 1000);
    LayoutContext innerCtx = new LayoutContext(0, 0, 160, 1000);

    float expectedChildH = child.measure(innerCtx);
    float containerH = ContainerElement.of(style).containing(child).measure(outerCtx);

    assertEquals(expectedChildH, containerH, 0.01f);
  }

  @Test
  void generatesPdf() throws Exception {
    File out = new File("target/element-container.pdf");
    Document.create(
            doc ->
                doc.page(
                    page ->
                        page.size(PageSize.A4)
                            .margin(50)
                            .content(
                                c ->
                                    c.add(
                                        ContainerElement.of(
                                                s ->
                                                    s.background(Colors.BLUE_50)
                                                        .padding(12f)
                                                        .border(1f, Colors.BLUE_200))
                                            .containing(
                                                new TextElement("Styled container")
                                                    .bold()
                                                    .fontSize(12f)
                                                    .color(Colors.BLUE_900))))))
        .generatePdf(out);
    assertTrue(out.exists() && out.length() > 0);
  }
}
