package com.braunster.chatsdk.Utils.helper;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Utils;
import com.braunster.chatsdk.Utils.sorter.MessageSorter;
import com.braunster.chatsdk.activities.ChatSDKLocationActivity;
import com.braunster.chatsdk.adapter.ChatSDKMessagesListAdapter;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.braunster.chatsdk.interfaces.RepetitiveCompletionListenerWithMainTaskAndError;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.object.BError;
import com.braunster.chatsdk.object.ChatSDKThreadPool;
import com.braunster.chatsdk.object.Cropper;
import com.braunster.chatsdk.view.ChatMessageBoxView;
import com.github.johnpersano.supertoasts.SuperCardToast;
import com.google.android.gms.maps.model.LatLng;
import com.soundcloud.android.crop.Crop;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Created by braunster on 20/09/14.
 */
public class ChatSDKChatHelper implements ChatMessageBoxView.MessageBoxOptionsListener, ChatMessageBoxView.MessageSendListener{

    public static final int ERROR = 1991, NOT_HANDLED = 1992, HANDELD = 1993;

    /** The key to get the shared file uri. This is used when the activity is opened to share and image or a file with the chat users.
     *  Example can be found in ContactsFragment that use click mode share with contact. */
    public static final String SHARED_FILE_URI = "share_file_uri";

    /** The key to get shared text, this is used when the activity is open to share text with the chat user.
     *  Example can be found in ContactsFragment that use click mode share with contact. */
    public static final String SHARED_TEXT = "shared_text";

    /** The key to get the shared file path. This is used when the activity is opened to share and image or a file with the chat users.
     */
    public static final String SHARED_FILE_PATH = "shared_file_path";

    public static final String SHARE_LOCATION = "share_location";
    public static final String LAT = "lat", LNG = "lng";

    public static final String READ_COUNT = "read_count";

    /** The key to get the path of the last captured image path in case the activity is destroyed while capturing.*/
    public static final String SELECTED_FILE_PATH = "captured_photo_path";

    private Cropper crop;
    
    protected static final int PHOTO_PICKER_ID = 100;
    protected static final int CAPTURE_IMAGE = 101;
    public static final int PICK_LOCATION = 102;

    public static final int ADD_USERS = 103;

    /** to keep track if the share asked for was for filled so we wont try sharing again.*/
    private static final String SHARED = "shared";
    private boolean shared = false;

    /** The amount of messages that was loaded for this thread,
     *  When we load more then the default messages amount we want to keep the amount so we could load them again if the list needs to be re-created.*/
    public static final String LOADED_MESSAGES_AMOUNT = "LoadedMessagesAmount";
    private int loadedMessagesAmount = 0;

    /** The selected file that is picked to be sent.
     *  This is also the path to the camera output.*/
    private String selectedFilePath = "";

    private String mediaType = "";

    private double lat = 0, lng = 0;

    /** Keeping track of the amount of messages that was read in this thread.*/
    private int readCount = 0;

    private static final String TAG = ChatSDKChatHelper.class.getSimpleName();
    private static final boolean DEBUG = true;

    private Activity activity;
    private BThread thread;
    private ChatSDKUiHelper uiHelper;
    private ListView listMessages;
    private ChatMessageBoxView messageBoxView;
    private ProgressBar progressBar;
    private ChatSDKMessagesListAdapter messagesListAdapter;



    public ChatSDKChatHelper(Activity activity, BThread thread, ChatSDKUiHelper uiHelper) {
        this.activity = activity;
        this.thread = thread;
        this.uiHelper = uiHelper;
    }

    /*Message Sending*/
    /** Send text message logic.*/
    public  void sendTextMessageWithStatus(){
        sendTextMessageWithStatus(messageBoxView.getMessageText(), true);
    }

