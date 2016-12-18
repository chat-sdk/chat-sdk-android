/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.Utils.helper;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.ImageUtils;
import com.braunster.chatsdk.Utils.Utils;
import com.braunster.chatsdk.Utils.sorter.MessageSorter;
import com.braunster.chatsdk.activities.ChatSDKLocationActivity;
import com.braunster.chatsdk.adapter.ChatSDKMessagesListAdapter;
import com.braunster.chatsdk.dao.BMessage;
import com.braunster.chatsdk.dao.BMessageDao;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.object.BError;
import com.braunster.chatsdk.object.ChatSDKThreadPool;
import com.braunster.chatsdk.object.Cropper;
import com.braunster.chatsdk.view.ChatMessageBoxView;
import com.github.johnpersano.supertoasts.SuperCardToast;
import com.google.android.gms.maps.model.LatLng;
import com.soundcloud.android.crop.BuildConfig;
import com.soundcloud.android.crop.Crop;

import org.apache.commons.lang3.StringUtils;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.ProgressCallback;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;
import timber.log.Timber;

public class ChatSDKChatHelper implements ChatMessageBoxView.MessageBoxOptionsListener, ChatMessageBoxView.MessageSendListener{

    public static final int ERROR = 1991, NOT_HANDLED = 1992, HANDLED = 1993;

    /** The key to get the shared file uri. This is used when the activity is opened to share and image or a file with the chat users.
     *  Example can be found in ContactsFragment that use click mode share with contact. */
    public static final String SHARED_FILE_URI = "share_file_uri";

    /** The key to get shared text, this is used when the activity is open to share text with the chat user.
     *  Example can be found in ContactsFragment that use click mode share with contact. */
    public static final String SHARED_TEXT = "shared_text";
    public static final String LAT = "lat", LNG = "lng";


    /** The key to get the shared file path. This is used when the activity is opened to share and image or a file with the chat users.
     */
    public static final String SHARED_FILE_PATH = "shared_file_path";

    public static final String SHARE_LOCATION = "share_location";

    public static final String READ_COUNT = "read_count";

    public static final String FILE_NAME = "file_name";
    
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

    /**
     * The file name of the image that was picked and cropped
     **/
    private String mFileName;

    /** Keeping track of the amount of messages that was read in this thread.*/
    private int readCount = 0;

    private static final String TAG = ChatSDKChatHelper.class.getSimpleName();
    private static final boolean DEBUG = false;

    private WeakReference<Activity> activity;
    private BThread thread;
    private ChatSDKUiHelper uiHelper;
    private ListView listMessages;
    private ChatMessageBoxView messageBoxView;
    private ProgressBar progressBar;
    private ChatSDKMessagesListAdapter messagesListAdapter;

