/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.helper.ChatSDKUiHelper;
import com.braunster.chatsdk.dao.BThread;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.dao.entities.Entity;
import com.braunster.chatsdk.network.AbstractNetworkAdapter;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.object.BError;
import com.github.johnpersano.supertoasts.SuperToast;

import org.apache.commons.lang3.StringUtils;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.Promise;

import java.util.concurrent.Callable;

/**
 * Created by itzik on 6/17/2014.
 */
public abstract class ChatSDKBaseFragment extends android.app.DialogFragment implements ChatSDKBaseFragmentInterface {

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
    protected Promise<BThread, BError, Void>  createAndOpenThreadWithUsers(String name, BUser...users){
        return createThreadWithUsers(name, true, users);
    }
    /** Create or fetch chat for users. Opens the chat if wanted.*/
    protected Promise<BThread, BError, Void>  createThreadWithUsers(String name, final boolean openChatWhenDone, BUser... users) {
        return getNetworkAdapter().createThreadWithUsers(name, users)
                .done(new DoneCallback<BThread>() {
                    @Override
                    public void onDone(BThread thread) {
                        if (thread != null) {
                            if (openChatWhenDone)
                                startChatActivityForID(thread.getId());
                        }
                    }
                })
                .fail(new FailCallback<BError>() {
                    @Override
                    public void onFail(BError error) {
                        if (isOnMainThread())
                            showAlertToast(getString(R.string.create_thread_with_users_fail_toast));
                        else getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showAlertToast(getString(R.string.create_thread_with_users_fail_toast));
                            }
                        });
                    }
                });
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

    protected boolean isOnMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            return false;
        }

        return true;
    }

    protected void showAlertDialog(String title, String alert, String p, String n, final Callable neg, final Callable pos){
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        // set title if not null
        if (StringUtils.isNotBlank(title))
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
            getNetworkAdapter().deleteThreadWithEntityID(threadID)
                    .done(new DoneCallback<Void>() {
                        @Override
                        public void onDone(Void aVoid) {
                            showToast( getString(R.string.delete_thread_success_toast) );
                            refreshOnBackground();
                        }
                    })
                    .fail(new FailCallback<BError>() {
                        @Override
                        public void onFail(BError error) {
                            showAlertToast(  getString(R.string.delete_thread_fail_toast)  );
                        }
                    });

            return null;
        }
    }

    @Override
    public AbstractNetworkAdapter getNetworkAdapter() {
        return BNetworkManager.sharedManager().getNetworkAdapter();
    }

    /** Authenticates the current user.*/
    public Promise<BUser, BError, Void> authenticate(){
        return getNetworkAdapter().checkUserAuthenticated();
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