    /** Send text message
     * FIXME the messages does not added to the row anymore because we are getting the date from firebase server. Need to find a different way, Maybe new item mode for the row that wont have any date.
     * @param text the text to send.
     * @param clearEditText if true clear the message edit text.*/
    public  void sendTextMessageWithStatus(String text, boolean clearEditText){
        if (DEBUG) Log.v(TAG, "sendTextMessage, Text: " + text + ", Clear: " + String.valueOf(clearEditText));

        if (StringUtils.isEmpty(text) || StringUtils.isBlank(text))
        {
            if (!uiHelper.getAlertToast().isShowing()) {
                uiHelper.getAlertToast().setText("Cant send empty message!");
                uiHelper.getAlertToast().show();
            }
            return;
        }

        // Clear all white space from message
        text = text.trim();

        BNetworkManager.sharedManager().getNetworkAdapter().sendMessageWithText(text, thread.getId(),new RepetitiveCompletionListenerWithMainTaskAndError<BMessage, BMessage, BError>() {
            @Override
            public boolean onMainFinised(BMessage message, BError error) {
                if (DEBUG) Log.v(TAG, "onMainFinished, Status: " + message.getStatusOrNull());

                if (messagesListAdapter== null)
                    return false;

                return false;
            }

            @Override
            public boolean onItem(BMessage message) {
                if (DEBUG) Log.v(TAG, "onItem, Status: " + message.getStatusOrNull());

                if (messagesListAdapter== null)
                    return false;

                return false;
            }

            @Override
            public void onDone() {
            }


            @Override
            public void onItemError(BMessage message, BError error) {
                uiHelper.showAlertToast("Error while sending message.");
                /*messagesListAdapter.addRow(message);*/
                /*FIXME todo handle error by showing indicator on the message in the list.*/
            }
        });

        if (clearEditText && messageBoxView!=null)
            messageBoxView.clearText();
    }

    /** Send an image message.
     * @param filePath the path to the image file that need to be sent.*/
    public  void sendImageMessage(String filePath){
        if (DEBUG) Log.v(TAG, "sendImageMessage, Path: " + filePath);
        BNetworkManager.sharedManager().getNetworkAdapter().sendMessageWithImage(
                filePath, thread.getId(), new CompletionListenerWithData<BMessage>() {
                    @Override
                    public void onDone(BMessage message) {
                        if (DEBUG) Log.v(TAG, "Image is sent");
                        uiHelper.dismissProgressCardWithSmallDelay();
                    }

                    @Override
                    public void onDoneWithError(BError error) {
                        uiHelper.dismissProgressCardWithSmallDelay();
                        uiHelper.showAlertToast("Image could not been sent. " + error.message);
                    }
                });
    }

    /** Load messages from the database and saving the current position of the list.*/
    public void loadMessagesAndRetainCurrentPos(){
        loadMessages(true, false, 0, 0);
    }

