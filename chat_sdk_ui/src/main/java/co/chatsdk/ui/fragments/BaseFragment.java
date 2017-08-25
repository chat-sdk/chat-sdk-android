/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.fragments;

import android.app.AlertDialog;
import android.support.v4.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import co.chatsdk.core.NM;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.ui.helpers.UIHelper;
import io.reactivex.CompletableObserver;
import io.reactivex.disposables.Disposable;
import co.chatsdk.ui.R;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.Callable;

/**
 * Created by itzik on 6/17/2014.
 */
public abstract class BaseFragment extends DialogFragment {

    private ProgressDialog progressDialog;

    protected View mainView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    public void setupTouchUIToDismissKeyboard(View view, final Integer... exceptIDs) {
        UIHelper.shared().setupTouchUIToDismissKeyboard(view, new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                UIHelper.shared().hideSoftKeyboard((AppCompatActivity) getActivity());
                return false;
            }
        }, exceptIDs);
    }

    /** Show a SuperToast with the given text. */
    protected void showToast(String text){
        if (!StringUtils.isEmpty(text)) {
            UIHelper.shared().showToast(text);
        }
    }

    public void startPickFriendsActivity() {
        UIHelper.shared().startPickFriendsActivity();
    }


    public void startProfileActivityForUser (User user) {
        UIHelper.shared().startProfileActivity(user.getEntityID());
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

        private Thread thread;
        public DeleteThread(Thread thread){
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

}


