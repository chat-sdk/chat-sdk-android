package com.braunster.chatsdk.Utils;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.braunster.chatsdk.R;

/**
 * Created by braunster on 19/06/14.
 */
public class DialogUtils {

    // TODO show friends from facebook.
    public static class FriendsListDialog extends android.app.DialogFragment {
    }

    public static class EditTextDialog extends DialogFragment implements TextView.OnEditorActionListener {

        private EditText mEditText;
        private String dialogTitle = "Title";
        private EditTextDialogInterface listener;

        public static EditTextDialog getInstace(){
            EditTextDialog f = new EditTextDialog();

            return f;
        }

        public EditTextDialog() {
            // Empty constructor required for DialogFragment
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.chat_sdk_dialog_edit_text, container);
            mEditText = (EditText) view.findViewById(R.id.et_enter);

            getDialog().setTitle(dialogTitle);


            // Show soft keyboard automatically
            mEditText.requestFocus();
            getDialog().getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

            // Listen to Done press on the keyboard.
            mEditText.setOnEditorActionListener(this);

            return view;
        }

        public void setTitleAndListen(String title, EditTextDialogInterface listener){
            this.dialogTitle = title;
            this.listener = listener;
        }

        public interface EditTextDialogInterface extends DialogInterface<String>{

        }

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (EditorInfo.IME_ACTION_DONE == actionId) {
                if (mEditText.getText().toString().isEmpty())
                {
                    Toast t = Toast.makeText(getActivity(), "Please enter chat name", Toast.LENGTH_SHORT);
                    t.setGravity(Gravity.TOP, 0, 0);
                    t.show();
                    return true;
                }

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);

                // Return input text to activity
                listener.onFinished(mEditText.getText().toString());
                this.dismiss();
                return true;
            }
            return false;
        }
    }

    public interface DialogInterface<T>{
        public void onFinished(T t);
    }
}
