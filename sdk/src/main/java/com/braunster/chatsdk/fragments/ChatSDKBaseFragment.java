package com.braunster.chatsdk.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.braunster.chatsdk.Utils.helper.ChatSDKUiHelper;
import com.braunster.chatsdk.dao.BLinkedContact;
import com.braunster.chatsdk.dao.BLinkedContactDao;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.core.DaoCore;
import com.braunster.chatsdk.dao.entities.Entity;
import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.braunster.chatsdk.interfaces.RepetitiveCompletionListenerWithMainTaskAndError;
import com.braunster.chatsdk.network.AbstractNetworkAdapter;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.listeners.AuthListener;
import com.braunster.chatsdk.object.BError;
import com.github.johnpersano.supertoasts.SuperToast;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.Callable;

/**
 * Created by itzik on 6/17/2014.
 */
public abstract class ChatSDKBaseFragment extends DialogFragment implements ChatSDKBaseFragmentInterface {

    private static final String TAG = ChatSDKBaseFragment.class.getSimpleName();
    private static final boolean DEBUG = true;

    private ProgressDialog progressDialog;

    protected View mainView;
    protected ChatSDKUiHelper chatSDKUiHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        chatSDKUiHelper = ChatSDKUiHelper.getInstance().get(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void refresh() {
        loadData();
    }

    @Override
    public void refreshOnBackground() {
        loadDataOnBackground();
    }

    @Override
    public void refreshForEntity(Entity entity) {

    }

    @Override
    public void loadData() {

    }

    @Override
    public void loadDataOnBackground() {

    }

    @Override
    public void clearData() {

    }

    /** Set up the ui so every view and nested view that is not EditText will listen to touch event and dismiss the keyboard if touched.*/
    public void setupTouchUIToDismissKeyboard(View view) {
        ChatSDKUiHelper.setupTouchUIToDismissKeyboard(view, new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ChatSDKUiHelper.hideSoftKeyboard(getActivity());
                return false;
            }
        });
    }

    public void setupTouchUIToDismissKeyboard(View view, final Integer... exceptIDs) {
        ChatSDKUiHelper.setupTouchUIToDismissKeyboard(view, new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ChatSDKUiHelper.hideSoftKeyboard(getActivity());
                return false;
            }
        }, exceptIDs);
    }

    public void setupTouchUIToDismissKeyboard(View view, View.OnTouchListener onTouchListener, final Integer... exceptIDs) {
        ChatSDKUiHelper.setupTouchUIToDismissKeyboard(view, onTouchListener, exceptIDs);
    }

    @Override
    public void initViews() {

    }

    /** Show a SuperToast with the given text. */
    protected void showToast(String text){
        if (chatSDKUiHelper==null || StringUtils.isEmpty(text))
            return;
        chatSDKUiHelper.getToast().setText(text);
        chatSDKUiHelper.getToast().show();
    }

    protected void showAlertToast(String text){
        if (chatSDKUiHelper==null || StringUtils.isEmpty(text))
            return;
        chatSDKUiHelper.getAlertToast().setText(text);
        chatSDKUiHelper.getAlertToast().show();
    }

    /** Start the chat activity for the given thread id.
     * @param id is the long value of local db id.*/
    public void startChatActivityForID(long id){
        if (chatSDKUiHelper != null)
            chatSDKUiHelper.startChatActivityForID(id);
    }

    public void startLoginActivity(boolean loggedOut){
        if (chatSDKUiHelper != null)
            chatSDKUiHelper.startLoginActivity(loggedOut);
    }

    public void startMainActivity(){
        if (chatSDKUiHelper != null)
            chatSDKUiHelper.startMainActivity();
    }

    public void startSearchActivity() {
        if (chatSDKUiHelper != null)
            chatSDKUiHelper.startSearchActivity();
    }

    public void startPickFriendsActivity() {
        if (chatSDKUiHelper != null)
            chatSDKUiHelper.startPickFriendsActivity();
    }

    public void startShareWithFriendsActivity() {
        if (chatSDKUiHelper != null)
            chatSDKUiHelper.startShareWithFriendsActivity();
    }

    public void startShareLocationActivityActivity() {
        if (chatSDKUiHelper != null)
            chatSDKUiHelper.startShareLocationActivityActivity();
    }

    /** Create or fetch chat for users, Opens the chat when done.*/
    protected void createAndOpenThreadWithUsers(String name, BUser...users){
        createAndOpenThreadWithUsers(name, null, true, users);
    }
    /** Create or fetch chat for users, Opens the chat when done.*/
    protected void createAndOpenThreadWithUsers(String name, final CompletionListenerWithData doneListener, BUser...users){
        createAndOpenThreadWithUsers(name, doneListener, true, users);
    }
    /** Create or fetch chat for users. Opens the chat if wanted.*/
    protected void createAndOpenThreadWithUsers(String name, final CompletionListenerWithData doneListener, final boolean openChatWhenDone, BUser...users){
        BNetworkManager.sharedManager().getNetworkAdapter().createThreadWithUsers(name, new RepetitiveCompletionListenerWithMainTaskAndError<BThread, BUser, Object>() {

            BThread thread = null;

            @Override
            public boolean onMainFinised(BThread bThread, Object o) {
                if (o != null)
                {
                    showToast("Failed to start chat.");
                    return true;
                }

                if (DEBUG) Log.d(TAG, "New thread is created.");

                thread = bThread;

                return false;
            }

            @Override
            public boolean onItem(BUser item) {
                return false;
            }

            @Override
            public void onDone() {
                Log.d(TAG, "On done.");
                if (thread != null)
                {
                    if (openChatWhenDone)
                        startChatActivityForID(thread.getId());

                    if (doneListener != null)
                        doneListener.onDone(thread);
                }
            }

            @Override
            public void onItemError(BUser user, Object o) {
                if (DEBUG) Log.d(TAG, "Failed to add user to thread, User name: " +user.getName());
            }
        }, users);
    }



    protected void showProgDialog(String message){
        if (progressDialog == null)
            progressDialog = new ProgressDialog(getActivity());

        if (!progressDialog.isShowing())
        {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(message);
            progressDialog.show();
        }
    }

    protected void showOrUpdateProgDialog(String message){
        if (progressDialog == null)
            progressDialog = new ProgressDialog(getActivity());

        if (!progressDialog.isShowing())
        {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(message);
            progressDialog.show();
        } else progressDialog.setMessage(message);
    }

    protected void dismissProgDialog(){
        try {
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
        } catch (Exception e) {
            // For handling orientation changed.
            e.printStackTrace();
        }
    }

    protected void showAlertDialog(String title, String alert, String p, String n, final Callable neg, final Callable pos){
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        // set title if not null
        if (title != null && !title.equals(""))
            alertDialogBuilder.setTitle(title);

        // set dialog message
        alertDialogBuilder
                .setMessage(alert)
                .setCancelable(false)
                .setPositiveButton(p, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (pos != null)
                            try {
                                pos.call();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(n, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        if (neg != null)
                            try {
                                neg.call();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    protected class DeleteThread implements Callable{

        private String threadID;
        public DeleteThread(String threadID){
            this.threadID = threadID;
        }

        @Override
        public Object call() throws Exception {
            getNetworkAdapter().deleteThreadWithEntityID(threadID, new CompletionListener() {
                @Override
                public void onDone() {
                    showToast("Thread is deleted.");
                    refreshOnBackground();
                }

                @Override
                public void onDoneWithError(BError error) {
                    showToast("Unable to delete thread.");
                }
            });
            return null;
        }
    }

    class DeleteContact implements Callable{

        private String userID;

        public DeleteContact(String userID){
            this.userID = userID;
        }

        @Override
        public Object call() throws Exception {
            BLinkedContact linkedContact = DaoCore.<BLinkedContact>fetchEntityWithProperty(BLinkedContact.class, BLinkedContactDao.Properties.EntityID, userID);
            DaoCore.deleteEntity(linkedContact);
            loadData();
            return null;
        }
    }

    @Override
    public AbstractNetworkAdapter getNetworkAdapter() {
        return BNetworkManager.sharedManager().getNetworkAdapter();
    }

    /** Authenticates the current user.*/
    public void authenticate(AuthListener listener){
        getNetworkAdapter().checkUserAuthenticatedWithCallback(listener);
    }


    public void setChatSDKUiHelper(ChatSDKUiHelper chatSDKUiHelper) {
        this.chatSDKUiHelper = chatSDKUiHelper;
    }

    public void setToast(SuperToast toast) {
        chatSDKUiHelper.setToast(toast);
    }

    public SuperToast getToast() {
        return chatSDKUiHelper.getToast();
    }

    public SuperToast getAlertToast() {
        return chatSDKUiHelper.getAlertToast();
    }
}

interface ChatSDKBaseFragmentInterface extends ChatSDKUiHelper.ChatSDKUiHelperInterface{
    public void refresh();

    public void refreshOnBackground();

    public void loadData();

    public void loadDataOnBackground();

    public void initViews();

    public void clearData();

    public void refreshForEntity(Entity entity);

    public AbstractNetworkAdapter getNetworkAdapter();
}

