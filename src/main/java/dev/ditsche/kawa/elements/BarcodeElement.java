package dev.ditsche.kawa.elements;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import dev.ditsche.kawa.layout.LayoutContext;
import dev.ditsche.kawa.renderer.RenderContext;
import dev.ditsche.kawa.units.Unit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import javax.imageio.ImageIO;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/** Renders a one-dimensional barcode from a string value using ZXing. */
public final class BarcodeElement implements ContentElement {

  private final String content;
  private BarcodeFormat format = BarcodeFormat.CODE_128;
  private float width = 160f; // points
  private float height = 50f; // points

  public BarcodeElement(String content) {
    this.content = content == null ? "" : content;
  }

  public BarcodeElement format(BarcodeFormat f) {
    this.format = f;
    return this;
  }

  public BarcodeElement width(float w) {
    this.width = Math.max(10f, w);
    return this;
  }

  public BarcodeElement width(float w, Unit unit) {
    return width(unit.toPoints(w));
  }

  public BarcodeElement height(float h) {
    this.height = Math.max(10f, h);
    return this;
  }

  public BarcodeElement height(float h, Unit unit) {
    return height(unit.toPoints(h));
  }

  @Override
  public float measure(LayoutContext context) {
    return height;
  }

  @Override
  public void render(LayoutContext context, RenderContext renderCtx) {
    try {
      int pixW = (int) width * 3;
      int pixH = (int) height * 3;

      MultiFormatWriter writer = new MultiFormatWriter();
      Map<EncodeHintType, Object> hints = Map.of(EncodeHintType.MARGIN, 0);
      BitMatrix matrix = writer.encode(content, format, pixW, pixH, hints);
      BufferedImage img = MatrixToImageWriter.toBufferedImage(matrix);

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(img, "PNG", baos);

      PDImageXObject image =
          PDImageXObject.createFromByteArray(renderCtx.getDocument(), baos.toByteArray(), "png");

      float pageH = renderCtx.getPage().getMediaBox().getHeight();
      float pdfY = pageH - context.y() - height;
      renderCtx.getContentStream().drawImage(image, context.x(), pdfY, width, height);
    } catch (IOException | WriterException e) {
      throw new KawaRenderException("Failed to render barcode for: " + content, e);
    }
  }
}