    public void loadMessages(final boolean retain, final boolean hideListView, final int offsetOrPos, final int amountToLoad){

        if (messagesListAdapter == null || listMessages == null || progressBar == null || activity == null)
            return;

        if (thread == null)
        {
            Log.e(TAG, "Thread is null");
            return;
        }

        if (hideListView)
        {
            listMessages.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }
        else progressBar.setVisibility(View.INVISIBLE);

        ChatSDKThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {

                final int oldDataSize = messagesListAdapter.getCount();

                List<BMessage> messages;
                // Loading messages
                // Load with fixed limit
                if (amountToLoad > 0)
                    messages = BNetworkManager.sharedManager().getNetworkAdapter().getMessagesForThreadForEntityID(thread.getId(), amountToLoad);
                // we allread loaded messages so we load more then the default limit.
                else if (messagesListAdapter.getCount() > BDefines.MAX_MESSAGES_TO_PULL + 1)
                    messages = BNetworkManager.sharedManager().getNetworkAdapter().getMessagesForThreadForEntityID(thread.getId(), messagesListAdapter.getCount());
                //This value is saved in the savedInstanceState so we could check if there was more loaded messages then normal before.
                else if (loadedMessagesAmount > BDefines.MAX_MESSAGES_TO_PULL + 1)
                    messages = BNetworkManager.sharedManager().getNetworkAdapter().getMessagesForThreadForEntityID(thread.getId(), loadedMessagesAmount);
                //Loading with default limit.
                else
                    messages = BNetworkManager.sharedManager().getNetworkAdapter().getMessagesForThreadForEntityID(thread.getId());

                // Sorting the message by date to make sure the list looks ok.
                Collections.sort(messages, new MessageSorter(MessageSorter.ORDER_TYPE_DESC));

                loadedMessagesAmount = messages.size();

                markAsRead(messages);

                // Setting the new message to the adapter.
                final List<ChatSDKMessagesListAdapter.MessageListItem> list = messagesListAdapter.makeList(messages);

                if (list.size() == 0)
                {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.INVISIBLE);
                            listMessages.setVisibility(View.VISIBLE);
                        }
                    });
                    return;
                }

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        messagesListAdapter.setListData(list);

                        // restoring the old position after the load is done.
                        if (retain)
                        {
                            int newDataSize = messagesListAdapter.getCount();
                            final int index = listMessages.getFirstVisiblePosition() + newDataSize - oldDataSize + offsetOrPos;
                            View v = listMessages.getChildAt(0);
                            final int top = (v == null) ? -1 : v.getTop();

                            listMessages.post(new Runnable() {
                                @Override
                                public void run() {
                                    listMessages.setSelectionFromTop(index, top);

                                    if (listMessages.getVisibility() == View.INVISIBLE)
                                        animateListView();
                                }
                            });
                        }
                        // If list view is visible smooth scroll else dirty.
                        else scrollListTo(offsetOrPos,  !hideListView);
                    }
                });
            }
        });


    }

    public void markAsRead(List<BMessage> messages){
        for (BMessage m : messages)
        {
            m.setIsRead(true);
            DaoCore.updateEntity(m);
            readCount++;
        }
    }

    public void markAsRead(BMessage message){
        message.setIsRead(true);
        DaoCore.updateEntity(message);
        readCount++;
    }

    public void loadMessagesAndScrollBottom(){
        loadMessages(false, true, - 1, 0);
    }

    public void loadMessages(int scrollingPos){
        loadMessages(false, true, scrollingPos, 0);
    }

    public void scrollListTo(final int pos, final boolean smooth) {

        if (listMessages == null)
            return;

        listMessages.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                int position = pos;
                if (pos == -1)
                    position = messagesListAdapter.getCount()-1;

                if (smooth)
                    listMessages.smoothScrollToPosition(position);
                else
                    listMessages.setSelection(position);

                if (listMessages.getVisibility() == View.INVISIBLE)
                    animateListView();
            }
        });
    }

    public void animateListView(){

        if (listMessages == null)
            return;

        if (DEBUG) Log.v(TAG, "animateListView");

        listMessages.setAnimation(AnimationUtils.loadAnimation(activity, R.anim.fade_in_expand));
        listMessages.getAnimation().setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (DEBUG) Log.v(TAG, "onAnimationStart");

                if (progressBar!= null)
                    progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (DEBUG) Log.v(TAG, "onAnimationEnd");

                listMessages.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        listMessages.getAnimation().start();
    }

    public int handleResult(int requestCode, int resultCode, Intent data) {
        return handleResult(true, requestCode, resultCode, data);
    }

    public int handleResult(boolean send, int requestCode, int resultCode, Intent data) {
        if (DEBUG) Log.v(TAG, "onActivityResult");

        if (requestCode != CAPTURE_IMAGE && requestCode != ADD_USERS && data == null)
        {
            if (DEBUG) Log.e(TAG, "onActivityResult, Intent is null");
            return NOT_HANDLED;
        }

        // Just to be sure its init.
        uiHelper.initCardToast();

        try {
            if (send && (requestCode == PHOTO_PICKER_ID || requestCode == PICK_LOCATION || requestCode == CAPTURE_IMAGE) && resultCode == Activity.RESULT_OK) {
                uiHelper.showProgressCard("Sending...");
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }

        /* Pick photo logic*/
        if (requestCode == PHOTO_PICKER_ID)
        {
            // Reset
            lat = 0; lng = 0;

            switch (resultCode)
            {
                case Activity.RESULT_OK:

                    if (DEBUG) Log.d(TAG, "Result OK");
                    Uri uri = data.getData();

                    Uri outputUri = Uri.fromFile(new File(this.activity.getCacheDir(), "cropped.jpg"));
                    crop = new Cropper(uri);

                    Intent cropIntent = crop.getAdjustIntent(this.activity, outputUri);
                    int request = Crop.REQUEST_CROP + PHOTO_PICKER_ID;

                    activity.startActivityForResult(cropIntent, request);
                    
                    return HANDELD;
                    
                    /*if (DEBUG) Log.d(TAG, "Result OK");
                    Uri uri = data.getData();
                    File image = null;
                    try
                    {
                        image = Utils.getFile(activity, uri);
                    }
                    catch (NullPointerException e){
                        if (DEBUG) Log.e(TAG, "Null pointer when getting file.");
                        uiHelper.showAlertToast("Unable to fetch image");
                        uiHelper.dismissProgressCardWithSmallDelay();
                        return ERROR;
                    }

                    if (image != null) {
                        if (DEBUG) Log.i(TAG, "Image is not null");
                        if (send)
                            sendImageMessage(image.getPath());
                        else
                        {
                            selectedFilePath = image.getPath();
                        }
                        return HANDELD;
                    }
                    else {
                        if (DEBUG) Log.e(TAG, "Image is null");
                        uiHelper.dismissProgressCardWithSmallDelay();
                        uiHelper.showAlertToast("Error when loading the image.");
                        return ERROR;
                    }*/

                case Activity.RESULT_CANCELED:
                    if (DEBUG) Log.d(TAG, "Result Canceled");
                    return HANDELD;
            }
        }
        else  if (requestCode == Crop.REQUEST_CROP + PHOTO_PICKER_ID) {
            if (resultCode == Crop.RESULT_ERROR)
            {
                if (DEBUG) Log.e(TAG, "Result Error");
                return ERROR;
            }

            try
            {
                File image;
                Uri uri = Crop.getOutput(data);

                if (DEBUG) Log.d(TAG, "Fetch image URI: " + uri.toString());
                image = new File(this.activity.getCacheDir(), "cropped.jpg");

                selectedFilePath = image.getPath();

                sendImageMessage(image.getPath());
                
                return HANDELD;
            }
            catch (NullPointerException e){
                if (DEBUG) Log.e(TAG, "Null pointer when getting file.");
                uiHelper.showAlertToast("Unable to fetch image");
                return ERROR;
            }
        }

        /* Pick location logic*/
        else if (requestCode == PICK_LOCATION)
        {
            // Reset
            lat = 0; lng = 0;

            if (resultCode == Activity.RESULT_CANCELED) {
                if (DEBUG) Log.d(TAG, "Result Cancelled");
                if (data.getExtras() == null)
                    return ERROR;

                if (data.getExtras().containsKey(ChatSDKLocationActivity.ERROR))
                    uiHelper.showAlertToast(data.getExtras().getString(ChatSDKLocationActivity.ERROR));

                return ERROR;
            }
            else if (resultCode == Activity.RESULT_OK) {
                if (DEBUG) Log.d(TAG, "Result OK");
                if (DEBUG)
                    Log.d(TAG, "Zoom level: " + data.getFloatExtra(ChatSDKLocationActivity.ZOOM, 0.0f));
                // Send the message, Params Latitude, Longitude, Base64 Representation of the image of the location, threadId.
                if (send)
                {
                    BNetworkManager.sharedManager().getNetworkAdapter().sendMessageWithLocation(data.getExtras().getString(ChatSDKLocationActivity.SNAP_SHOT_PATH, null),
                            new LatLng(data.getDoubleExtra(ChatSDKLocationActivity.LANITUDE, 0), data.getDoubleExtra(ChatSDKLocationActivity.LONGITUDE, 0)),
                            thread.getId(), new CompletionListenerWithData<BMessage>() {
                                @Override
                                public void onDone(BMessage bMessage) {
                                    if (DEBUG) Log.v(TAG, "Image is sent");

                                    uiHelper.dismissProgressCardWithSmallDelay();
                                }

                                @Override
                                public void onDoneWithError(BError error) {
                                    uiHelper.dismissProgressCardWithSmallDelay();
                                    uiHelper.showAlertToast("Location could not been sent.");
                                }
                            });
                }
                else
                {
                    lat = data.getDoubleExtra(ChatSDKLocationActivity.LANITUDE, 0);
                    lng = data.getDoubleExtra(ChatSDKLocationActivity.LONGITUDE, 0);
                    selectedFilePath = data.getExtras().getString(ChatSDKLocationActivity.SNAP_SHOT_PATH, null);
                }

                return HANDELD;
            }
        }
        /* Capture image logic*/
        else if (requestCode == CAPTURE_IMAGE)
        {
            // Reset
            lat = 0; lng = 0;

            if (DEBUG) Log.d(TAG, "Capture image return");
            if (resultCode == Activity.RESULT_OK) {
                if (DEBUG) Log.d(TAG, "Result OK");

                if (send)
                    sendImageMessage(selectedFilePath);

                return HANDELD;
            }
        }

        return NOT_HANDLED;
    }

    public void onSavedInstanceBundle(Bundle outState){
        if (StringUtils.isNotEmpty(selectedFilePath))
        {
            outState.putString(SELECTED_FILE_PATH, selectedFilePath);

            if (lng != 0)
            {
                outState.putDouble(LNG, lng);
                outState.putDouble(LAT, lat);
            }
        }

        outState.putInt(LOADED_MESSAGES_AMOUNT, loadedMessagesAmount);

        outState.putBoolean(SHARED, shared);

        SuperCardToast.onSaveState(outState);

        outState.putInt(READ_COUNT, readCount);
    }
    public void restoreSavedInstance(Bundle savedInstanceState){
        if (savedInstanceState == null)
            return;

        selectedFilePath = savedInstanceState.getString(SELECTED_FILE_PATH);
        savedInstanceState.remove(SELECTED_FILE_PATH);

        lng = savedInstanceState.getDouble(LNG, 0);
        lat = savedInstanceState.getDouble(LAT, 0);

        shared = savedInstanceState.getBoolean(SHARED);

        loadedMessagesAmount = savedInstanceState.getInt(LOADED_MESSAGES_AMOUNT, 0);

        readCount = savedInstanceState.getInt(READ_COUNT);
        savedInstanceState.remove(LNG);
        savedInstanceState.remove(LAT);

        SuperCardToast.onRestoreState(savedInstanceState, activity);
    }

    @Override
    public void onSendPressed(String text) {
        sendTextMessageWithStatus();
    }

    @Override
    public void onLocationPressed() {
        Intent intent = new Intent(activity, uiHelper.shareLocationActivity);
        activity.startActivityForResult(intent, PICK_LOCATION);
    }

    @Override
    public void onTakePhotoPressed() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File file, dir = Utils.ImageSaver.getAlbumStorageDir(Utils.ImageSaver.IMAGE_DIR_NAME);
        if(dir.exists())
        {
            file = new File(dir, DaoCore.generateEntity() + ".jpg");
            selectedFilePath = file.getPath();
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        }

        // start the image capture Intent
        activity.startActivityForResult(intent, CAPTURE_IMAGE);
    }

    @Override
    public void onPickImagePressed() {
        // TODO allow multiple pick of photos.
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
//                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        activity.startActivityForResult(Intent.createChooser(intent,
                "Complete action using"), PHOTO_PICKER_ID);
    }

    @Override
    public boolean onOptionButtonPressed() {
        return false;
    }

    public void integrateUI(ChatMessageBoxView messageBoxView, ChatSDKMessagesListAdapter messagesListAdapter, ListView listView, ProgressBar progressBar) {
        integrateUI(true, messageBoxView, messagesListAdapter, listView, progressBar);
    }

    public void integrateUI(boolean autoSend, ChatMessageBoxView messageBoxView, ChatSDKMessagesListAdapter messagesListAdapter, ListView listView, ProgressBar progressBar) {
        this.listMessages = listView;
        this.progressBar = progressBar;
        this.messagesListAdapter = messagesListAdapter;
        this.messageBoxView = messageBoxView;

        if (autoSend)
            messageBoxView.setMessageSendListener(this);

        messageBoxView.setMessageBoxOptionsListener(this);
    }

    public void setThread(BThread thread) {
        this.thread = thread;
    }

    /** Check the intent if carries some data that received from another app to share on this chat.*/
    public void checkIfWantToShare(Intent intent){
        if (DEBUG) Log.v(TAG, "checkIfWantToShare");

        if (shared)
            return;

        if (intent.getExtras() == null || intent.getExtras().isEmpty())
        {
            if (DEBUG) Log.e(TAG, "Extras is null or empty");
            return;
        }

        if (intent.getExtras().containsKey(SHARED_FILE_URI))
        {
            if (DEBUG) Log.i(TAG, "Want to share URI");

            uiHelper.showProgressCard("Sending...");

            String path = Utils.getRealPathFromURI(activity, (Uri) intent.getExtras().get(SHARED_FILE_URI));
            if (DEBUG) Log.d(TAG, "Path from uri: " + path);

            // removing the key so we wont send again,
            intent.getExtras().remove(SHARED_FILE_URI);

            sendImageMessage(path);

            intent.removeExtra(SHARED_FILE_URI);

            shared = true;
        }
        else if (intent.getExtras().containsKey(SHARED_TEXT))
        {
            if (DEBUG) Log.i(TAG, "Want to share Text");

            String text =intent.getExtras().getString(SHARED_TEXT);

            // removing the key so we wont send again,
            intent.getExtras().remove(SHARED_TEXT);

            sendTextMessageWithStatus(text, false);

            intent.removeExtra(SHARED_TEXT);

            shared = true;
        }
        else if (intent.getExtras().containsKey(SHARED_FILE_PATH))
        {
            if (DEBUG) Log.i(TAG, "Want to share File from path");
            uiHelper.showProgressCard("Sending...");

            String path =intent.getStringExtra(SHARED_FILE_PATH);
            if (DEBUG) Log.d(TAG, "Path: " + path);

            // removing the key so we wont send again,
            intent.getExtras().remove(SHARED_FILE_PATH);

            sendImageMessage(path);

            intent.removeExtra(SHARED_FILE_PATH);

            shared = true;
        }
        else if (intent.getExtras().containsKey(SHARE_LOCATION)){
            if (DEBUG) Log.i(TAG, "Want to share Location");
            uiHelper.showProgressCard("Sending...");

            BNetworkManager.sharedManager().getNetworkAdapter().sendMessageWithLocation(intent.getExtras().getString(SHARE_LOCATION, null),
                    new LatLng(intent.getDoubleExtra(LAT, 0), intent.getDoubleExtra(LNG, 0)),
                    thread.getId(), new CompletionListenerWithData<BMessage>() {
                        @Override
                        public void onDone(BMessage bMessage) {
                            if (DEBUG) Log.v(TAG, "Image is sent");

                            uiHelper.dismissProgressCardWithSmallDelay();
                        }

                        @Override
                        public void onDoneWithError(BError error) {
                            uiHelper.dismissProgressCardWithSmallDelay();
                            uiHelper.showAlertToast("Location could not been sent.");
                        }
                    });

            intent.removeExtra(SHARE_LOCATION);

            shared = true;
        }
    }

    public String getSelectedFilePath() {
        return selectedFilePath;
    }

    public boolean isLoactionMedia(){
        return StringUtils.isNotEmpty(getSelectedFilePath()) && StringUtils.isNotBlank(getSelectedFilePath()) && lat != 0 && lng != 0;
    }

    public double getLng() {
        return lng;
    }

    public double getLat() {
        return lat;
    }

    public void setListMessages(ListView listMessages) {
        this.listMessages = listMessages;
    }

    public void setMessageBoxView(ChatMessageBoxView messageBoxView) {
        this.messageBoxView = messageBoxView;
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public void setMessagesListAdapter(ChatSDKMessagesListAdapter messagesListAdapter) {
        this.messagesListAdapter = messagesListAdapter;
    }

    public int getReadCount() {
        return readCount;
    }
}
