# Kawa

**Readable PDF composition for Java.**

Kawa is an open source Java library for building structured PDF documents with a fluent API.
Compose reports, invoices, and generated documents with text, tables, images, layout primitives, styling, and PDF manipulation without dropping into low-level PDF code.

[Read the documentation](https://kawa.ditsche.dev) • [Getting Started](https://kawa.ditsche.dev/docs/getting-started) • [GitHub Releases](https://github.com/ditschedev/kawa/releases)

> [!WARNING]
> Kawa v1 is still in development. The API is not stable yet and may change at any time until the first stable release.

## Why Kawa

- Fluent Java API, no templates or XML
- Automatic pagination, headers, and footers
- Tables, media, barcodes, QR codes, and form elements
- Custom fonts, reusable styles, and unit helpers
- PDF manipulation with `PdfDocument`

## Installation

```xml
<dependency>
  <groupId>dev.ditsche</groupId>
  <artifactId>kawa</artifactId>
  <version>0.1.1</version>
</dependency>
```

Requires Java 17+.

## Quick Example

```java

import core.dev.ditsche.kawa.PageSize;

Document.create(doc ->{
  doc.

title("Hello Kawa");

    doc.

page(page ->page
  .

size(PageSize.A4)
        .

margin(50)
        .

content(c ->{
  c.

item().

text("Hello, Kawa!").

bold().

fontSize(24);
            c.

item().

text("This PDF was generated in Java.");
        })
          );
          }).

generatePdf("hello.pdf");
```

## Development

Contributions are welcome.

- Run the Java tests with `mvn test`
- Start the docs app with `cd docs && pnpm dev`
- Use the docs site for the full guides and API explanations: [kawa.ditsche.dev](https://kawa.ditsche.dev)

If you want to contribute, opening an issue or pull request with a small focused change is the easiest way to get started.

Built on top of [Apache PDFBox](https://pdfbox.apache.org/) which is licensed under Apache License 2.0.
