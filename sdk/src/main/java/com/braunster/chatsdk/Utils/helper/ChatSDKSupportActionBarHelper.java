package com.braunster.chatsdk.Utils.helper;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.braunster.chatsdk.R;


/**
 * Created by braunster on 24/09/14.
 */
public class ChatSDKSupportActionBarHelper {

    private ActionBar actionBar;
    private View actionBarView;
    private Activity activity;

    public ChatSDKSupportActionBarHelper(Activity activity) {
        this.activity = activity;
    }

    public ChatSDKSupportActionBarHelper setActionBar(ActionBar actionBar) {
        this.actionBar = actionBar;
        return this;
    }

    public ChatSDKSupportActionBarHelper readyActionBarToCustomView(){
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        return this;
    }

    public ChatSDKSupportActionBarHelper hideUpIndicator(){
        actionBar.setHomeAsUpIndicator(android.R.color.transparent);
        return this;

    }

    public ChatSDKSupportActionBarHelper inflateActionBarView(int resId){
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

    public ChatSDKSupportActionBarHelper setViewWithDefaultParams(){
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

    public ChatSDKSupportActionBarHelper setLeftIcon(int id){
        ((ImageView) actionBarView.findViewById(R.id.chat_sdk_left_icon)).setImageResource(id);
        return this;
    }

    public ChatSDKSupportActionBarHelper setCenterIcon(int id){
        ((ImageView) actionBarView.findViewById(R.id.chat_sdk_center_icon)).setImageResource(id);
        return this;
    }

    public ChatSDKSupportActionBarHelper setRightIcon(int id){
        ((ImageView) actionBarView.findViewById(R.id.chat_sdk_right_icon)).setImageResource(id);
        return this;
    }

    public enum CenterViewOption{
        ICON, TEXT;
    }
    public ChatSDKSupportActionBarHelper setCenter(CenterViewOption option){
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

    public ChatSDKSupportActionBarHelper setBackground(int resID){
        actionBarView.findViewById(R.id.chat_sdk_content).setBackgroundResource(resID);
        return this;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public ChatSDKSupportActionBarHelper setBackground(Drawable drawable){
        actionBarView.findViewById(R.id.chat_sdk_content).setBackground(drawable);
        return this;
    }
}
