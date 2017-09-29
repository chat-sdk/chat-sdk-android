/*
 * Created by Itzik Braun on 12/3/2015.
 * Copyright (c) 2015 deluge. All rights reserved.
 *
 * Last Modification at: 3/12/15 4:27 PM
 */

package co.chatsdk.ui.chat;

import android.content.Context;
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
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import co.chatsdk.core.NM;
import co.chatsdk.core.audio.Recording;
import co.chatsdk.core.utils.StringUtils;
import co.chatsdk.ui.R;
import co.chatsdk.core.defines.Debug;
import co.chatsdk.ui.helpers.DialogUtils;
import co.chatsdk.ui.utils.InfiniteToast;
import co.chatsdk.ui.utils.Utils;

public class TextInputView extends LinearLayout implements View.OnClickListener , View.OnKeyListener, TextView.OnEditorActionListener{

    public static final String TAG = TextInputView.class.getSimpleName();
    public static final boolean DEBUG = Debug.ChatMessageBoxView;

    protected Listener listener;
    protected ImageButton btnSend;
    protected ImageButton btnOptions;
    protected EditText etMessage;
    protected PopupWindow optionPopup;
    protected boolean audioModeEnabled = false;
    protected boolean recordOnPress = false;
    protected Recording recording = null;
    protected InfiniteToast toast;

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

    protected void init(){
        inflate(getContext(), R.layout.chat_sdk_view_message_box, this);
    }

    protected void initViews(){
        btnSend = (ImageButton) findViewById(R.id.chat_sdk_btn_chat_send_message);
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

        // Handle recording when the record button is held down
        btnSend.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(recordOnPress) {

                    // Start recording when we press down
                    if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        recording = new Recording();
                        recording.start();
                        toast = new InfiniteToast(getContext(), R.string.recording, true);
                    }

                    // Stop recording
                    if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        if(recording != null) {
                            recording.stop();
                            if(listener != null) {
                                listener.sendAudio(recording);
                                recording = null;
                            }
                        }
                        if(toast != null) {
                            toast.cancel();
                        }
                    }
                }
                return btnSend.onTouchEvent(motionEvent);
            }
        });

        btnOptions.setOnClickListener(this);

        etMessage.setOnEditorActionListener(this);
        etMessage.setOnKeyListener(this);
        etMessage.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(listener != null) {
                    listener.startTyping();
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
        if(StringUtils.isNullOrEmpty(getMessageText()) && audioModeEnabled) {
            btnSend.setBackgroundResource(R.drawable.ic_36_mic);
            recordOnPress = true;
        }
        else {
            btnSend.setBackgroundResource(R.drawable.ic_36_send);
            recordOnPress = false;
        }
    }

    /** Show the message option popup, From here the user can send images and location messages.*/
    public void showOptionPopup () {
        if (optionPopup!= null && optionPopup.isShowing()) {
            return;
        }

        optionPopup = DialogUtils.getMenuOptionPopup(getContext(), this);
        optionPopup.showAsDropDown(btnOptions);
    }

    public void dismissOptionPopup() {
        if (optionPopup != null) {
            optionPopup.dismiss();
        }
    }

    /* Implement listeners.*/
    @Override
    public void onClick(View v) {
        int id= v.getId();

        if (id == R.id.chat_sdk_btn_chat_send_message) {
            if(!recordOnPress) {
                if (listener != null) {
                    listener.onSendPressed(getMessageText());
                }
            }
        }
        else if (id == R.id.chat_sdk_btn_options) {
            showOptionPopup();
        }
        else  if (id == R.id.chat_sdk_btn_choose_picture) {
            dismissOptionPopup();
            if (listener != null) {
                listener.onPickImagePressed();
            }
        }
        else  if (id == R.id.chat_sdk_btn_take_picture) {
            if (!Utils.SystemChecks.checkCameraHardware(getContext())) {
                // TODO: Localize this
                Toast.makeText(getContext(), "This device does not have a camera.", Toast.LENGTH_SHORT).show();
                return;
            }

            dismissOptionPopup();

            if (listener != null) {
                listener.onTakePhotoPressed();
            }
        }
        else  if (id == R.id.chat_sdk_btn_location) {
            dismissOptionPopup();

            if (listener != null) {
                listener.onLocationPressed();
            }
        }
    }

    /** Send a text message when the done button is pressed on the keyboard.*/
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND)
            if (listener!=null)
                listener.onSendPressed(getMessageText());

        return false;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        // if enter is pressed start calculating
        if(listener != null) {
            listener.startTyping();
        }
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            int editTextLineCount = ((EditText) v).getLineCount();
            if (editTextLineCount >= getResources().getInteger(R.integer.chat_sdk_max_message_lines))
                return true;
        }
        return false;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public String getMessageText(){
        return etMessage.getText().toString();
    }

    public void clearText(){
        etMessage.getText().clear();
    }


    public interface Listener {
        void onLocationPressed();
        void onTakePhotoPressed();
        void onPickImagePressed();
        void onSendPressed(String text);
        void startTyping();
        void sendAudio (Recording recording);
        void stopTyping();
    }

}
