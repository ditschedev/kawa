package dev.ditsche.kawa.font;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Downloads and caches font files from the Google Fonts repository.
 *
 * @author Tobias Dittmann
 */
final class GoogleFontsDownloader {

  private static final Path CACHE_DIR = Path.of(System.getProperty("user.home"), ".kawa", "fonts");
  private static final int TIMEOUT_MS = 15_000;

  // Licence prefixes used in the Google Fonts GitHub repo (tried in order)
  private static final List<String> LICENCES = List.of("ofl", "apache", "ufl");

  private GoogleFontsDownloader() {}

  /**
   * Downloads (or loads from cache) common TTF variants for {@code family} and returns a {@link
   * KawaFont} backed by local files.
   *
   * @throws IOException if the regular variant cannot be downloaded
   */
  static KawaFont download(String family) throws IOException {
    String slug = family.toLowerCase().replace(" ", "");
    String nameBase = family.replace(" ", ""); // e.g. "OpenSans" for "Open Sans"
    Files.createDirectories(CACHE_DIR);

    Path regularFile = CACHE_DIR.resolve(slug + "-400.ttf");
    Path boldFile = CACHE_DIR.resolve(slug + "-700.ttf");
    Path italicFile = CACHE_DIR.resolve(slug + "-400italic.ttf");
    Path boldItalicFile = CACHE_DIR.resolve(slug + "-700italic.ttf");

    if (!Files.exists(regularFile)) {
      byte[] bytes = downloadVariant(slug, nameBase, "Regular");
      validateTtf(bytes, family + " Regular");
      Files.write(regularFile, bytes);
    }

    if (!Files.exists(boldFile)) {
      try {
        byte[] bytes = downloadVariant(slug, nameBase, "Bold");
        validateTtf(bytes, family + " Bold");
        Files.write(boldFile, bytes);
      } catch (IOException e) {
        // Bold variant not available — use regular for both
      }
    }

    if (!Files.exists(italicFile)) {
      try {
        byte[] bytes = downloadVariant(slug, nameBase, "Italic");
        validateTtf(bytes, family + " Italic");
        Files.write(italicFile, bytes);
      } catch (IOException ignored) {
        // optional variant
      }
    }

    if (!Files.exists(boldItalicFile)) {
      try {
        byte[] bytes = downloadVariant(slug, nameBase, "BoldItalic");
        validateTtf(bytes, family + " BoldItalic");
        Files.write(boldItalicFile, bytes);
      } catch (IOException ignored) {
        // optional variant
      }
    }

    KawaFont.Builder builder =
        KawaFont.builder().regularFile(regularFile.toAbsolutePath().toString());

    if (Files.exists(boldFile)) builder.boldFile(boldFile.toAbsolutePath().toString());
    if (Files.exists(italicFile)) builder.italicFile(italicFile.toAbsolutePath().toString());
    if (Files.exists(boldItalicFile))
      builder.boldItalicFile(boldItalicFile.toAbsolutePath().toString());

    return builder.build();
  }

  // -------------------------------------------------------------------------
  // Internal
  // -------------------------------------------------------------------------

  /** Tries supported repository locations for the requested font variant. */
  private static byte[] downloadVariant(String slug, String nameBase, String variant)
      throws IOException {
    String filename = nameBase + "-" + variant + ".ttf";

    for (String licence : LICENCES) {
      // Flat layout:   ofl/lato/Lato-Regular.ttf
      // Static layout: ofl/opensans/static/OpenSans-Regular.ttf
      for (String subdir : List.of("", "static/")) {
        String url =
            "https://raw.githubusercontent.com/google/fonts/main/"
                + licence
                + "/"
                + slug
                + "/"
                + subdir
                + filename;
        try {
          return fetchBytes(url);
        } catch (IOException ignored) {
          // 404 or connection error — try next candidate
        }
      }
    }
    throw new IOException(
        "Font file '"
            + filename
            + "' not found in Google Fonts GitHub repository. "
            + "Tried ofl/, apache/, ufl/ with and without static/ subdirectory.");
  }

  private static byte[] fetchBytes(String url) throws IOException {
    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
    conn.setConnectTimeout(TIMEOUT_MS);
    conn.setReadTimeout(TIMEOUT_MS);

    int status = conn.getResponseCode();
    if (status == 404) throw new IOException("404 Not Found: " + url);
    if (status != 200) throw new IOException("HTTP " + status + " for: " + url);

    try (InputStream is = conn.getInputStream()) {
      return is.readAllBytes();
    }
  }

  /** Validates that the downloaded data looks like a TrueType or OpenType font. */
  private static void validateTtf(byte[] bytes, String label) throws IOException {
    if (bytes.length < 4) throw new IOException("Font file too small for: " + label);
    // TrueType: 00 01 00 00  or  74 72 75 65 ("true")
    // OpenType: 4F 54 54 4F ("OTTO")
    boolean isTtf =
        (bytes[0] == 0x00 && bytes[1] == 0x01 && bytes[2] == 0x00 && bytes[3] == 0x00)
            || (bytes[0] == 0x74 && bytes[1] == 0x72 && bytes[2] == 0x75 && bytes[3] == 0x65)
            || (bytes[0] == 0x4F && bytes[1] == 0x54 && bytes[2] == 0x54 && bytes[3] == 0x4F);
    if (!isTtf) {
      throw new IOException(
          "Downloaded font for '"
              + label
              + "' is not a valid TTF/OTF file "
              + "(magic bytes: "
              + String.format("%02X %02X %02X %02X", bytes[0], bytes[1], bytes[2], bytes[3])
              + ").");
    }
  }
}
