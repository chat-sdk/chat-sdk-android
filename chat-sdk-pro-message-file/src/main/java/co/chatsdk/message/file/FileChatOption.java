package co.chatsdk.message.file;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.ActivityResultPushSubjectHolder;
import co.chatsdk.ui.activities.ChatActivity;
import co.chatsdk.ui.chat.options.BaseChatOption;
import io.reactivex.Completable;

import static android.app.Activity.RESULT_OK;
import static co.chatsdk.message.file.FileMessageModule.CHOOSE_FILE;

/**
 * Created by Pepe on 01/05/18.
 */

public class FileChatOption extends BaseChatOption {

    public FileChatOption(String title, Drawable iconDrawable) {
        super(title, iconDrawable, null);
        action = (activity, thread) -> Completable.create(emitter -> {
            if (activity instanceof ChatActivity) {
                final ChatActivity chatActivity = (ChatActivity) activity;

                dispose();

                // Listen for the context result which is when the sticker context finishes
                disposableList.add(ActivityResultPushSubjectHolder.shared().subscribe(activityResult -> {
                    // If the result is ok, connect the message send observable to the returned Observable
                    if (activityResult.requestCode == CHOOSE_FILE && activityResult.resultCode == RESULT_OK) {
                        // Get filePath and fileName
                        Uri fileUri = activityResult.data.getData();

                        if (fileUri != null) {
                            String mimeType = chatActivity.getContentResolver().getType(fileUri);

                            File file = fileFromURI(fileUri, activity, MediaStore.Files.FileColumns.DATA);

                            // Send the file
                            disposableList.add(ChatSDK.fileMessage().sendMessageWithFile(file.getName(), mimeType, file, thread)
                                    .subscribe(emitter::onComplete, emitter::onError));
                        }
                    } else {
                        emitter.onError(new Throwable(activity.getString(R.string.choose_a_file)));
                    }
                }));

                // Create and show the file chooser
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

                intent.setType("*/*" );

                final Intent chooser = Intent.createChooser(intent, activity.getString(R.string.choose_a_file));

                chatActivity.startActivityForResult(chooser, CHOOSE_FILE);
            }
        });
    }

    protected File fileFromURI (Uri uri, Activity activity, String name) throws IOException, FileNotFoundException {

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

    public FileChatOption(String title) {
        this(title, null);
    }

}
