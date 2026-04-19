package io.kawa.elements;

/** Marker interface for leaf elements that render visible content. */
public sealed interface ContentElement extends Element
    permits TextElement,
        ImageElement,
        SpacerElement,
        SeparatorElement,
        PageBreakElement,
        RichTextElement,
        HyperlinkElement,
        QrCodeElement,
        BarcodeElement,
        InputElement,
        CustomElement {}
