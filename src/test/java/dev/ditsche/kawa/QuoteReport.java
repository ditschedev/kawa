package dev.ditsche.kawa;

import com.google.zxing.BarcodeFormat;
import dev.ditsche.kawa.core.*;
import dev.ditsche.kawa.elements.*;
import dev.ditsche.kawa.font.KawaFont;
import dev.ditsche.kawa.style.KawaColor;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/** Showcase document for a service quote report. */
public class QuoteReport implements KawaDocument {

  // -------------------------------------------------------------------------
  // Design tokens
  // -------------------------------------------------------------------------

  private static final KawaColor BRAND = KawaColor.rgb(34, 139, 87);
  private static final KawaColor BRAND_LIGHT = KawaColor.rgb(236, 252, 243);
  private static final KawaColor BRAND_DARK = KawaColor.rgb(22, 101, 60);
  private static final KawaColor ACCENT = KawaColor.rgb(37, 99, 235);
  private static final KawaColor MUTED = KawaColor.rgb(107, 114, 128);
  private static final KawaColor SURFACE = KawaColor.rgb(249, 250, 251);
  private static final KawaColor BORDER_COLOR = KawaColor.rgb(209, 213, 219);

  private static final KawaFont FONT = KawaFont.fromGoogle("Inter");

  // -------------------------------------------------------------------------
  // Document data
  // -------------------------------------------------------------------------
  private static final DateTimeFormatter DATE_FMT =
      DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);
  private final String quoteNumber;
  private final LocalDate date;
  private final String customerName;
  private final String customerEmail;

  public QuoteReport(
      String quoteNumber, LocalDate date, String customerName, String customerEmail) {
    this.quoteNumber = quoteNumber;
    this.date = date;
    this.customerName = customerName;
    this.customerEmail = customerEmail;
  }

  public static QuoteReport sample() {
    return new QuoteReport(
        "QT-2025-0042", LocalDate.of(2025, 6, 15), "TechStart Inc.", "orders@techstart.com");
  }

  // -------------------------------------------------------------------------
  // KawaDocument
  // -------------------------------------------------------------------------

  @Override
  public void configure(DocumentSettings s) {
    s.title("Quote " + quoteNumber).author("ACME Software Ltd.").subject("Software Services");
  }

  @Override
  public void compose(PageDefinition page) {
    page.size(PageSize.A4)
        .marginTop(40)
        .marginBottom(40)
        .marginLeft(55)
        .marginRight(55)
        .header(this::buildHeader)
        .footer(this::buildFooter)
        .content(this::buildContent);
  }

  @Override
  public void onAfterGeneration(int pageCount) {
    System.out.printf("[QuoteReport] %s → %d page(s)%n", quoteNumber, pageCount);
  }

  // -------------------------------------------------------------------------
  // Header / Footer
  // -------------------------------------------------------------------------

  private void buildHeader(ColumnElement h) {
    h.add(
        new RowElement(
            row -> {
              row.fillColumn(
                  left -> {
                    left.text("ACME Software Ltd.").bold().fontSize(14).color(BRAND).font(FONT);
                    left.text("123 Example Street  ·  London EC1A 1BB")
                        .fontSize(9)
                        .color(MUTED)
                        .font(FONT);
                  });
              row.fixedColumn(
                  130,
                  right -> {
                    right.text("Quote " + quoteNumber).bold().fontSize(10).rightAlign().font(FONT);
                    right
                        .text(date.format(DATE_FMT))
                        .fontSize(9)
                        .color(MUTED)
                        .rightAlign()
                        .font(FONT);
                  });
            }));
    h.add(new SeparatorElement().marginTop(6).marginBottom(0).color(BRAND).lineWidth(1.5f));
  }

  private void buildFooter(ColumnElement f, PageContext ctx) {
    f.add(new SeparatorElement().marginTop(0).marginBottom(4).color(BORDER_COLOR));
    f.add(
        new RowElement(
            row -> {
              row.fillColumn(
                  left -> {
                    left.add(
                        new HyperlinkElement("https://acme-software.com", "acme-software.com")
                            .fontSize(8)
                            .font(FONT));
                  });
              row.fixedColumn(
                  100,
                  mid -> {
                    mid.text("info@acme-software.com")
                        .fontSize(8)
                        .color(MUTED)
                        .centerAlign()
                        .font(FONT);
                  });
              row.fixedColumn(
                  60,
                  right -> {
                    right
                        .text("Page " + ctx.pageOf())
                        .fontSize(8)
                        .color(MUTED)
                        .rightAlign()
                        .font(FONT);
                  });
            }));
  }

  // -------------------------------------------------------------------------
  // Content
  // -------------------------------------------------------------------------

  private void buildContent(ColumnElement c) {
    buildCoverArea(c);
    c.add(new SpacerElement(16));

    buildIntroText(c);
    c.add(new SeparatorElement().marginV(14).color(BORDER_COLOR));

    buildContactBlock(c);
    c.add(new SeparatorElement().marginV(14).color(BORDER_COLOR));

    buildServiceTiers(c);
    c.add(new PageBreakElement());

    buildTermsSection(c);
    c.add(new SpacerElement(20));

    buildSignatureAndBarcode(c);
  }

  // -------------------------------------------------------------------------
  // Section builders
  // -------------------------------------------------------------------------

  /** StackElement: document title text layered over a coloured spacer block. */
  private void buildCoverArea(ColumnElement c) {
    c.add(
        new RowElement(
            row -> {
              row.fillColumn(
                  left -> {
                    left.add(
                        new StackElement()
                            .layer(new SpacerElement(72))
                            .layer(
                                new ColumnElement(
                                    col -> {
                                      col.spacing(3);
                                      col.text("Software Development")
                                          .fontSize(9)
                                          .color(MUTED)
                                          .font(FONT);
                                      col.text("Quote for " + customerName)
                                          .bold()
                                          .fontSize(20)
                                          .color(BRAND_DARK)
                                          .font(FONT);
                                      col.text("Valid until: " + date.plusDays(30).format(DATE_FMT))
                                          .fontSize(10)
                                          .color(MUTED)
                                          .font(FONT);
                                    })));
                  });

              // QrCodeElement: link to online version of the quote
              row.fixedColumn(
                  90,
                  right -> {
                    right.add(new SpacerElement(4));
                    right.add(
                        new QrCodeElement("https://acme-software.com/quote/" + quoteNumber)
                            .size(82));
                    right.text("View online").fontSize(7).color(MUTED).centerAlign().font(FONT);
                  });
            }));
  }

  /** RichTextElement: mixed-style introduction paragraph. */
  private void buildIntroText(ColumnElement c) {
    c.add(new TextElement("Dear Sir or Madam,").fontSize(11).font(FONT));
    c.add(new SpacerElement(8));
    c.add(
        new RichTextElement()
            .span(
                "We are pleased to present our quote for the development of your "
                    + "custom software solution. Based on our discussions, we have defined "
                    + "three service packages that differ in scope and budget. Our ",
                s -> s.fontSize(10).font(FONT))
            .span("Professional package", s -> s.fontSize(10).bold().color(BRAND).font(FONT))
            .span(
                " is our recommended choice, offering the best balance of features "
                    + "and investment. All packages include ",
                s -> s.fontSize(10).font(FONT))
            .span("12 months of support", s -> s.fontSize(10).bold().font(FONT))
            .span(" and comprehensive technical documentation.", s -> s.fontSize(10).font(FONT)));
  }

  /** GridElement: compact key/value address and contact block. */
  private void buildContactBlock(ColumnElement c) {
    c.add(new TextElement("Client").bold().fontSize(11).color(BRAND_DARK).font(FONT));
    c.add(new SpacerElement(8));

    c.add(
        new GridElement(
            grid -> {
              grid.columns(
                  cols -> {
                    cols.fixed(90);
                    cols.relative(1);
                  });
              grid.rowGap(5);
              for (String[] kv :
                  new String[][] {
                    {"Company:", customerName},
                    {"Contact:", "Ms. / Mr. N. N."},
                    {"Email:", customerEmail},
                    {"Quote No.:", quoteNumber},
                    {"Date:", date.format(DATE_FMT)},
                    {"Valid until:", date.plusDays(30).format(DATE_FMT)}
                  }) {
                String key = kv[0], val = kv[1];
                grid.row(
                    r -> {
                      r.cell(col -> col.text(key).fontSize(9).bold().color(MUTED).font(FONT));
                      r.cell(col -> col.text(val).fontSize(9).font(FONT));
                    });
              }
            }));
  }

  /**
   * Three service tiers in a GridElement; middle tier uses StackElement for the "RECOMMENDED"
   * badge.
   */
  private void buildServiceTiers(ColumnElement c) {
    c.add(new TextElement("Service Packages").bold().fontSize(11).color(BRAND_DARK).font(FONT));
    c.add(new SpacerElement(10));

    record Tier(String name, String price, List<String> features, boolean highlighted) {}
    List<Tier> tiers =
        List.of(
            new Tier(
                "Basic",
                "$ 4,900",
                List.of("up to 3 months", "1 developer", "email support", "basic docs"),
                false),
            new Tier(
                "Professional",
                "$ 9,900",
                List.of(
                    "up to 6 months",
                    "2 developers",
                    "priority support",
                    "full documentation",
                    "CI/CD setup"),
                true),
            new Tier(
                "Enterprise",
                "$ 18,900",
                List.of(
                    "up to 12 months",
                    "4+ developers",
                    "dedicated support",
                    "full documentation",
                    "CI/CD + monitoring",
                    "SLA 99.9%"),
                false));

    c.add(
        new GridElement(
            grid -> {
              grid.columns(
                  cols -> {
                    cols.relative(1);
                    cols.relative(1);
                    cols.relative(1);
                  });
              grid.columnGap(10);
              grid.row(
                  r -> {
                    for (Tier tier : tiers) {
                      r.cell(
                          col -> {
                            if (tier.highlighted()) {
                              // StackElement: content card + "RECOMMENDED" badge overlay
                              col.add(
                                  new StackElement()
                                      .layer(
                                          buildTierCard(
                                              tier.name(), tier.price(), tier.features(), true))
                                      .layer(
                                          new ColumnElement(
                                              badge -> {
                                                badge
                                                    .text("* RECOMMENDED")
                                                    .fontSize(7f)
                                                    .bold()
                                                    .color(KawaColor.WHITE)
                                                    .centerAlign()
                                                    .font(FONT);
                                              })));
                            } else {
                              col.add(
                                  buildTierCard(tier.name(), tier.price(), tier.features(), false));
                            }
                          });
                    }
                  });
            }));
  }

  private ColumnElement buildTierCard(
      String name, String price, List<String> features, boolean highlighted) {
    KawaColor bg = highlighted ? BRAND_LIGHT : SURFACE;
    KawaColor border = highlighted ? BRAND : BORDER_COLOR;

    ColumnElement card = new ColumnElement();
    card.spacing(4);

    card.text(name)
        .bold()
        .fontSize(11)
        .color(highlighted ? BRAND_DARK : KawaColor.rgb(30, 30, 30))
        .font(FONT);

    card.text(price)
        .bold()
        .fontSize(16)
        .color(highlighted ? BRAND : KawaColor.rgb(30, 30, 30))
        .font(FONT);

    card.add(new SeparatorElement().marginV(4).color(border).lineWidth(0.5f));

    for (String f : features) {
      card.text("+ " + f).fontSize(8.5f).color(MUTED).font(FONT);
    }

    return card;
  }

  /** Terms section on page 2 after the PageBreakElement. */
  private void buildTermsSection(ColumnElement c) {
    c.add(
        new TextElement("Terms and Conditions (Excerpt)")
            .bold()
            .fontSize(11)
            .color(BRAND_DARK)
            .font(FONT));
    c.add(new SpacerElement(10));

    for (String[] section :
        new String[][] {
          {
            "§ 1 Scope",
            "These terms apply to all contracts between ACME Software Ltd. and the client, "
                + "unless otherwise explicitly agreed. Deviating terms of the client shall not be "
                + "recognised."
          },
          {
            "§ 2 Fees and Payment",
            "Invoices are due within 14 days of the invoice date without deduction. "
                + "In the event of late payment, interest shall be charged at 9 percentage "
                + "points above the base rate."
          },
          {
            "§ 3 Warranty",
            "The warranty period is 12 months from acceptance. Defects must be reported "
                + "in writing without delay. The contractor is entitled to two attempts at "
                + "rectification before further rights of the client may be asserted."
          },
        }) {
      String heading = section[0], body = section[1];
      c.add(
          new RichTextElement()
              .span(heading + "  ", s -> s.fontSize(10).bold().font(FONT))
              .span(body, s -> s.fontSize(9).color(MUTED).font(FONT)));
      c.add(new SpacerElement(8));
    }

    c.add(new SeparatorElement().marginV(12).color(BORDER_COLOR));

    // Acceptance block
    c.add(new TextElement("Order Confirmation").bold().fontSize(11).color(BRAND_DARK).font(FONT));
    c.add(new SpacerElement(8));
    c.add(
        new RichTextElement()
            .span("By signing this quote, ")
            .span(customerName, s -> s.bold())
            .span(" engages ACME Software Ltd. under the terms stated above. This quote is ")
            .span("valid until " + date.plusDays(30).format(DATE_FMT), s -> s.bold().color(BRAND))
            .span("."));

    c.add(new SpacerElement(30));
    c.add(
        new GridElement(
            grid -> {
              grid.columns(
                  cols -> {
                    cols.relative(1);
                    cols.fixed(20);
                    cols.relative(1);
                  });
              grid.row(
                  r -> {
                    r.cell(
                        col -> {
                          col.add(
                              new SeparatorElement()
                                  .marginTop(0)
                                  .marginBottom(4)
                                  .color(KawaColor.BLACK)
                                  .lineWidth(0.3f));
                          col.text("Place, Date").fontSize(8).color(MUTED).font(FONT);
                        });
                    r.cell(col -> col.add(new SpacerElement(1))); // gap
                    r.cell(
                        col -> {
                          col.add(
                              new SeparatorElement()
                                  .marginTop(0)
                                  .marginBottom(4)
                                  .color(KawaColor.BLACK)
                                  .lineWidth(0.3f));
                          col.text("Client Signature").fontSize(8).color(MUTED).font(FONT);
                        });
                  });
            }));
  }

  /** Barcode for document reference + footer hyperlinks. */
  private void buildSignatureAndBarcode(ColumnElement c) {
    c.add(
        new RowElement(
            row -> {
              row.fillColumn(
                  left -> {
                    left.text("Document Reference").fontSize(8).color(MUTED).font(FONT);
                    left.add(new SpacerElement(4));
                    left.add(
                        new BarcodeElement(quoteNumber)
                            .format(BarcodeFormat.CODE_128)
                            .width(160)
                            .height(36));
                    left.add(new SpacerElement(3));
                    left.text(quoteNumber).fontSize(7).color(MUTED).font(FONT);
                  });
              row.fixedColumn(
                  200,
                  right -> {
                    right.add(new SpacerElement(8));
                    right.text("Related Links").fontSize(8).bold().color(MUTED).font(FONT);
                    right.add(new SpacerElement(4));
                    right.add(
                        new HyperlinkElement(
                                "https://acme-software.com/terms", "acme-software.com/terms")
                            .fontSize(9)
                            .font(FONT));
                    right.add(new SpacerElement(2));
                    right.add(
                        new HyperlinkElement(
                                "https://acme-software.com/privacy", "acme-software.com/privacy")
                            .fontSize(9)
                            .font(FONT));
                    right.add(new SpacerElement(2));
                    right.add(
                        new HyperlinkElement(
                                "mailto:info@acme-software.com", "info@acme-software.com")
                            .fontSize(9)
                            .font(FONT));
                  });
            }));
  }
}
