package dev.ditsche.kawa.core;

/**
 * Runtime information about the current physical page.
 *
 * <p>Instances are provided to dynamic header and footer builders.
 *
 * @author Tobias Dittmann
 */
public record PageContext(int pageNumber, int totalPages) {

  /** 1-based index of the current physical page. */
  @Override
  public int pageNumber() {
    return pageNumber;
  }

  /**
   * Total number of physical pages in the document. Returns {@code -1} during the first layout pass
   * (before pagination is complete).
   */
  @Override
  public int totalPages() {
    return totalPages;
  }

  /** Convenience: {@code "3 / 7"} style string. */
  public String pageOf() {
    return totalPages > 0 ? pageNumber + " / " + totalPages : String.valueOf(pageNumber);
  }
}
