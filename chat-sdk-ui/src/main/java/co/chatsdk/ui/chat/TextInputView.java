/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.chat;

import android.content.Context;
import android.graphics.Rect;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import co.chatsdk.core.audio.Recording;
import co.chatsdk.core.utils.StringChecker;
import co.chatsdk.ui.R;
import co.chatsdk.ui.utils.InfiniteToast;
import co.chatsdk.ui.utils.ToastHelper;

public class TextInputView extends LinearLayout implements View.OnKeyListener, TextView.OnEditorActionListener{

    protected ImageButton btnSend;
    protected ImageButton btnOptions;
    protected EditText etMessage;
    protected boolean audioModeEnabled = false;
    protected boolean recordOnPress = false;
    protected Recording recording = null;
    protected InfiniteToast toast;
    protected WeakReference<TextInputDelegate> delegate;
    protected Rect rect;

    public TextInputView(Context context) {
        super(context);
        init();
    }

    public TextInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TextInputView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setDelegate (TextInputDelegate delegate) {
        this.delegate = new WeakReference<>(delegate);
    }

    protected void init(){
        inflate(getContext(), R.layout.chat_sdk_view_message_box, this);
    }

    protected void initViews(){
        btnSend = findViewById(R.id.chat_sdk_btn_chat_send_message);
        btnOptions = findViewById(R.id.chat_sdk_btn_options);
        etMessage = findViewById(R.id.chat_sdk_et_message_to_send);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initViews();

        if (isInEditMode()) {
            return;
        }

        btnSend.setOnClickListener(view -> {
            if(!recordOnPress) {
                if (delegate != null) {
                    delegate.get().onSendPressed(getMessageText());
                }
            }
        });

        // Handle recording when the record button is held down
        btnSend.setOnTouchListener((view, motionEvent) -> {
            if(recordOnPress) {

                // Start recording when we press down
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    recording = new Recording();
                    recording.start().subscribe(() -> toast = new InfiniteToast(getContext(), R.string.recording, true));

                    rect = new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
                }

                // Stop recording
                if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(recording != null) {
                        recording.stop();
                        if(delegate != null && recording.getDurationMillis() > 1000) {
                            if(!rect.contains(view.getLeft() + (int) motionEvent.getX(), view.getTop() + (int) motionEvent.getY())){
                                // User moved outside bounds
                                ToastHelper.show(getContext(), "Recording cancelled");
                            }
                            else {
                                delegate.get().sendAudio(recording);
                            }
                        }
                        else {
                            ToastHelper.show(getContext(), "Recording is too short");
                        }
                        recording = null;
                    }
                    if(toast != null) {
                        toast.cancel();
                    }
                }
            }
            return btnSend.onTouchEvent(motionEvent);
        });

        btnOptions.setOnClickListener(view -> showOption());

        etMessage.setOnEditorActionListener(this);
        etMessage.setOnKeyListener(this);
        etMessage.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        etMessage.setOnFocusChangeListener((view, focus) -> {
            if(delegate != null) {
                if(focus) {
                    delegate.get().onKeyboardShow();
                }
                else {
                    delegate.get().onKeyboardHide();
                }
            }
        });

        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(delegate != null) {
                    delegate.get().startTyping();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                updateSendButton();
            }
        });

    }

    public void setAudioModeEnabled (boolean audioEnabled) {
        audioModeEnabled = audioEnabled;
        updateSendButton();
    }

    public void updateSendButton () {
        if(StringChecker.isNullOrEmpty(getMessageText()) && audioModeEnabled) {
            btnSend.setBackgroundResource(R.drawable.ic_36_mic);
            recordOnPress = true;
        }
        else {
            btnSend.setBackgroundResource(R.drawable.ic_36_send);
            recordOnPress = false;
        }
    }

    /** Show the message option popup, From here the user can send images and location messages.*/
    public void showOption () {
        if(delegate != null) {
            delegate.get().showOptions();
        }
    }

    public void hideOption () {
        if(delegate != null) {
            delegate.get().hideOptions();
        }
    }

    /** Send a text message when the done button is pressed on the keyboard.*/
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND && delegate != null) {
            delegate.get().onSendPressed(getMessageText());
        }

        return false;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        // if enter is pressed start calculating
        if(delegate != null) {
            delegate.get().startTyping();
        }
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            int editTextLineCount = ((EditText) v).getLineCount();
            if (editTextLineCount >= getResources().getInteger(R.integer.chat_sdk_max_message_lines))
                return true;
        }
        return false;
    }

    public String getMessageText(){
        return etMessage.getText().toString();
    }

    public void clearText(){
        etMessage.getText().clear();
    }



}
