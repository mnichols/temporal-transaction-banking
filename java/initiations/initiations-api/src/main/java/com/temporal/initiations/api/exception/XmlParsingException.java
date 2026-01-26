package com.temporal.initiations.api.exception;

/**
 * Exception thrown when XML parsing or validation fails.
 *
 * This is a specialized validation exception for XML-specific parsing errors.
 */
public class XmlParsingException extends ValidationException {

    public XmlParsingException(String message) {
        this("XML_PARSING_ERROR", message, null);
    }

    public XmlParsingException(String message, String details) {
        this("XML_PARSING_ERROR", message, details);
    }

    public XmlParsingException(String code, String message, String details) {
        super(code, message, details);
    }
}
