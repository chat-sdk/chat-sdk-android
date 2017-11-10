/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.main;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import co.chatsdk.core.session.NM;

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

    public void setupTouchUIToDismissKeyboard(View view, final Integer... exceptIDs) {
        BaseActivity.setupTouchUIToDismissKeyboard(view, new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                BaseActivity.hideSoftKeyboard((AppCompatActivity) getActivity());
                return false;
            }
        }, exceptIDs);
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

    abstract public void clearData ();
    public void safeReloadData () {
        if(getView() != null && NM.auth().userAuthenticated()) {
            reloadData();
        }
    }
    public abstract void reloadData();

}


