package io.kawa.font;

import java.io.IOException;
import java.util.TreeMap;

/**
 * Describes a font family used by document elements.
 *
 * <p>A family maps numeric CSS-style weights (100–900) to font sources, independently for upright
 * and italic axes. At least one upright source is required; all other weights are resolved to the
 * nearest registered one.
 *
 * <p>Named builder methods ({@link Builder#regular}, {@link Builder#semiBold}, {@link
 * Builder#bold}, …) are shortcuts for the corresponding weight constant from {@link FontWeight}.
 * The generic {@link Builder#weight(int, FontSource)} accepts any value in [1, 1000].
 */
public final class KawaFont {

  private final TreeMap<Integer, FontSource> uprightSources;
  private final TreeMap<Integer, FontSource> italicSources;

  private KawaFont(TreeMap<Integer, FontSource> upright, TreeMap<Integer, FontSource> italic) {
    if (upright.isEmpty())
      throw new IllegalArgumentException("At least one upright font source is required");
    this.uprightSources = new TreeMap<>(upright);
    this.italicSources = new TreeMap<>(italic);
  }

  // -------------------------------------------------------------------------
  // Quick factories — resource (classpath)
  // -------------------------------------------------------------------------

  /** Loads a single regular (weight 400) variant from a classpath resource. */
  public static KawaFont ofResource(String regularPath) {
    return builder().weightResource(FontWeight.REGULAR, regularPath).build();
  }

  // -------------------------------------------------------------------------
  // Quick factories — file system
  // -------------------------------------------------------------------------

  /** Loads a single regular (weight 400) variant from a file-system path. */
  public static KawaFont ofFile(String regularPath) {
    return builder().weight(FontWeight.REGULAR, regularPath).build();
  }

  // -------------------------------------------------------------------------
  // Factory — Google Fonts
  // -------------------------------------------------------------------------

  /**
   * Downloads the named Google Fonts family and returns it as a {@code KawaFont}.
   *
   * @param family Google Fonts family name
   * @return the downloaded font, or {@code null} if the download fails
   */
  public static KawaFont fromGoogle(String family) {
    try {
      return GoogleFontsDownloader.download(family);
    } catch (IOException e) {
      System.err.println(
          "[Kawa] Warning: could not download Google Font '"
              + family
              + "': "
              + e.getMessage()
              + " — falling back to default font.");
      return null;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  // -------------------------------------------------------------------------
  // Internal resolution
  // -------------------------------------------------------------------------

  private static FontSource nearest(TreeMap<Integer, FontSource> map, int weight) {
    if (map.isEmpty()) return null;
    Integer lo = map.floorKey(weight);
    Integer hi = map.ceilingKey(weight);
    if (lo == null) return map.get(hi);
    if (hi == null) return map.get(lo);
    // ties go to the heavier (higher) weight
    return (weight - lo < hi - weight) ? map.get(lo) : map.get(hi);
  }

  /**
   * Resolves the best-matching {@link FontSource} for the requested weight and italic axis. Falls
   * back from italic to upright if no italic sources are registered.
   */
  FontSource resolveSource(int weight, boolean italic) {
    if (italic && !italicSources.isEmpty()) {
      FontSource src = nearest(italicSources, weight);
      if (src != null) return src;
    }
    return nearest(uprightSources, weight);
  }

  // -------------------------------------------------------------------------
  // Builder
  // -------------------------------------------------------------------------

  public static final class Builder {
    private final TreeMap<Integer, FontSource> upright = new TreeMap<>();
    private final TreeMap<Integer, FontSource> italic = new TreeMap<>();

    // --- Generic ---

    /** Registers an upright source for the given numeric weight. */
    public Builder weight(int w, FontSource source) {
      upright.put(w, source);
      return this;
    }

    /** Registers an italic source for the given numeric weight. */
    public Builder weightItalic(int w, FontSource source) {
      italic.put(w, source);
      return this;
    }

    // --- File-path overloads ---

    /** Registers an upright source from a file-system path for the given weight. */
    public Builder weight(int w, String filePath) {
      return weight(w, new FileFontSource(filePath));
    }

    /** Registers an italic source from a file-system path for the given weight. */
    public Builder weightItalic(int w, String filePath) {
      return weightItalic(w, new FileFontSource(filePath));
    }

    // --- Classpath-resource overloads ---

    /** Registers an upright source from a classpath resource for the given weight. */
    public Builder weightResource(int w, String resourcePath) {
      return weight(w, new ResourceFontSource(resourcePath));
    }

    /** Registers an italic source from a classpath resource for the given weight. */
    public Builder weightItalicResource(int w, String resourcePath) {
      return weightItalic(w, new ResourceFontSource(resourcePath));
    }

    // --- Named upright shortcuts ---

    public Builder thin(FontSource s) {
      return weight(FontWeight.THIN, s);
    }

    public Builder extraLight(FontSource s) {
      return weight(FontWeight.EXTRA_LIGHT, s);
    }

    public Builder light(FontSource s) {
      return weight(FontWeight.LIGHT, s);
    }

    public Builder regular(FontSource s) {
      return weight(FontWeight.REGULAR, s);
    }

    public Builder medium(FontSource s) {
      return weight(FontWeight.MEDIUM, s);
    }

    public Builder semiBold(FontSource s) {
      return weight(FontWeight.SEMI_BOLD, s);
    }

    public Builder bold(FontSource s) {
      return weight(FontWeight.BOLD, s);
    }

    public Builder extraBold(FontSource s) {
      return weight(FontWeight.EXTRA_BOLD, s);
    }

    public Builder black(FontSource s) {
      return weight(FontWeight.BLACK, s);
    }

    // --- Named italic shortcuts ---

    public Builder regularItalic(FontSource s) {
      return weightItalic(FontWeight.REGULAR, s);
    }

    public Builder semiBoldItalic(FontSource s) {
      return weightItalic(FontWeight.SEMI_BOLD, s);
    }

    public Builder boldItalic(FontSource s) {
      return weightItalic(FontWeight.BOLD, s);
    }

    // --- Named file-path shortcuts (common patterns) ---

    public Builder regularFile(String p) {
      return weight(FontWeight.REGULAR, new FileFontSource(p));
    }

    public Builder boldFile(String p) {
      return weight(FontWeight.BOLD, new FileFontSource(p));
    }

    public Builder italicFile(String p) {
      return weightItalic(FontWeight.REGULAR, new FileFontSource(p));
    }

    public Builder boldItalicFile(String p) {
      return weightItalic(FontWeight.BOLD, new FileFontSource(p));
    }

    // --- Named resource shortcuts ---

    public Builder regularResource(String p) {
      return weight(FontWeight.REGULAR, new ResourceFontSource(p));
    }

    public Builder boldResource(String p) {
      return weight(FontWeight.BOLD, new ResourceFontSource(p));
    }

    public Builder italicResource(String p) {
      return weightItalic(FontWeight.REGULAR, new ResourceFontSource(p));
    }

    public Builder boldItalicResource(String p) {
      return weightItalic(FontWeight.BOLD, new ResourceFontSource(p));
    }

    public KawaFont build() {
      return new KawaFont(upright, italic);
    }
  }
}
