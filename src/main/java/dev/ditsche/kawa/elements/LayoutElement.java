package dev.ditsche.kawa.elements;

/**
 * Marker interface for structural elements that arrange child elements.
 *
 * @author Tobias Dittmann
 */
public sealed interface LayoutElement extends Element
    permits TableElement,
        RowElement,
        ColumnElement,
        ContainerElement,
        PaddingElement,
        GridElement,
        StackElement {}
