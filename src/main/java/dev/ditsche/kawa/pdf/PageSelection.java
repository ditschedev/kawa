package dev.ditsche.kawa.pdf;

import java.util.*;

/**
 * Represents a user-facing, one-based page selection.
 *
 * @author Tobias Dittmann
 */
public final class PageSelection {

  private static final PageSelection ALL = new PageSelection(true, List.of());

  private final boolean selectAll;
  private final List<Integer> pageNumbers;

  private PageSelection(boolean selectAll, List<Integer> pageNumbers) {
    this.selectAll = selectAll;
    this.pageNumbers = pageNumbers;
  }

  public static PageSelection all() {
    return ALL;
  }

  public static PageSelection of(int... pageNumbers) {
    if (pageNumbers.length == 0) {
      throw new IllegalArgumentException("At least one page number is required");
    }

    Set<Integer> ordered = new LinkedHashSet<>();
    Arrays.stream(pageNumbers)
        .forEach(
            page -> {
              if (page < 1) {
                throw new IllegalArgumentException("Page numbers are 1-based and must be >= 1");
              }
              ordered.add(page);
            });
    return new PageSelection(false, List.copyOf(ordered));
  }

  public static PageSelection range(int startInclusive, int endInclusive) {
    if (startInclusive < 1 || endInclusive < startInclusive) {
      throw new IllegalArgumentException(
          "Invalid page range: " + startInclusive + "-" + endInclusive);
    }

    List<Integer> pages = new ArrayList<>();
    for (int page = startInclusive; page <= endInclusive; page++) {
      pages.add(page);
    }
    return new PageSelection(false, List.copyOf(pages));
  }

  public static PageSelection parse(String spec) {
    if (spec == null || spec.isBlank()) {
      throw new IllegalArgumentException("Page selection spec must not be blank");
    }

    Set<Integer> ordered = new LinkedHashSet<>();
    for (String token : spec.split(",")) {
      String trimmed = token.trim();
      if (trimmed.isEmpty()) {
        continue;
      }

      int dash = trimmed.indexOf('-');
      if (dash >= 0) {
        int start = Integer.parseInt(trimmed.substring(0, dash).trim());
        int end = Integer.parseInt(trimmed.substring(dash + 1).trim());
        if (start < 1 || end < start) {
          throw new IllegalArgumentException("Invalid page range: " + trimmed);
        }
        for (int page = start; page <= end; page++) {
          ordered.add(page);
        }
      } else {
        int page = Integer.parseInt(trimmed);
        if (page < 1) {
          throw new IllegalArgumentException("Page numbers are 1-based and must be >= 1");
        }
        ordered.add(page);
      }
    }

    if (ordered.isEmpty()) {
      throw new IllegalArgumentException("Page selection spec produced no pages");
    }

    return new PageSelection(false, List.copyOf(ordered));
  }

  List<Integer> resolve(int pageCount) {
    if (selectAll) {
      List<Integer> pages = new ArrayList<>(pageCount);
      for (int page = 1; page <= pageCount; page++) {
        pages.add(page);
      }
      return pages;
    }

    for (int page : pageNumbers) {
      if (page > pageCount) {
        throw new IllegalArgumentException(
            "Page selection references page "
                + page
                + " but document has only "
                + pageCount
                + " pages");
      }
    }
    return pageNumbers;
  }
}
