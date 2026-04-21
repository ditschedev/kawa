package dev.ditsche.kawa.elements;

/**
 * Marker interface for leaf elements that render visible content.
 *
 * @author Tobias Dittmann
 */
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
        ButtonElement,
        CheckboxElement,
        RadioButtonElement,
        ChoiceElement,
        ComboboxElement,
        CustomElement,
        BarChartElement,
        LineChartElement,
        PieChartElement {}