    public ChatSDKChatHelper(Activity activity, BThread thread, ChatSDKUiHelper uiHelper) {
        this.activity = new WeakReference<Activity>(activity);
        this.thread = thread;
        this.uiHelper = uiHelper;
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

    /** Load messages from the database and saving the current position of the list.*/
    public void loadMessagesAndRetainCurrentPos(){
        loadMessages(true, false, 0, 0);
    }

    public void loadMessages(final boolean retain, final boolean hideListView, final int offsetOrPos, final int amountToLoad){

        if (messagesListAdapter == null || listMessages == null || progressBar == null || activity == null)
            return;

        if (thread == null)
        {
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

                if (!hasActivity())
                    return;
                
                final int oldDataSize = messagesListAdapter.getCount();

                List<BMessage> messages;
                // Loading messages
                // Load with fixed limit
                if (amountToLoad > 0)
                    messages = getMessagesForThreadForEntityID(thread.getId(), amountToLoad);
                // we allread loaded messages so we load more then the default limit.
                else if (messagesListAdapter.getCount() > BDefines.MAX_MESSAGES_TO_PULL + 1)
                    messages = getMessagesForThreadForEntityID(thread.getId(), messagesListAdapter.getCount());
                //This value is saved in the savedInstanceState so we could check if there was more loaded messages then normal before.
                else if (loadedMessagesAmount > BDefines.MAX_MESSAGES_TO_PULL + 1)
                    messages = getMessagesForThreadForEntityID(thread.getId(), loadedMessagesAmount);
                //Loading with default limit.
                else
                    messages = getMessagesForThreadForEntityID(thread.getId());

                // Sorting the message by date to make sure the list looks ok.
                Collections.sort(messages, new MessageSorter(MessageSorter.ORDER_TYPE_DESC));

                loadedMessagesAmount = messages.size();

                markAsRead(messages);

                // Setting the new message to the adapter.
                final List<ChatSDKMessagesListAdapter.MessageListItem> list = messagesListAdapter.makeList(messages);

                if (list.size() == 0)
                {
                    activity.get().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.INVISIBLE);
                            listMessages.setVisibility(View.VISIBLE);
                        }
                    });
                    return;
                }

                activity.get().runOnUiThread(new Runnable() {
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

    /**
     * Get all messages for given thread id ordered Ascending/Descending
     */
    public List<BMessage> getMessagesForThreadForEntityID(Long id) {
        return getMessagesForThreadForEntityID(id, BDefines.MAX_MESSAGES_TO_PULL);
    }

    /**
     * Get all messages for given thread id ordered Ascending/Descending
     */
    public List<BMessage> getMessagesForThreadForEntityID(Long id, int limit) {
        List<BMessage> list ;

        QueryBuilder<BMessage> qb = DaoCore.daoSession.queryBuilder(BMessage.class);
        qb.where(BMessageDao.Properties.ThreadDaoId.eq(id));

        // Making sure no null messages infected the sort.
        qb.where(BMessageDao.Properties.Date.isNotNull());
        qb.where(BMessageDao.Properties.Sender.isNotNull());

        qb.orderDesc(BMessageDao.Properties.Date);

        if (limit != -1)
            qb.limit(limit);

        list = qb.list();

        return list;
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
        if (!hasActivity())
            return;
        
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

        if (!hasActivity())
            return;
        
        if (listMessages == null)
            return;

        if (DEBUG) Timber.v("animateListView");

        listMessages.setAnimation(AnimationUtils.loadAnimation(activity.get(), R.anim.fade_in_expand));
        listMessages.getAnimation().setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (DEBUG) Timber.v("onAnimationStart");

                if (progressBar!= null)
                    progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (DEBUG) Timber.v("onAnimationEnd");

                listMessages.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        listMessages.getAnimation().start();
    }

    public void handleResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) Timber.v("onActivityResult");

        if (!hasActivity()) return;

        /* Pick photo logic*/
        if (requestCode == PHOTO_PICKER_ID)
        {
            processPickedPhoto(resultCode, data);
        }
        else  if (requestCode == Crop.REQUEST_CROP + PHOTO_PICKER_ID) {
            processCroppedPhoto(resultCode, data);
        }
        /* Pick location logic*/
        else if (requestCode == PICK_LOCATION)
        {
            processPickedLocation(resultCode, data);
        }
        /* Capture image logic*/
        else if (requestCode == CAPTURE_IMAGE)
        {
            if (resultCode == Activity.RESULT_OK) {
                sendImageMessage(selectedFilePath);
            }
        }
    }
    private void processCroppedPhoto(int resultCode, Intent data){
        if (resultCode == Crop.RESULT_ERROR)
        {
            uiHelper.dismissProgressCard();
            return;
        }

        try
        {
            // If enabled we will save the image to the app
            // directory in gallery else we will save it in the cache dir.
            File dir;
            if (BDefines.Options.SaveImagesToDir)
                dir = Utils.ImageSaver.getAlbumStorageDir(activity.get(), Utils.ImageSaver.IMAGE_DIR_NAME);
            else
                dir = this.activity.get().getCacheDir();

            if (dir == null)
            {
                uiHelper.dismissProgressCard();
                uiHelper.showAlertToast(R.string.unable_to_fetch_image);
                return;
            }

            File image = new File(dir, mFileName  + ".jpeg");

            selectedFilePath = image.getPath();

            // Scanning the image so it would be visible in the gallery images.
            if (BDefines.Options.SaveImagesToDir)
                ImageUtils.scanFilePathForGallery(activity.get(), selectedFilePath);

            sendImageMessage(image.getPath());
        }
        catch (NullPointerException e){
            uiHelper.showAlertToast(R.string.unable_to_fetch_image);
        }
    }
    private void processPickedPhoto(int resultCode, Intent data){

        switch (resultCode)
        {
            case Activity.RESULT_OK:

                Uri uri = data.getData();
                mFileName = DaoCore.generateEntity();

                // If enabled we will save the image to the app
                // directory in gallery else we will save it in the cache dir.
                File dir;
                if (BDefines.Options.SaveImagesToDir)
                    dir = Utils.ImageSaver.getAlbumStorageDir(activity.get(), Utils.ImageSaver.IMAGE_DIR_NAME);
                else
                    dir = this.activity.get().getCacheDir();

                if (dir == null)
                {
                    uiHelper.dismissProgressCard();
                    uiHelper.showAlertToast(R.string.unable_to_fetch_image);
                    return;
                }

                Uri outputUri = Uri.fromFile(new File(dir, mFileName  + ".jpeg"));

                crop = new Cropper(uri);

                Intent cropIntent = crop.getAdjustIntent(this.activity.get(), outputUri);
                int request = Crop.REQUEST_CROP + PHOTO_PICKER_ID;

                activity.get().startActivityForResult(cropIntent, request);

                return;

            case Activity.RESULT_CANCELED:
                uiHelper.dismissProgressCard();
        }
    }

    private void processPickedLocation(int resultCode, Intent data){
        if (resultCode == Activity.RESULT_CANCELED) {
            if (data.getExtras() == null)
                return;

            if (data.getExtras().containsKey(ChatSDKLocationActivity.ERROR))
                uiHelper.showAlertToast(data.getExtras().getString(ChatSDKLocationActivity.ERROR));
        }
        else if (resultCode == Activity.RESULT_OK) {
            if (DEBUG)
                Timber.d("Zoom level: %s", data.getFloatExtra(ChatSDKLocationActivity.ZOOM, 0.0f));
            // Send the message, Params Latitude, Longitude, Base64 Representation of the image of the location, threadId.

            sendLocationMessage(data);
        }
    }

    private void sendingMessageToast(){
        // Just to be sure it's initialized.
        uiHelper.initCardToast();
        uiHelper.showProgressCard("Sending...");
    }

    public void onSavedInstanceBundle(Bundle outState){
        if (StringUtils.isNotEmpty(selectedFilePath))
        {
            outState.putString(SELECTED_FILE_PATH, selectedFilePath);
        }

        outState.putInt(LOADED_MESSAGES_AMOUNT, loadedMessagesAmount);

        outState.putBoolean(SHARED, shared);

        SuperCardToast.onSaveState(outState);

        outState.putInt(READ_COUNT, readCount);
        
        outState.putString(FILE_NAME, mFileName);
    }
    
    public void restoreSavedInstance(Bundle savedInstanceState){
        if (savedInstanceState == null)
            return;
        
        if (!hasActivity())
            return;

        selectedFilePath = savedInstanceState.getString(SELECTED_FILE_PATH);
        savedInstanceState.remove(SELECTED_FILE_PATH);


        shared = savedInstanceState.getBoolean(SHARED);

        loadedMessagesAmount = savedInstanceState.getInt(LOADED_MESSAGES_AMOUNT, 0);

        readCount = savedInstanceState.getInt(READ_COUNT);

        mFileName = savedInstanceState.getString(FILE_NAME);
        SuperCardToast.onRestoreState(savedInstanceState, activity.get());
    }

    
    @Override
    public void onSendPressed(String text) {
        sendMessageWithText();
    }

    @Override
    public void onLocationPressed() {
        if (!hasActivity())
            return;
        
        Intent intent = new Intent(activity.get(), uiHelper.shareLocationActivity);
        activity.get().startActivityForResult(intent, PICK_LOCATION);
    }

    @Override
    public void onTakePhotoPressed() {
        
        if (!hasActivity())
            return;
        
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File file, dir = Utils.ImageSaver.getAlbumStorageDir(activity.get(), Utils.ImageSaver.IMAGE_DIR_NAME);

        if (dir == null)
        {
            uiHelper.dismissProgressCard();
            uiHelper.showAlertToast(R.string.unable_to_catch_image);
            return;
        }
        
        if(dir.exists())
        {
            file = new File(dir, DaoCore.generateEntity() + ".jpg");
            selectedFilePath = file.getPath();
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        }

        // start the image capture Intent
        activity.get().startActivityForResult(intent, CAPTURE_IMAGE);
    }

    @Override
    public void onPickImagePressed() {
        
        if (!hasActivity())
            return;

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        
        activity.get().startActivityForResult(Intent.createChooser(intent,
                "Complete action using"), PHOTO_PICKER_ID);
    }

    @Override
    public boolean onOptionButtonPressed() {
        return false;
    }


    /** Send text message logic.*/
    public  void sendMessageWithText(){
        sendMessageWithText(messageBoxView.getMessageText(), true);
    }

    /** Send text message
     * FIXME the messages does not added to the row anymore because we are getting the date from firebase server. Need to find a different way, Maybe new item mode for the row that wont have any date.
     * @param text the text to send.
     * @param clearEditText if true clear the message edit text.*/
    public  void sendMessageWithText(String text, boolean clearEditText){
        if (DEBUG) Timber.v("sendTextMessage, Text: %s, Clear: %s", text, String.valueOf(clearEditText));

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

        BNetworkManager.sharedManager().getNetworkAdapter().sendMessageWithText(text, thread.getId())
                .then(new DoneCallback<BMessage>() {
                    @Override
                    public void onDone(BMessage message) {
                    }
                }, new FailCallback<BError>() {
                    @Override
                    public void onFail(BError error) {
                        uiHelper.showAlertToast(R.string.unable_to_send_message);
                    }
                }, new ProgressCallback<BMessage>() {
                    @Override
                    public void onProgress(BMessage message) {
                        // Adding the message after it was prepared bt the NetworkAdapter.
                        if (messagesListAdapter != null)
                            messagesListAdapter.addRow(message);
                    }
                });

        if (clearEditText && messageBoxView!=null)
            messageBoxView.clearText();
    }

    /** Send an image message.
     * @param filePath the path to the image file that need to be sent.*/
    public  void sendImageMessage(final String filePath){
        if (DEBUG) Timber.v("sendImageMessage, Path: %s", filePath);
        sendingMessageToast();
        BNetworkManager.sharedManager().getNetworkAdapter().sendMessageWithImage(filePath, thread.getId())
                .then(new DoneCallback<BMessage>() {
                    @Override
                    public void onDone(BMessage message) {
                        if (DEBUG) Timber.v("Image is sent");
                        uiHelper.dismissProgressCardWithSmallDelay();
                    }
                }, new FailCallback<BError>() {
                    @Override
                    public void onFail(BError error) {
                        uiHelper.dismissProgressCardWithSmallDelay();
                        uiHelper.showAlertToast(R.string.unable_to_send_image_message);
                    }
                }, new ProgressCallback<BMessage>() {
                    @Override
                    public void onProgress(BMessage message) {
                        // Adding the message after it was prepared bt the NetworkAdapter.
                        if (messagesListAdapter != null)
                        {
                            messagesListAdapter.addRow(message);
                            scrollListTo(messagesListAdapter.getCount(), true);
                        }
                    }
                });
    }

    public void sendLocationMessage(final Intent data){
        sendingMessageToast();
        BNetworkManager.sharedManager().getNetworkAdapter().sendMessageWithLocation(data.getExtras().getString(ChatSDKLocationActivity.SNAP_SHOT_PATH, null),
                new LatLng(data.getDoubleExtra(ChatSDKLocationActivity.LANITUDE, 0), data.getDoubleExtra(ChatSDKLocationActivity.LONGITUDE, 0)),
                thread.getId())
                .then(new DoneCallback<BMessage>() {
                    @Override
                    public void onDone(BMessage message) {
                        if (DEBUG) Timber.v("Image is sent");
                        uiHelper.dismissProgressCardWithSmallDelay();
                    }
                }, new FailCallback<BError>() {
                    @Override
                    public void onFail(BError error) {
                        uiHelper.dismissProgressCardWithSmallDelay();
                        uiHelper.showAlertToast(R.string.unable_to_send_location_message);
                    }
                }, new ProgressCallback<BMessage>() {
                    @Override
                    public void onProgress(BMessage message) {
                        // Adding the message after it was prepared bt the NetworkAdapter.
                        String path = data.getExtras().getString(ChatSDKLocationActivity.SNAP_SHOT_PATH, "");
                        if (StringUtils.isNotBlank(path))
                        {
                            if (messagesListAdapter != null)
                            {
                                messagesListAdapter.addRow(message);
                                scrollListTo(messagesListAdapter.getCount(), true);
                            }
                        }
                    }
                });
    }
    

    /** Check the intent if carries some data that received from another app to share on this chat.*/
    public void checkIfWantToShare(Intent intent){
        if (DEBUG) Timber.v("checkIfWantToShare");

        if (!hasActivity())
            return;

        if (shared)
            return;

        if (intent.getExtras() == null || intent.getExtras().isEmpty())
        {
            return;
        }

        if (intent.getExtras().containsKey(SHARED_FILE_URI))
        {
            if (DEBUG) Timber.i("Want to share URI");

            try{
                String path = Utils.getRealPathFromURI(activity.get(), (Uri) intent.getExtras().get(SHARED_FILE_URI));

                if (DEBUG) Timber.d("Path from uri: " + path);

                uiHelper.showProgressCard(R.string.sending);

                sendImageMessage(path);
            }
            catch (NullPointerException e){
                uiHelper.showAlertToast(R.string.unable_to_fetch_image);
            }

            // removing the key so we wont send again,
            intent.getExtras().remove(SHARED_FILE_URI);

            intent.removeExtra(SHARED_FILE_URI);

            shared = true;
        }
        else if (intent.getExtras().containsKey(SHARED_TEXT))
        {
            if (DEBUG) Timber.i("Want to share Text");

            String text =intent.getExtras().getString(SHARED_TEXT);

            // removing the key so we wont send again,
            intent.getExtras().remove(SHARED_TEXT);

            sendMessageWithText(text, false);

            intent.removeExtra(SHARED_TEXT);

            shared = true;
        }
        else if (intent.getExtras().containsKey(SHARED_FILE_PATH))
        {
            if (DEBUG) Timber.i("Want to share File from path");
            uiHelper.showProgressCard("Sending...");

            String path =intent.getStringExtra(SHARED_FILE_PATH);

            // removing the key so we wont send again,
            intent.getExtras().remove(SHARED_FILE_PATH);

            sendImageMessage(path);

            intent.removeExtra(SHARED_FILE_PATH);

            shared = true;
        }
        else if (intent.getExtras().containsKey(SHARE_LOCATION)){
            if (DEBUG) Timber.i("Want to share Location");
            // FIXME pull text from string resource for language control
            uiHelper.showProgressCard(R.string.sending);

            BNetworkManager.sharedManager().getNetworkAdapter().sendMessageWithLocation(intent.getExtras().getString(SHARE_LOCATION, null),
                    new LatLng(intent.getDoubleExtra(LAT, 0), intent.getDoubleExtra(LNG, 0)),
                    thread.getId())
                    .done(new DoneCallback<BMessage>() {
                        @Override
                        public void onDone(BMessage message) {
                            if (DEBUG) Timber.v("Image is sent");

                            uiHelper.dismissProgressCardWithSmallDelay();
                        }
                    })
                    .fail(new FailCallback<BError>() {
                        @Override
                        public void onFail(BError bError) {
                            uiHelper.dismissProgressCardWithSmallDelay();
                            uiHelper.showAlertToast(R.string.unable_to_send_location_message);
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
        return StringUtils.isNotEmpty(getSelectedFilePath()) && StringUtils.isNotBlank(getSelectedFilePath());
    }

    public void setThread(BThread thread) {
        this.thread = thread;
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
    
    public boolean hasActivity(){
        return  activity != null && activity.get() != null;
        
    }
}
