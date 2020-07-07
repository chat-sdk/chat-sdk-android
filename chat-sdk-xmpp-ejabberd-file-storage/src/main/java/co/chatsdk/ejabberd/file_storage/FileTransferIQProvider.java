package co.chatsdk.ejabberd.file_storage;

import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;

import io.reactivex.Observable;


public class FileTransferIQProvider extends IQProvider<FileTransferIQ> {

    PublishSubject<FileTransferIQ> iqPublishSubject = PublishSubject.create();

    @Override
    public FileTransferIQ parse(XmlPullParser parser, int initialDepth) throws Exception {
        FileTransferIQ iq = null;

        if (parser.getAttributeValue("", FileDownloadIQ.Download) != null) {
            FileDownloadIQ _iq = new FileDownloadIQ();
            for (int i = 0; i < parser.getAttributeCount(); i++) {
                String name = parser.getAttributeName(i);
                if (name.equals(FileDownloadIQ.Download)) {
                    _iq.url = parser.getAttributeValue(i);
                }
            }
            iq = _iq;
        }

        if (parser.getAttributeValue("", FileUploadIQ.Upload) != null) {
            FileUploadIQ _iq = new FileUploadIQ();
            for (int i = 0; i < parser.getAttributeCount(); i++) {
                String name = parser.getAttributeName(i);
                if (name.equals(FileUploadIQ.FileId)) {
                    _iq.fileId = parser.getAttributeValue(i);
                }
                if (name.equals(FileUploadIQ.Md5)) {
                    _iq.md5 = parser.getAttributeValue(i);
                }
                if (name.equals(FileUploadIQ.Upload)) {
                    _iq.url = parser.getAttributeValue(i);
                }
            }
            iq = _iq;
        }

        if (iq != null) {
            iqPublishSubject.onNext(iq);
        }
        return iq;
    }

    public Observable<FileTransferIQ> onIQ () {
        return iqPublishSubject;
    }
}
