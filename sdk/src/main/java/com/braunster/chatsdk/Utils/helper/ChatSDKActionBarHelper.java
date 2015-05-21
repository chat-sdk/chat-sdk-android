/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.Utils.helper;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.braunster.chatsdk.R;


public class ChatSDKActionBarHelper {

    private ActionBar actionBar;
    private View actionBarView;
    private Activity activity;

    public ChatSDKActionBarHelper(Activity activity) {
        this.activity = activity;
        actionBar = activity.getActionBar();
    }

    public ChatSDKActionBarHelper setActionBar(ActionBar actionBar) {
        this.actionBar = actionBar;
        return this;
    }

    public ChatSDKActionBarHelper readyActionBarToCustomView(){
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setHomeButtonEnabled(false);

        return this;
    }

    /** If you want to achieve this for lower api the best way it to add a theme to your activity with the homeAsUpIndicator as transparent.
     * Example can be found in the style.xml file*/
    public ChatSDKActionBarHelper hideUpIndicator(){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1)
            actionBar.setHomeAsUpIndicator(android.R.color.transparent);

        actionBar.setDisplayHomeAsUpEnabled(false);

        return this;

    }

    public ChatSDKActionBarHelper inflateActionBarView(int resId){
        // Inflate the custom view
        if (actionBarView == null || actionBarView.getId() != resId) {
            LayoutInflater inflater = LayoutInflater.from(activity);
            actionBarView = inflater.inflate(resId, null);
        }

        return this;
    }

    public void setView(){
        actionBar.setCustomView(actionBarView);
    }

    public void setView(ActionBar.LayoutParams params){
        actionBar.setCustomView(actionBarView, params);
    }

    public ChatSDKActionBarHelper setViewWithDefaultParams(){
        ActionBar.LayoutParams lp =
                new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                        ActionBar.LayoutParams.MATCH_PARENT);
        setView(lp);

        return this;
    }

    public ImageView getLeftIcon(){
        return (ImageView) actionBarView.findViewById(R.id.chat_sdk_left_icon);
    }

    public ImageView getCenterIcon(){
        return (ImageView) actionBarView.findViewById(R.id.chat_sdk_center_icon);
    }

    public ImageView getRightIcon(){
        return (ImageView) actionBarView.findViewById(R.id.chat_sdk_right_icon);
    }

    public TextView getCenterTextView(){
        return (TextView) actionBarView.findViewById(R.id.chat_sdk_txt_header_center);
    }

    public TextView getRightTextView(){
        return (TextView) actionBarView.findViewById(R.id.chat_sdk_txt_header_right);
    }

    public TextView getLeftTextView(){
        return (TextView) actionBarView.findViewById(R.id.chat_sdk_txt_header_left);
    }

    public ChatSDKActionBarHelper setLeftIcon(int id){
        ((ImageView) actionBarView.findViewById(R.id.chat_sdk_left_icon)).setImageResource(id);
        return this;
    }

    public ChatSDKActionBarHelper setCenterIcon(int id){
        ((ImageView) actionBarView.findViewById(R.id.chat_sdk_center_icon)).setImageResource(id);
        return this;
    }

    public ChatSDKActionBarHelper setRightIcon(int id){
        ((ImageView) actionBarView.findViewById(R.id.chat_sdk_right_icon)).setImageResource(id);
        return this;
    }

    public enum CenterViewOption{
        ICON, TEXT;
    }

    public ChatSDKActionBarHelper setCenter(CenterViewOption option){
        switch (option)
        {
            case ICON:
                actionBarView.findViewById(R.id.chat_sdk_txt_header_center).setVisibility(View.INVISIBLE);
                actionBarView.findViewById(R.id.chat_sdk_center_icon).setVisibility(View.VISIBLE);
                break;

            case TEXT:
                actionBarView.findViewById(R.id.chat_sdk_center_icon).setVisibility(View.INVISIBLE);
                actionBarView.findViewById(R.id.chat_sdk_txt_header_center).setVisibility(View.VISIBLE);
                break;
        }

        return this;
    }

    public ActionBar getActionBar() {
        return actionBar;
    }

    public View getActionBarView() {
        return actionBarView;
    }

    public ChatSDKActionBarHelper setBackground(int resID){
        actionBarView.findViewById(R.id.chat_sdk_content).setBackgroundResource(resID);
        return this;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public ChatSDKActionBarHelper setBackground(Drawable drawable){
        actionBarView.findViewById(R.id.chat_sdk_content).setBackground(drawable);
        return this;
    }
}
