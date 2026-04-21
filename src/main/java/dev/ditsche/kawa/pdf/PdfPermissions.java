package dev.ditsche.kawa.pdf;

import org.apache.pdfbox.pdmodel.encryption.AccessPermission;

/**
 * Fluent builder around PDFBox access permissions.
 *
 * @author Tobias Dittmann
 */
public final class PdfPermissions {

  private final AccessPermission accessPermission;

  private PdfPermissions(AccessPermission accessPermission) {
    this.accessPermission = accessPermission;
  }

  public static PdfPermissions fullAccess() {
    return new PdfPermissions(AccessPermission.getOwnerAccessPermission());
  }

  public static Builder builder() {
    return new Builder();
  }

  AccessPermission toAccessPermission() {
    return accessPermission;
  }

  public static final class Builder {
    private final AccessPermission accessPermission = new AccessPermission();

    public Builder allowPrint(boolean allow) {
      accessPermission.setCanPrint(allow);
      return this;
    }

    public Builder allowModify(boolean allow) {
      accessPermission.setCanModify(allow);
      return this;
    }

    public Builder allowExtractContent(boolean allow) {
      accessPermission.setCanExtractContent(allow);
      return this;
    }

    public Builder allowModifyAnnotations(boolean allow) {
      accessPermission.setCanModifyAnnotations(allow);
      return this;
    }

    public Builder allowFillInForm(boolean allow) {
      accessPermission.setCanFillInForm(allow);
      return this;
    }

    public Builder allowExtractForAccessibility(boolean allow) {
      accessPermission.setCanExtractForAccessibility(allow);
      return this;
    }

    public Builder allowAssembleDocument(boolean allow) {
      accessPermission.setCanAssembleDocument(allow);
      return this;
    }

    public Builder allowPrintFaithful(boolean allow) {
      accessPermission.setCanPrintFaithful(allow);
      return this;
    }

    public PdfPermissions build() {
      return new PdfPermissions(accessPermission);
    }
  }
}
