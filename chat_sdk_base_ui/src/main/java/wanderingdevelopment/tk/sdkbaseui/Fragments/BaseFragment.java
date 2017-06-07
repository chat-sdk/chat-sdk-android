/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package wanderingdevelopment.tk.sdkbaseui.Fragments;

import android.app.AlertDialog;
import android.support.v4.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import co.chatsdk.core.NM;

import co.chatsdk.core.dao.BThread;
import co.chatsdk.core.dao.BUser;
import io.reactivex.CompletableObserver;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import wanderingdevelopment.tk.sdkbaseui.R;
import wanderingdevelopment.tk.sdkbaseui.UiHelpers.ChatSDKUiHelper;

import com.github.johnpersano.supertoasts.SuperToast;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.Callable;

/**
 * Created by itzik on 6/17/2014.
 */
public abstract class BaseFragment extends DialogFragment {

    private static final String TAG = BaseFragment.class.getSimpleName();
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

    public void refresh() {
        loadData();
    }

    public void refreshForEntity(Object entity) {

    }

    public void loadData() {

    }

    public void clearData() {

    }

    /** Set up the ui so every view and nested view that is not EditText will listen to touch event and dismiss the keyboard if touched.*/
    public void setupTouchUIToDismissKeyboard(View view) {
        ChatSDKUiHelper.setupTouchUIToDismissKeyboard(view, new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ChatSDKUiHelper.hideSoftKeyboard((AppCompatActivity) getActivity());
                return false;
            }
        });
    }

    public void setupTouchUIToDismissKeyboard(View view, final Integer... exceptIDs) {
        ChatSDKUiHelper.setupTouchUIToDismissKeyboard(view, new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ChatSDKUiHelper.hideSoftKeyboard((AppCompatActivity) getActivity());
                return false;
            }
        }, exceptIDs);
    }

    public void setupTouchUIToDismissKeyboard(View view, View.OnTouchListener onTouchListener, final Integer... exceptIDs) {
        ChatSDKUiHelper.setupTouchUIToDismissKeyboard(view, onTouchListener, exceptIDs);
    }

    public void initViews() {

    }

    /** Show a SuperToast with the given text. */
    protected void showToast(String text){
        if (chatSDKUiHelper==null || StringUtils.isEmpty(text))
            return;
        chatSDKUiHelper.getToast().setText(text);
        chatSDKUiHelper.getToast().show();
    }

    /** Start the chat activity for the given thread id.
     * @param id is the long value of local db id.*/
    public void startChatActivityForID(long id){
        if (chatSDKUiHelper != null)
            chatSDKUiHelper.startChatActivityForID(id);
    }

    public void startPickFriendsActivity() {
        if (chatSDKUiHelper != null)
            chatSDKUiHelper.startPickFriendsActivity();
    }

    /** Create or fetch chat for users, Opens the chat when done.*/
    protected Single<BThread> createAndOpenThreadWithUsers(String name, BUser...users){
        return createThreadWithUsers(name, true, users);
    }
    /** Create or fetch chat for users. Opens the chat if wanted.*/
    protected Single<BThread> createThreadWithUsers(String name, final boolean openChatWhenDone, BUser... users) {
        return NM.thread().createThread(name, users).doOnSuccess(new Consumer<BThread>() {
            @Override
            public void accept(BThread thread) throws Exception {
                if (thread != null) {
                    if (openChatWhenDone)
                        startChatActivityForID(thread.getId());
                }
            }
        }).doOnError(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                if (isOnMainThread())
                    showToast(getString(R.string.create_thread_with_users_fail_toast));
                else getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast(getString(R.string.create_thread_with_users_fail_toast));
                    }
                });
            }
        });
    }

    protected void showOrUpdateProgressDialog(String message) {
        if (progressDialog == null)
            progressDialog = new ProgressDialog(getActivity());

        if (!progressDialog.isShowing())
        {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(message);
            progressDialog.show();
        }
        else {
            progressDialog.setMessage(message);
        }
    }

    protected void dismissProgressDialog() {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (Exception e) {
            // For handling orientation changed.
            e.printStackTrace();
        }
    }

    protected boolean isOnMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    protected void showToastDialog(String title, String alert, String p, String n, final Callable neg, final Callable pos){
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

    protected class DeleteThread implements Callable {

        private BThread thread;
        public DeleteThread(BThread thread){
            this.thread = thread;
        }

        @Override
        public Object call() throws Exception {
            NM.thread().deleteThread(thread).subscribe(new CompletableObserver() {
                @Override
                public void onSubscribe(Disposable d) {
                }

                @Override
                public void onComplete() {
                    showToast( getString(R.string.delete_thread_success_toast));
                    loadData();
                }

                @Override
                public void onError(Throwable e) {
                    showToast(getString(R.string.delete_thread_fail_toast));
                }
            });

            return null;
        }
    }

    public void setChatSDKUiHelper(ChatSDKUiHelper chatSDKUiHelper) {
        this.chatSDKUiHelper = chatSDKUiHelper;
    }

}


