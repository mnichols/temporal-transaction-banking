package com.temporal.initiations.api.validation;

import com.temporal.initiations.api.exception.XmlParsingException;
import com.temporal.initiations.api.exception.ValidationException;
import org.springframework.stereotype.Service;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * Service for validating PAIN.001.001.03 XML content.
 *
 * This service validates XML well-formedness and basic structure.
 * It checks that content is non-empty and valid XML.
 */
@Service
public class PainXmlValidator {

    /**
     * Validates that the provided XML content is well-formed and non-empty.
     *
     * @param xmlContent The XML content to validate
     * @throws ValidationException if the content is null or blank
     * @throws XmlParsingException if the XML is malformed or structurally invalid
     */
    public void validate(String xmlContent) {
        // Check for null or empty content
        if (xmlContent == null || xmlContent.trim().isEmpty()) {
            throw new ValidationException(
                "EMPTY_CONTENT",
                "File content cannot be empty or blank"
            );
        }

        // Validate XML well-formedness
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // Disable external entity processing to prevent XXE attacks
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.parse(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8)));
        } catch (org.xml.sax.SAXParseException e) {
            throw new XmlParsingException(
                "XML parsing failed at line " + e.getLineNumber() + ", column " + e.getColumnNumber(),
                e.getMessage()
            );
        } catch (org.xml.sax.SAXException e) {
            throw new XmlParsingException("Invalid XML structure", e.getMessage());
        } catch (Exception e) {
            throw new XmlParsingException("XML parsing error", e.getMessage());
        }
    }
}
