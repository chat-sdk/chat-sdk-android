package co.chatsdk.message.file;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.view.View;

import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import co.chatsdk.core.base.AbstractMessageViewHolder;
import co.chatsdk.core.base.AbstractThreadHandler;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.handlers.FileMessageHandler;
import co.chatsdk.core.rigs.BitmapUploadable;
import co.chatsdk.core.rigs.FileUploadable;
import co.chatsdk.core.rigs.MessageSendRig;
import co.chatsdk.core.rigs.Uploadable;
import co.chatsdk.core.rx.ObservableConnector;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.FileUploadResult;
import co.chatsdk.core.types.MessageSendProgress;
import co.chatsdk.core.types.MessageSendStatus;
import co.chatsdk.core.types.MessageType;
import io.reactivex.Completable;

/**
 * Created by Pepe on 01/05/18.
 */

public class BaseFileMessageHandler implements FileMessageHandler {

    public static String imageName = "image.jpg";
    public static String imageMimeType = "image/jpeg";

    public Completable sendMessageWithFile(final String fileName, final String mimeType, File file, final Thread thread) {
        MessageSendRig rig = new MessageSendRig(new MessageType(MessageType.File), thread, message -> {
            // First pass back an empty result so that we add the cell to the table view
            message.setValueForKey(fileName, Keys.MessageText);
            message.setValueForKey(mimeType, Keys.MessageMimeType);
        }).setUploadable(new FileUploadable(file, fileName, mimeType), (message, result) -> {
            // Set the File URL
            message.setValueForKey(result.url, Keys.MessageFileURL);
        });

        if (mimeType.equals("application/pdf")) {

            // Generate the preview image
            try {
                Bitmap bitmap = pdfPreview(file);
                if (bitmap != null) {
                    rig.setUploadable(new BitmapUploadable(bitmap, imageName, imageMimeType), (message, result) -> {
                        if(result.mimeType.equals(imageMimeType)) {
                            message.setValueForKey(result.url, Keys.MessageImageURL);
                        }
                    });
                }

            } catch (Exception e) {
                // Just abort and don't send the preview
                e.printStackTrace();
            }

        }

        return rig.run();
    }

    @Override
    public String textRepresentation(Message message) {
        return message.stringForKey(Keys.MessageFileURL);
    }

    Bitmap pdfPreview(File file) throws Exception {
        Context context = ChatSDK.shared().context();

        int pageNumber = 0;
        PdfiumCore pdfiumCore = new PdfiumCore(context);

        //http://www.programcreek.com/java-api-examples/index.php?api=android.os.ParcelFileDescriptor
//        ParcelFileDescriptor fd = context.getContentResolver().openFileDescriptor(pdfUri, "r");
//        context.getContentResolver().openFileDescr
//
//        PdfDocument pdfDocument = pdfiumCore.newDocument(fd);
        PdfDocument pdf = pdfiumCore.newDocument(fileToBytes(file));
        pdfiumCore.openPage(pdf, pageNumber);
        int width = pdfiumCore.getPageWidthPoint(pdf, pageNumber);
        int height = pdfiumCore.getPageHeightPoint(pdf, pageNumber);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        pdfiumCore.renderPageBitmap(pdf, bitmap, pageNumber, 0, 0, width, height);
        pdfiumCore.closeDocument(pdf); // important!

        return bitmap;
    }

    byte [] fileToBytes (File file) throws Exception {
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
        buf.read(bytes, 0, bytes.length);
        buf.close();
        return bytes;
    }
}
