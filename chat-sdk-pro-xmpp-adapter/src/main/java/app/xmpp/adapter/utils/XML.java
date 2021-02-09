package app.xmpp.adapter.utils;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

public class XML {

    private static final TransformerFactory transformerFactory = TransformerFactory.newInstance();

    static {
//        try {
//            transformerFactory.setAttribute("indent-number", 2);
//        } catch (IllegalArgumentException e) {
//            LOGGER.log(Level.INFO, "XML TransformerFactory does not support indent-number attribute", e);
//        }
    }

    public static String prettyFormatXml(CharSequence xml) {
        String xmlString = xml.toString();
        StreamSource source = new StreamSource(new StringReader(xmlString));
        StringWriter stringWriter = new StringWriter();
        StreamResult result = new StreamResult(stringWriter);

        try {
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            // Transform the requested string into a nice formatted XML string
            transformer.transform(source, result);
        }
        catch (TransformerException | IllegalArgumentException e) {
//            LOGGER.log(Level.SEVERE, "Transformer error", e);
            return xmlString;
        }

        return stringWriter.toString();
    }

}
