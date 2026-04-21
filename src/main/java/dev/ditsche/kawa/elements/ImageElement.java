package dev.ditsche.kawa.elements;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.view.FloatSize;
import com.github.weisj.jsvg.parser.LoaderContext;
import com.github.weisj.jsvg.parser.SVGLoader;
import dev.ditsche.kawa.layout.LayoutContext;
import dev.ditsche.kawa.renderer.RenderContext;
import dev.ditsche.kawa.units.Unit;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * Embeds an image into the document. Supported source formats: PNG, JPEG, SVG, PDF.
 *
 * @author Tobias Dittmann
 */
public final class ImageElement implements ContentElement {

  private static final float PDF_RENDER_DPI = 144f;
  private final Source source;
  private final String path;
  private final byte[] bytes;
  private final String formatHint;
  private SizeMode sizeMode = SizeMode.FIT;
  private float fixedW = 0f;
  private float fixedH = 0f;

  private ImageElement(Source source, String path, byte[] bytes, String format) {
    this.source = source;
    this.path = path;
    this.bytes = bytes;
    this.formatHint = format;
  }

  /** Loads image from an absolute or relative file path. */
  public static ImageElement ofFile(String filePath) {
    return new ImageElement(Source.FILE, filePath, null, guessFormat(filePath));
  }

  /** Loads image from a classpath resource (e.g. {@code "/images/logo.png"}). */
  public static ImageElement ofResource(String resourcePath) {
    return new ImageElement(Source.RESOURCE, resourcePath, null, guessFormat(resourcePath));
  }

  /**
   * Loads an image from raw bytes.
   *
   * @param data source bytes
   * @param format format hint: {@code png}, {@code jpg}, {@code svg}, or {@code pdf}
   */
  public static ImageElement ofBytes(byte[] data, String format) {
    return new ImageElement(Source.BYTES, null, data, guessFormat(format));
  }

  private static String guessFormat(String pathOrFormat) {
    if (pathOrFormat == null || pathOrFormat.isBlank()) return "png";
    String lower = pathOrFormat.toLowerCase();
    int dot = lower.lastIndexOf('.');
    String ext = dot >= 0 ? lower.substring(dot + 1) : lower;
    return switch (ext) {
      case "jpeg" -> "jpg";
      case "jpg", "png", "svg", "pdf" -> ext;
      default -> "png";
    };
  }

  /** Scales proportionally to fill the available content width (default). */
  public ImageElement fit() {
    sizeMode = SizeMode.FIT;
    return this;
  }

  /** Scales proportionally to cover the whole available box, centered and clipped if needed. */
  public ImageElement fill() {
    sizeMode = SizeMode.FILL;
    return this;
  }

  /** Exact fixed size in points. */
  public ImageElement fixed(float width, float height) {
    sizeMode = SizeMode.FIXED;
    fixedW = width;
    fixedH = height;
    return this;
  }

  /** Exact fixed size in the specified unit. */
  public ImageElement fixed(float width, float height, Unit unit) {
    return fixed(unit.toPoints(width), unit.toPoints(height));
  }

  /** Fixed width in points; height calculated from image aspect ratio. */
  public ImageElement width(float width) {
    sizeMode = SizeMode.FIXED_WIDTH;
    fixedW = width;
    return this;
  }

  /** Fixed width in the specified unit; height calculated from image aspect ratio. */
  public ImageElement width(float width, Unit unit) {
    return width(unit.toPoints(width));
  }

  @Override
  public float measure(LayoutContext context) {
    return switch (sizeMode) {
      case FIXED -> fixedH;
      case FIXED_WIDTH -> fixedW;
      case FILL -> Float.isFinite(context.height()) ? context.height() : context.width() * 0.5f;
      case FIT -> context.width() * 0.5f;
    };
  }

