package co.chatsdk.ejabberd.file_storage;

import org.jivesoftware.smack.packet.IQ;

public class FileTransferIQ extends IQ {

    public static String childElementName = "query";
    public static String childElementNamespace = "p1:s3filetransfer";

    protected FileTransferIQ(String childElementName, String childElementNamespace) {
        super(childElementName, childElementNamespace);
    }

    @Override
    protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
        return xml;
    }

}
