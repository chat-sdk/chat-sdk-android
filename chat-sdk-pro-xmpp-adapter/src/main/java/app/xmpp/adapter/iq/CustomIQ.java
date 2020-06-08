package app.xmpp.adapter.iq;

import org.jivesoftware.smack.packet.IQ;

public class CustomIQ extends IQ {

    protected XMLBuilder xmlBuilder;

    public interface XMLBuilder {
        IQChildElementXmlStringBuilder build(IQChildElementXmlStringBuilder xml);
    }

    public CustomIQ(String childElementName, String childElementNamespace) {
        super(childElementName, childElementNamespace);
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        if (xmlBuilder != null) {
            return xmlBuilder.build(xml);
        } else {
            xml.rightAngleBracket();
            return xml;
        }
    }

    public void setXmlBuilder (XMLBuilder builder) {
        xmlBuilder = builder;
    }

}
