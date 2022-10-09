package sdk.chat.message.file;

import static android.app.Activity.RESULT_OK;
import static sdk.chat.message.file.FileMessageModule.CHOOSE_FILE;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.reactivex.Completable;
import sdk.chat.core.dao.Thread;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.utils.ActivityResultPushSubjectHolder;
import sdk.chat.ui.chat.options.BaseChatOption;

/**
 * Created by Pepe on 01/05/18.
 */

public class FileChatOption extends BaseChatOption {

    public boolean filePicked;

    public FileChatOption(@StringRes int title, @DrawableRes int image) {
        super(title, image, null);
        action = (activity, launcher, thread) -> Completable.create(emitter -> {
            dispose();
           dm.add(selectFileWithDefaultPicker(activity, thread).subscribe(emitter::onComplete, emitter::onError));
        });
    }

    protected Completable selectFileWithDefaultPicker(Activity activity, Thread thread) {
        return Completable.create(emitter -> {

            filePicked = false;

            // Listen for the context result which is when the sticker context finishes
            dm.add(ActivityResultPushSubjectHolder.shared().subscribe(activityResult -> {

                // Stop the possibility to pick two files
                if (filePicked) {
                    return;
                }
                filePicked = true;

                // If the result is ok, connect the message send observable to the returned Observable
                if (activityResult.requestCode == CHOOSE_FILE && activityResult.resultCode == RESULT_OK) {
                    // Get filePath and fileName
                    Uri fileUri = activityResult.data.getData();

                    if (fileUri != null) {
                        String mimeType = activity.getContentResolver().getType(fileUri);

                        File file = fileFromURI(fileUri, activity, MediaStore.Files.FileColumns.DATA);

                        String name = getFileName(fileUri);

                        // Send the file
                        dm.add(ChatSDK.fileMessage().sendMessageWithFile(name, mimeType, file, thread)
                                .subscribe(emitter::onComplete, emitter::onError));
                    }
                }
            }));

            // Create and show the file chooser
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);

            intent.setType("*/*" );

            final Intent chooser = Intent.createChooser(intent, activity.getString(R.string.choose_a_file));

            ChatSDK.core().addBackgroundDisconnectExemption();
            activity.startActivityForResult(chooser, CHOOSE_FILE);
        });
    }

    protected static String getFileName(Uri uri) {
        // Get the Uri of the selected file
        String uriString = uri.toString();
        File myFile = new File(uriString);
        String path = myFile.getAbsolutePath();
        String displayName = null;

        if (uriString.startsWith("content://")) {
            Cursor cursor = null;
            try {
                cursor = ChatSDK.ctx().getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        displayName = cursor.getString(index);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else if (uriString.startsWith("file://")) {
            displayName = myFile.getName();
        }
        return displayName;
    }


    protected File fileFromURI (Uri uri, Activity activity, String name) throws IOException {

        InputStream input = activity.getContentResolver().openInputStream(uri);

        File file = new File(activity.getCacheDir(), name);
        OutputStream output = new FileOutputStream(file);

        byte[] buffer = new byte[4 * 1024]; // or other buffer size
        int read;

        while ((read = input.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }

        output.flush();
        output.close();

        input.close();

        return file;
    }

}
