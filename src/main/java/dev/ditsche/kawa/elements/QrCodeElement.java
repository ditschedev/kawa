package dev.ditsche.kawa.elements;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import dev.ditsche.kawa.layout.LayoutContext;
import dev.ditsche.kawa.renderer.RenderContext;
import dev.ditsche.kawa.units.Unit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import javax.imageio.ImageIO;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/** Renders a QR code from text content using ZXing. */
public final class QrCodeElement implements ContentElement {

  private final String content;
  private float size = 80f; // width & height in points (square)

  public QrCodeElement(String content) {
    this.content = content == null ? "" : content;
  }

  /** Sets the width and height of the QR code in points. */
  public QrCodeElement size(float size) {
    this.size = Math.max(10f, size);
    return this;
  }

  /** Sets the width and height of the QR code in the specified unit. */
  public QrCodeElement size(float size, Unit unit) {
    return size(unit.toPoints(size));
  }

  @Override
  public float measure(LayoutContext context) {
    return size;
  }

  @Override
  public void render(LayoutContext context, RenderContext renderCtx) {
    try {
      byte[] pngBytes = generateQrPng((int) size * 3);
      PDImageXObject image =
          PDImageXObject.createFromByteArray(renderCtx.getDocument(), pngBytes, "png");

      float pageH = renderCtx.getPage().getMediaBox().getHeight();
      float pdfY = pageH - context.y() - size;
      renderCtx.getContentStream().drawImage(image, context.x(), pdfY, size, size);
    } catch (IOException | WriterException e) {
      throw new KawaRenderException("Failed to render QR code for: " + content, e);
    }
  }

  private byte[] generateQrPng(int pixels) throws WriterException, IOException {
    Map<EncodeHintType, Object> hints = Map.of(EncodeHintType.MARGIN, 1);
    QRCodeWriter writer = new QRCodeWriter();
    BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, pixels, pixels, hints);
    BufferedImage img = MatrixToImageWriter.toBufferedImage(matrix);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(img, "PNG", baos);
    return baos.toByteArray();
  }
}