  @Override
  public void render(LayoutContext context, RenderContext renderCtx) {
    try {
      PDImageXObject image = loadImage(renderCtx);
      float imgW = image.getWidth();
      float imgH = image.getHeight();

      float drawW, drawH;
      switch (sizeMode) {
        case FIXED -> {
          drawW = fixedW;
          drawH = fixedH;
        }
        case FIXED_WIDTH -> {
          drawW = fixedW;
          drawH = imgH / imgW * drawW;
        }
        case FILL -> {
          float scale = Math.max(context.width() / imgW, context.height() / imgH);
          drawW = imgW * scale;
          drawH = imgH * scale;
        }
        default -> {
          drawW = context.width();
          drawH = imgH / imgW * drawW;
        }
      }

      float pageH = renderCtx.getPage().getMediaBox().getHeight();
      float drawX = context.x();
      float drawY = pageH - context.y() - drawH;
      if (sizeMode == SizeMode.FILL) {
        drawX = context.x() + (context.width() - drawW) / 2f;
        float y = pageH - context.y() - context.height();

        drawY = y + (context.height() - drawH) / 2f;
        renderCtx.getContentStream().saveGraphicsState();
        renderCtx
            .getContentStream()
            .addRect(
                context.x(), y, context.width(), context.height());
        renderCtx.getContentStream().clip();
        renderCtx.getContentStream().drawImage(image, drawX, drawY, drawW, drawH);
        renderCtx.getContentStream().restoreGraphicsState();
      } else {
        renderCtx.getContentStream().drawImage(image, drawX, drawY, drawW, drawH);
      }
    } catch (IOException e) {
      throw new KawaRenderException("Failed to render image: " + imageSourceLabel(), e);
    }
  }

  private PDImageXObject loadImage(RenderContext ctx) throws IOException {
    byte[] sourceBytes = readSourceBytes();
    return switch (formatHint) {
      case "pdf" ->
          LosslessFactory.createFromImage(ctx.getDocument(), renderPdfFirstPage(sourceBytes));
      case "svg" -> LosslessFactory.createFromImage(ctx.getDocument(), renderSvg(sourceBytes));
      case "jpg", "jpeg", "png" ->
          PDImageXObject.createFromByteArray(ctx.getDocument(), sourceBytes, imageSourceLabel());
      default -> throw new IOException("Unsupported image format: " + formatHint);
    };
  }

  private byte[] readSourceBytes() throws IOException {
    return switch (source) {
      case FILE -> Files.readAllBytes(new File(path).toPath());
      case RESOURCE -> {
        try (InputStream is = getClass().getResourceAsStream(path)) {
          if (is == null) throw new IOException("Resource not found: " + path);
          yield is.readAllBytes();
        }
      }
      case BYTES -> {
        if (bytes == null) throw new IOException("Image bytes are missing");
        yield bytes;
      }
    };
  }

  private BufferedImage renderPdfFirstPage(byte[] pdfBytes) throws IOException {
    try (PDDocument pdf = Loader.loadPDF(pdfBytes)) {
      if (pdf.getNumberOfPages() == 0) throw new IOException("PDF has no pages");
      return new PDFRenderer(pdf).renderImageWithDPI(0, PDF_RENDER_DPI);
    }
  }

  private BufferedImage renderSvg(byte[] svgBytes) throws IOException {
    SVGDocument doc = new SVGLoader()
        .load(new ByteArrayInputStream(svgBytes), null, LoaderContext.createDefault());
    if (doc == null) throw new IOException("Failed to parse SVG");

    FloatSize size = doc.size();
    // Render at 2× the natural size for crisp output when scaled down into the PDF
    int w = Math.max(1, (int) Math.ceil(size.width * 2));
    int h = Math.max(1, (int) Math.ceil(size.height * 2));

    BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = img.createGraphics();
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    g2d.scale(2.0, 2.0);
    doc.render(null, g2d);
    g2d.dispose();
    return img;
  }

  private String imageSourceLabel() {
    return path != null ? path : "embedded-image." + formatHint;
  }

  private enum Source {
    FILE,
    RESOURCE,
    BYTES
  }

  private enum SizeMode {
    FIT,
    FILL,
    FIXED,
    FIXED_WIDTH
  }

}
