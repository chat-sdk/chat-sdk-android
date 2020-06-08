package co.chatsdk.ejabberd.file_storage;

import org.jivesoftware.smack.packet.IQ;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;

import java.util.UUID;

import app.xmpp.adapter.iq.CustomIQ;

public class FileDownloadIQ extends FileTransferIQ {

    public static String Download = "download";
    public String url;

    protected FileDownloadIQ() {
        super(FileTransferIQ.childElementName, FileTransferIQ.childElementNamespace);
    }

    public static CustomIQ build (Jid serverJID, String fileId) throws Exception {
        CustomIQ iq = new CustomIQ(childElementName, childElementNamespace);
        iq.setStanzaId(UUID.randomUUID().toString());
        iq.setType(IQ.Type.get);
        iq.setXmlBuilder(xml -> {
            xml.attribute(FileUploadIQ.FileId, fileId);
            xml.rightAngleBracket();
            return xml;
        });
        iq.setTo(serverJID);
        return iq;
    }
}
