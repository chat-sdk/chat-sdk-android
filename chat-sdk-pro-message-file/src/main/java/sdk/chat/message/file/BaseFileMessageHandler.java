package sdk.chat.message.file;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import sdk.chat.core.dao.Keys;
import sdk.chat.core.dao.Message;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.handlers.FileMessageHandler;
import sdk.chat.core.rigs.BitmapUploadable;
import sdk.chat.core.rigs.FileUploadable;
import sdk.chat.core.rigs.MessageSendRig;
import sdk.chat.core.rigs.Uploadable;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.types.MessageType;

/**
 * Created by Pepe on 01/05/18.
 */

public class BaseFileMessageHandler implements FileMessageHandler {

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
        }).setUploadables(uploadables, (message, result) -> {

//            if(result.mimeType.equals(imageMimeType)) {
//                message.setValueForKey(result.url, Keys.MessageImageURL);
//            }
//            if(result.mimeType.equals(mimeType)) {
//                message.setValueForKey(result.url, Keys.MessageFileURL);
//            }

        });
        return rig.run();
    }

    @Override
    public String textRepresentation(Message message) {
        return message.stringForKey(Keys.MessageFileURL);
    }

    @Override
    public String toString(Message message) {
        return ChatSDK.getString(R.string.file_message);
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

    @Override
    public String getImageURL(Message message) {
        if (message.getMessageType().is(MessageType.File) || message.getReplyType().is(MessageType.File)) {
            String imageURL = message.getImageURL();
            if (imageURL != null && !imageURL.isEmpty()) {
                return imageURL;
            } else {
                Context context = ChatSDK.ctx();
                Resources resources = context.getResources();

                final String mimeType = message.stringForKey(Keys.MessageMimeType);
                String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);

                int resID = context.getResources().getIdentifier("file_type_" + extension, "drawable", context.getPackageName());
                resID = resID > 0 ? resID : R.drawable.file;

                Uri uri = new Uri.Builder()
                        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                        .authority(resources.getResourcePackageName(resID))
                        .appendPath(resources.getResourceTypeName(resID))
                        .appendPath(resources.getResourceEntryName(resID))
                        .build();

                return uri.toString();
            }
        }
        return null;
    }

}
