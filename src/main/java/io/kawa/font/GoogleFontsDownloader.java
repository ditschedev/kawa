package io.kawa.font;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Downloads and caches font files from the Google Fonts repository. */
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
    // Each entry: [filename, subdir-to-try]  ("" = root only, "*" = root + static/)
    List<String[]> candidates = buildCandidates(nameBase, variant);

    for (String licence : LICENCES) {
      for (String[] candidate : candidates) {
        String filename = candidate[0];
        boolean tryStatic = "*".equals(candidate[1]);
        for (String subdir : tryStatic ? List.of("", "static/") : List.of("")) {
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
    }

    // Static candidates exhausted — discover actual filenames via the GitHub API.
    // This handles fonts with non-standard axis combinations (e.g. Inter[opsz,wght].ttf).
    byte[] discovered = downloadVariantFromApi(slug, variant);
    if (discovered != null) return discovered;

    throw new IOException(
        "Font variant '"
            + nameBase
            + "-"
            + variant
            + "' not found in Google Fonts GitHub repository. "
            + "Tried ofl/, apache/, ufl/ with static/ and variable-font filename patterns.");
  }

  /**
   * Falls back to the GitHub Contents API to list the font directory and pick a matching TTF file.
   * Handles variable fonts with any axis combination (e.g. {@code Inter[opsz,wght].ttf}).
   */
  private static byte[] downloadVariantFromApi(String slug, String variant) {
    boolean wantItalic = variant.equals("Italic") || variant.equals("BoldItalic");

    for (String licence : LICENCES) {
      String apiUrl =
          "https://api.github.com/repos/google/fonts/contents/" + licence + "/" + slug;
      try {
        String json = fetchText(apiUrl);
        String filename = pickVariantFile(json, wantItalic);
        if (filename != null) {
          return fetchBytes(
              "https://raw.githubusercontent.com/google/fonts/main/"
                  + licence
                  + "/"
                  + slug
                  + "/"
                  + filename);
        }
      } catch (IOException ignored) {
        // licence directory doesn't exist — try next
      }
    }
    return null;
  }

  private static final Pattern FILENAME_PATTERN = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+\\.ttf)\"");

  /** Picks the best TTF from a GitHub API directory listing JSON for the requested italic axis. */
  private static String pickVariantFile(String json, boolean wantItalic) {
    Matcher m = FILENAME_PATTERN.matcher(json);
    String fallback = null;
    while (m.find()) {
      String name = m.group(1);
      boolean isItalic = name.toLowerCase().contains("italic");
      if (wantItalic == isItalic) return name;
      if (fallback == null) fallback = name;
    }
    return fallback;
  }

  private static String fetchText(String url) throws IOException {
    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
    conn.setConnectTimeout(TIMEOUT_MS);
    conn.setReadTimeout(TIMEOUT_MS);
    conn.setRequestProperty("Accept", "application/vnd.github+json");

    int status = conn.getResponseCode();
    if (status != 200) throw new IOException("HTTP " + status + " for: " + url);

    try (InputStream is = conn.getInputStream()) {
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  /**
   * Returns candidate [filename, subdir-flag] pairs for a variant. Subdir flag "*" means try both
   * root and static/; "" means root only (variable fonts are never in static/).
   */
  private static List<String[]> buildCandidates(String nameBase, String variant) {
    return switch (variant) {
      case "Regular" ->
          List.<String[]>of(
              new String[] {nameBase + "-Regular.ttf", "*"},
              new String[] {nameBase + "[wght].ttf", ""},
              new String[] {nameBase + "-VariableFont_wght.ttf", ""},
              new String[] {nameBase + ".ttf", ""});
      case "Bold" ->
          List.<String[]>of(
              new String[] {nameBase + "-Bold.ttf", "*"},
              // Bold is a weight on the variable font axis — reuse the same file
              new String[] {nameBase + "[wght].ttf", ""},
              new String[] {nameBase + "-VariableFont_wght.ttf", ""});
      case "Italic" ->
          List.<String[]>of(
              new String[] {nameBase + "-Italic.ttf", "*"},
              new String[] {nameBase + "-Italic[wght].ttf", ""},
              new String[] {nameBase + "[wght,ital].ttf", ""},
              new String[] {nameBase + "-VariableFont_ital,wght.ttf", ""});
      case "BoldItalic" ->
          List.<String[]>of(
              new String[] {nameBase + "-BoldItalic.ttf", "*"},
              new String[] {nameBase + "-Italic[wght].ttf", ""},
              new String[] {nameBase + "[wght,ital].ttf", ""},
              new String[] {nameBase + "-VariableFont_ital,wght.ttf", ""});
      default ->
          List.<String[]>of(new String[] {nameBase + "-" + variant + ".ttf", "*"});
    };
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
