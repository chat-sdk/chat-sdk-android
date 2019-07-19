/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.main;

import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.utils.DisposableList;

/**
 * Created by itzik on 6/17/2014.
 */
public abstract class BaseFragment extends DialogFragment {

    protected ProgressDialog progressDialog;

    protected View mainView;
    protected boolean tabIsVisible;
    protected DisposableList disposableList = new DisposableList();

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

    public void setupTouchUIToDismissKeyboard(View view, final Integer... exceptIDs) {
        BaseActivity.setupTouchUIToDismissKeyboard(view, (v, event) -> {
            hideKeyboard();
            return false;
        }, exceptIDs);
    }

    public void hideKeyboard () {
        BaseActivity.hideKeyboard(getActivity());
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

    public void setTabVisibility (boolean isVisible) {
        tabIsVisible = isVisible;
    }

    protected void dismissProgressDialog() {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (Exception e) {
            ChatSDK.logError(e);
            // For handling orientation changed.
        }
    }

    abstract public void clearData ();
    public void safeReloadData () {
        if(getView() != null && ChatSDK.auth().isAuthenticated()) {
            reloadData();
        }
    }
    public abstract void reloadData();

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposableList.dispose();
    }

}


