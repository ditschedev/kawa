package dev.ditsche.kawa.core;

/**
 * Mutable document-level settings used during rendering.
 *
 * @author Tobias Dittmann
 */
public class DocumentSettings {

  private String title = "";
  private String author = "";
  private String subject = "";
  private String creator = "Kawa PDF Library";
  private boolean silent = true;

  public DocumentSettings title(String title) {
    this.title = title;
    return this;
  }

  public DocumentSettings author(String author) {
    this.author = author;
    return this;
  }

  public DocumentSettings subject(String subject) {
    this.subject = subject;
    return this;
  }

  public DocumentSettings creator(String creator) {
    this.creator = creator;
    return this;
  }

  public DocumentSettings silent(boolean silent) {
    this.silent = silent;
    return this;
  }

  public boolean isSilent() {
    return silent;
  }

  public String getTitle() {
    return title;
  }

  public String getAuthor() {
    return author;
  }

  public String getSubject() {
    return subject;
  }

  public String getCreator() {
    return creator;
  }
}
