package sdk.chat.message.file;

import android.content.Context;
import android.graphics.Bitmap;

import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import sdk.chat.core.base.AbstractMessageHandler;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.handlers.FileMessageHandler;
import sdk.chat.core.manager.MessagePayload;
import sdk.chat.core.rigs.BitmapUploadable;
import sdk.chat.core.rigs.FileUploadable;
import sdk.chat.core.rigs.MessageSendRig;
import sdk.chat.core.rigs.Uploadable;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageType;

/**
 * Created by Pepe on 01/05/18.
 */

public class BaseFileMessageHandler extends AbstractMessageHandler implements FileMessageHandler {

    public static String imageName = "image.jpg";
    public static String imageMimeType = "image/jpeg";

    public Completable sendMessageWithFile(final String fileName, final String mimeType, File file, final Thread thread) {

        List<Uploadable> uploadables = new ArrayList<>();
        uploadables.add(new FileUploadable(file, fileName, mimeType, Keys.MessageFileURL));

        if (mimeType.equals("application/pdf")) {
            // Generate the preview image
            try {
                Bitmap bitmap = pdfPreview(file);
                if (bitmap != null) {
                    uploadables.add(new BitmapUploadable(bitmap, imageName, imageMimeType, Keys.MessageImageURL));
                }
            } catch (Exception e) {
                // Just abort and don't send the preview
                e.printStackTrace();
            }
        }

        MessageSendRig rig = new MessageSendRig(new MessageType(MessageType.File), thread, message -> {
            // First pass back an empty result so that we add the cell to the table view
            message.setValueForKey(fileName, Keys.MessageText);
            message.setValueForKey(mimeType, Keys.MessageMimeType);
            message.setValueForKey(file.length(), Keys.MessageSize);
            message.setFilePath(file.getPath());
            ChatSDK.db().update(message);

        }).setUploadables(uploadables, null);
        return rig.run();
    }

    protected Bitmap pdfPreview(File file) throws Exception {
        Context context = ChatSDK.shared().context();

        int pageNumber = 0;
        PdfiumCore pdfiumCore = new PdfiumCore(context);

        PdfDocument pdf = pdfiumCore.newDocument(fileToBytes(file));
        pdfiumCore.openPage(pdf, pageNumber);
        int width = pdfiumCore.getPageWidthPoint(pdf, pageNumber);
        int height = pdfiumCore.getPageHeightPoint(pdf, pageNumber);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        pdfiumCore.renderPageBitmap(pdf, bitmap, pageNumber, 0, 0, width, height);
        pdfiumCore.closeDocument(pdf); // important!

        return bitmap;
    }

    protected byte[] fileToBytes(File file) throws Exception {
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
        buf.read(bytes, 0, bytes.length);
        buf.close();
        return bytes;
    }

    @Override
    public MessagePayload payloadFor(Message message) {
        return new FileMessagePayload(message);
    }

    @Override
    public boolean isFor(MessageType type) {
        return type != null && type.is(MessageType.File);
    }

}
