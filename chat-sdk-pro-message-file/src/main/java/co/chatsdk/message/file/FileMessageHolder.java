package co.chatsdk.message.file;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.webkit.MimeTypeMap;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.DownloadListener;
import com.stfalcon.chatkit.commons.models.MessageContentType;

import org.pmw.tinylog.Logger;

import java.io.File;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.PermissionRequestHandler;
import co.chatsdk.ui.chat.model.MessageHolder;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;

public class FileMessageHolder extends MessageHolder implements MessageContentType {

    public FileMessageHolder(Message message) {
        super(message);
    }

    public String getIcon() {
        String imageURL = message.imageURL();
        if (imageURL != null && !imageURL.isEmpty()) {
            return imageURL;
        } else {
            Context context = ChatSDK.shared().context();
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

//    public Single<String> downloadFile (String url, String name) {
//        return getDownloadDirectory().flatMap((Function<File, SingleSource<String>>) directory -> Single.create(emitter -> {
//            File file = new File(directory.getPath(), name);
//            if (file.exists() && file.length() > 0) {
//                emitter.onSuccess(file.getPath());
//            } else {
//                AndroidNetworking.download(url, directory.getPath(), name)
//                        .build()
//                        .setDownloadProgressListener((bytesDownloaded, totalBytes) -> {
//                            // Progress
//                            Logger.debug("Progress: " + bytesDownloaded);
//                        }).startDownload(new DownloadListener() {
//                    @Override
//                    public void onDownloadComplete() {
//                        emitter.onSuccess(file.getPath());
//                    }
//
//                    @Override
//                    public void onError(ANError anError) {
//                        emitter.onError(anError);
//                    }
//                });
//            }
//        }));
//    }

//    public Single<File> getDownloadDirectory () {
//        return PermissionRequestHandler.requestWriteExternalStorage().andThen(Single.create(emitter -> {
//            File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "ChatSDK");
//            if (!f.exists()) {
//                if (f.mkdirs()) {
//                    emitter.onSuccess(f);
//                } else {
//                    emitter.onError(new Throwable("Unable to create directory"));
//                }
//            } else {
//                emitter.onSuccess(f);
//            }
//        }));
//    }

}
