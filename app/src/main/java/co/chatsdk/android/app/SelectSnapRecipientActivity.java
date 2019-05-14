package co.chatsdk.android.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import java.io.File;
import java.util.List;

import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.Message;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.rigs.MessageSendRig;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.FileUploadResult;
import co.chatsdk.core.types.MessageSendProgress;
import co.chatsdk.core.types.MessageSendStatus;
import co.chatsdk.core.types.MessageType;
import co.chatsdk.ui.contacts.SelectContactActivity;
import co.chatsdk.ui.utils.ToastHelper;
import id.zelory.compressor.Compressor;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;

public class SelectSnapRecipientActivity extends SelectContactActivity {

    private String theImagePath;
    protected Class snapChatActivity = co.chatsdk.android.app.SnapChatActivity.class;
    private Integer viewingTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        theImagePath = (String) i.getSerializableExtra("theImagePath");
        viewingTime = (Integer) i.getSerializableExtra("viewing_time");
        setMultiSelectEnabled(false);
    }

    @Override
    protected void doneButtonPressed(List<User> users) {
        disposableList.add(ChatSDK.thread().createThread("", users, 10)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(thread -> {
                    disposableList.add(new MessageSendRig(new MessageType(MessageType.Image), thread, message -> {
                        // Get the image and set the image message dimensions
                        final Bitmap image = BitmapFactory.decodeFile(theImagePath, null);

                        message.setValueForKey(image.getWidth(), Keys.MessageImageWidth);
                        message.setValueForKey(image.getHeight(), Keys.MessageImageHeight);

                    }).setFile(new File(theImagePath), "image.jpg", "image/jpeg", (message, result) -> {
                        // When the file has uploaded, set the image URL
                        message.setValueForKey(result.url, Keys.MessageImageURL);
                        message.setValueForKey(viewingTime, "message-lifetime");
                        message.setText("");
                    }).setFileCompressAction(file -> {
                        return new Compressor(ChatSDK.shared().context())
                                .setMaxHeight(ChatSDK.config().imageMaxHeight)
                                .setMaxWidth(ChatSDK.config().imageMaxWidth)
                                .compressToFile(file);
                    }).run().subscribe(() -> {
                                //
                            }, throwable -> {
                                //
                            }));

                    if (thread != null) {
                        this.startChatActivityForID(getApplicationContext(), thread.getEntityID());
                    }
                }, toastOnErrorConsumer()));
    }




    public void startChatActivityForID(Context context, String threadEntityID) {
        Intent intent = new Intent(context, getSnapChatActivity());
        intent.putExtra(Keys.IntentKeyThreadEntityID, threadEntityID);
        startActivity(intent);
    }
//This step is a little unnecessary, but it mirrors the rest of the framework so when it comes time
// to move it to the framework it can be easily done.
    public Class getSnapChatActivity () {
        return snapChatActivity;
    }

}
