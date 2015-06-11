/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package com.braunster.chatsdk.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.Utils.DialogUtils;
import com.braunster.chatsdk.Utils.Utils;
import com.github.johnpersano.supertoasts.SuperToast;

public class ChatMessageBoxView extends LinearLayout implements View.OnClickListener , View.OnKeyListener, TextView.OnEditorActionListener{

    public static final String TAG = ChatMessageBoxView.class.getSimpleName();
    public static final boolean DEBUG = Debug.ChatMessageBoxView;

    protected MessageBoxOptionsListener messageBoxOptionsListener;
    protected MessageBoxListener messageBoxListener;
    protected TextView btnSend;
    protected ImageButton btnOptions;
    protected EditText etMessage;
    protected PopupWindow optionPopup;

    /** The alert toast that the app will use to alert the user.*/
    protected SuperToast alertToast;

    public ChatMessageBoxView(Context context) {
        super(context);
        init();
    }

    public ChatMessageBoxView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChatMessageBoxView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    protected void init(){
        inflate(getContext(), R.layout.chat_sdk_view_message_box, this);
    }

    protected void initViews(){
        btnSend = (TextView) findViewById(R.id.chat_sdk_btn_chat_send_message);
        btnOptions = (ImageButton) findViewById(R.id.chat_sdk_btn_options);
        etMessage = (EditText) findViewById(R.id.chat_sdk_et_message_to_send);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initViews();

        if (isInEditMode())
            return;

        btnSend.setOnClickListener(this);

        btnOptions.setOnClickListener(this);

        etMessage.setOnEditorActionListener(this);
        etMessage.setOnKeyListener(this);

        etMessage.addTextChangedListener(new TextWatcher() {

            private int delayBeforeTypingStops = 2500;

            boolean isTyping = false;

            Runnable stoppedTypingRunnable = new Runnable() {
                @Override
                public void run() {
                    isTyping = false;

                    dispatchFinishedTyping();
                }
            };

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isTyping)
                {
                    dispatchStartedTyping();

                    isTyping = true;

                    postDelayed(stoppedTypingRunnable, delayBeforeTypingStops);
                }
                else
                {
                    // Remove the old callback and start a new buying us more time of typing
                    removeCallbacks(stoppedTypingRunnable);
                    postDelayed(stoppedTypingRunnable, delayBeforeTypingStops);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        dispatchFinishedTyping();
    }

    /** Show the message option popup, From here the user can send images and location messages.*/
    public void showOptionPopup(){
        if (optionPopup!= null && optionPopup.isShowing())
        {
            return;
        }

        optionPopup = DialogUtils.getMenuOptionPopup(getContext(), this);
        optionPopup.showAsDropDown(btnOptions);
    }

    public void dismissOptionPopup(){
        if (optionPopup != null)
            optionPopup.dismiss();
    }

    /* Implement listeners.*/
    @Override
    public void onClick(View v) {
        int id= v.getId();

        if (id == R.id.chat_sdk_btn_chat_send_message) {
            dispatchMessageSent();
        }
        else if (id == R.id.chat_sdk_btn_options){
            boolean b = false;
            if (messageBoxOptionsListener != null) {
                b = messageBoxOptionsListener.onOptionButtonPressed();
            }

            if (!b)
                showOptionPopup();
        }
        else  if (id == R.id.chat_sdk_btn_choose_picture) {
            dismissOptionPopup();

            if (messageBoxOptionsListener != null)
                messageBoxOptionsListener.onPickImagePressed();
        }
        else  if (id == R.id.chat_sdk_btn_take_picture) {
            if (!Utils.SystemChecks.checkCameraHardware(getContext()))
            {
                Toast.makeText(getContext(), "This device does not have a camera.", Toast.LENGTH_SHORT).show();
                return;
            }

            dismissOptionPopup();

            if (messageBoxOptionsListener != null)
                messageBoxOptionsListener.onTakePhotoPressed();
        }
        else  if (id == R.id.chat_sdk_btn_location) {
            dismissOptionPopup();

            if (messageBoxOptionsListener != null)
                messageBoxOptionsListener.onLocationPressed();
        }
    }

    /** Send a text message when the done button is pressed on the keyboard.*/
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND)
        {
            dispatchMessageSent();
        }

        return false;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        // if enter is pressed start calculating
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            int editTextLineCount = ((EditText) v).getLineCount();
            if (editTextLineCount >= getResources().getInteger(R.integer.chat_sdk_max_message_lines))
                return true;
        }
        return false;
    }

    public void setMessageBoxOptionsListener(MessageBoxOptionsListener messageBoxOptionsListener) {
        this.messageBoxOptionsListener = messageBoxOptionsListener;
    }

    public void setMessageBoxListener(MessageBoxListener messageBoxListener) {
        this.messageBoxListener = messageBoxListener;
    }

    public String getMessageText(){
        return etMessage.getText().toString();
    }

    public void clearText(){
        etMessage.getText().clear();
    }

    public ImageButton getOptionsButton() {
        return btnOptions;
    }

    /*Getters and Setters*/
    public void setAlertToast(SuperToast alertToast) {
        this.alertToast = alertToast;
    }

    public SuperToast getAlertToast() {
        return alertToast;
    }






    private void dispatchMessageSent(){
        if (messageBoxListener !=null)
            messageBoxListener.onSendPressed(getMessageText());

        dispatchFinishedTyping();
    }

    private void dispatchStartedTyping(){
        if (messageBoxListener != null)
            messageBoxListener.onTypingStart();
    }

    private void dispatchFinishedTyping(){
        if (messageBoxListener != null)
            messageBoxListener.onTypingFinished();
    }

    public interface MessageBoxOptionsListener{
        public void onLocationPressed();
        public void onTakePhotoPressed();
        public void onPickImagePressed();

        /** Invoked when the option button pressed, If returned true the system wont show the option popup.*/
        public boolean onOptionButtonPressed();

    }

    public interface MessageBoxListener {
        public void onSendPressed(String text);

        void onTypingStart();

        void onTypingFinished();
    }
}
