package dev.ditsche.kawa.core;

/** Mutable document-level settings used during rendering. */
public class DocumentSettings {

  private String title = "";
  private String author = "";
  private String subject = "";
  private String creator = "Kawa PDF Library";

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
