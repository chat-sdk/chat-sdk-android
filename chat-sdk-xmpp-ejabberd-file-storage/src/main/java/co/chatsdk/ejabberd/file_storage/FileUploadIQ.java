package co.chatsdk.ejabberd.file_storage;

import org.jivesoftware.smack.packet.IQ;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;

import java.util.UUID;

import app.xmpp.adapter.iq.CustomIQ;

public class FileUploadIQ extends FileTransferIQ {

    public static String FileId = "fileid";
    public static String Md5 = "md5";
    public static String Upload = "upload";

    public String url;
    public String md5;
    public String fileId;

    protected FileUploadIQ() {
        super(FileTransferIQ.childElementName, FileTransferIQ.childElementNamespace);
    }

    public static CustomIQ build (Jid serverJID, String md5) throws Exception {
        CustomIQ iq = new CustomIQ(childElementName, childElementNamespace);
        iq.setStanzaId(UUID.randomUUID().toString());
        iq.setType(IQ.Type.set);
        iq.setXmlBuilder(xml -> {
            xml.attribute(Md5, md5);
            xml.rightAngleBracket();
            return xml;
        });
        iq.setTo(serverJID);
        return iq;
    }

}
