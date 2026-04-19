package dev.ditsche.kawa.elements;

import dev.ditsche.kawa.layout.LayoutContext;
import dev.ditsche.kawa.renderer.RenderContext;
import dev.ditsche.kawa.units.Unit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;

/** Embeds an image into the document. Supported source formats: PNG, JPEG, SVG, PDF. */
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
        default -> {
          drawW = context.width();
          drawH = imgH / imgW * drawW;
        }
      }

      float pageH = renderCtx.getPage().getMediaBox().getHeight();
      float pdfY = pageH - context.y() - drawH;
      renderCtx.getContentStream().drawImage(image, context.x(), pdfY, drawW, drawH);
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
    BufferedImageTranscoder transcoder = new BufferedImageTranscoder();
    try {
      transcoder.transcode(new TranscoderInput(new ByteArrayInputStream(svgBytes)), null);
    } catch (TranscoderException e) {
      throw new IOException("Failed to rasterize SVG", e);
    }
    BufferedImage image = transcoder.getImage();
    if (image == null) throw new IOException("SVG transcoding produced no image");
    return image;
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
    FIXED,
    FIXED_WIDTH
  }

  private static final class BufferedImageTranscoder extends PNGTranscoder {
    private BufferedImage image;

    @Override
    public BufferedImage createImage(int width, int height) {
      image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
      return image;
    }

    @Override
    public void writeImage(
        BufferedImage image, org.apache.batik.transcoder.TranscoderOutput output) {
      this.image = image;
    }

    private BufferedImage getImage() {
      return image;
    }
  }
}
