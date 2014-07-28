package com.braunster.chatsdk.Utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.volley.VolleyUtills;
import com.braunster.chatsdk.adapter.FBFriendsListVolleyAdapter;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.object.BError;
import com.facebook.model.GraphUser;
import com.github.johnpersano.supertoasts.SuperToast;

import java.util.List;

/**
 * Created by braunster on 19/06/14.
 */
public class DialogUtils {

    public static final String TAG = DialogUtils.class.getSimpleName();
    public static final boolean DEBUG = true;

    // TODO show friends from facebook.
    public static class FriendsListDialog extends DialogFragment {

    }

    //region AlertDialog currently not working.
    // TODO Customizing alert dialog id needed.
    public static void showAlertDialog(FragmentManager fm, String alert, DialogInterface<Intent> listener){
        ChatSDKAlertDialog dialog = ChatSDKAlertDialog.getInstace();

        dialog.setAlert(alert, listener);

        dialog.show(fm, "Alert Dialog");
    }
    //endregion

    public static class ChatSDKAlertDialog extends DialogFragment {

        private String alert = "Alert";
        private DialogInterface<Intent> listener;

        public static ChatSDKAlertDialog getInstace(){
            return new ChatSDKAlertDialog();

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.chat_sdk_dialog_edit_text, container);

            ((TextView)view.findViewById(R.id.textView)).setText(alert);
//
//
//            if ( listener != null)
//                listener.onFinished();
            return view;
        }

        public void setAlert(String alert, DialogInterface<Intent> listener){
            this.alert = alert;
            this.listener = listener;
        }
    }

    /** A dialog that contain editText, Response from dialog is received through the interface.*/
    public static class ChatSDKEditTextDialog extends DialogFragment implements TextView.OnEditorActionListener {

        private EditText mEditText;
        private String dialogTitle = "Title";
        private EditTextDialogInterface listener;

        public static ChatSDKEditTextDialog getInstace(){
            ChatSDKEditTextDialog f = new ChatSDKEditTextDialog();

            return f;
        }

        public ChatSDKEditTextDialog() {
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

        /** Option to add more callbacks to the dialog.*/
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

    public static class ChatSDKFacebookFriendsDialog extends DialogFragment {

        private DialogInterface<List<GraphUser>> listener;

        public static ChatSDKFacebookFriendsDialog getInstance(){
            return new ChatSDKFacebookFriendsDialog();
        }

        // TODO add check option for each user and return the list when done.
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            final View view = inflater.inflate(R.layout.chat_sdk_fb_friends_list_dialog, null);


            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

            BNetworkManager.sharedManager().getNetworkAdapter().getUserFacebookFriendsWithCallback(new CompletionListenerWithData<List<GraphUser>>() {
                @Override
                public void onDone(List<GraphUser> graphUsers) {
                    if (DEBUG) Log.v(TAG, "onDone");
                    // The regular adapter (i.e not volley) have problem sometime with the loading so ive changed to the volley.
                    // The adapter duplicate pictures.
//                    FBFriendsListAdapter adapter = new FBFriendsListAdapter(getActivity(), graphUsers);
                    FBFriendsListVolleyAdapter adapter = new FBFriendsListVolleyAdapter(getActivity(), graphUsers);
                    ((ListView) view.findViewById(R.id.chat_sdk_listview_friends_list)).setAdapter(adapter);
                }

                @Override
                public void onDoneWithError(BError error) {
                    if (DEBUG) Log.e(TAG, "cant find friends.");
                    Toast.makeText(getActivity(), "Cant find friends...", Toast.LENGTH_SHORT).show();
                }
            });

            view.findViewById(R.id.chat_sdk_btn_fb_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (DEBUG) Log.v(TAG, "Cancel");
                    dismiss();
                }
            });

            view.findViewById(R.id.chat_sdk_btn_fb_send).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (DEBUG) Log.v(TAG, "Send");
                }
            });
            return view;
        }

        public void setFinishedListener(DialogInterface<List<GraphUser>> listener){
            this.listener = listener;
        }
    }
    /** A popup to select the type of message to send, "Text", "Image", "Location".*/
    public static PopupWindow getMenuOptionPopup(Context context, View.OnClickListener listener){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.chat_sdk_popup_options, null);
        PopupWindow optionPopup = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupView.findViewById(R.id.chat_sdk_btn_choose_picture).setOnClickListener(listener);
        popupView.findViewById(R.id.chat_sdk_btn_take_picture).setOnClickListener(listener);
        popupView.findViewById(R.id.chat_sdk_btn_location).setOnClickListener(listener);

        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        // TODO fix popup size to wrap view size.
        optionPopup.setContentView(popupView);
        optionPopup.setBackgroundDrawable(new BitmapDrawable());
        optionPopup.setOutsideTouchable(true);
        optionPopup.setWidth(popupView.getMeasuredWidth());
        optionPopup.setHeight(popupView.getMeasuredHeight());
        return optionPopup;
    }

    /** Type indicate from where to load the file.*/
    public enum LoadTypes{
        LOAD_FROM_PATH, LOAD_FROM_URL, LOAD_FROM_BASE64
    }

    /** Full screen popup for showing an image in greater size.*/
    public static PopupWindow getImageDialog(final Context context, String data, LoadTypes loadingType){
        if (DEBUG) Log.v(TAG, "getImageDialog");

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.chat_sdk_popup_image, null);

        // Full screen popup.
        final PopupWindow imagePopup = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);

        // Dismiss popup when clicked.
        popupView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePopup.dismiss();
            }
        });

        final ImageView imageView = (ImageView) popupView.findViewById(R.id.chat_sdk_popup_image_imageview);
        final ProgressBar progressBar = (ProgressBar) popupView.findViewById(R.id.chat_sdk_popup_image_progressbar);

        switch (loadingType)
        {
            case LOAD_FROM_BASE64:
                if (DEBUG) Log.i(TAG, "Image is Base64");
                progressBar.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                imageView.startAnimation(AnimationUtils.loadAnimation(context, android.R.anim.fade_in));
                imageView.setImageBitmap(ImageUtils.decodeFrom64(data.getBytes()));
                break;

            case LOAD_FROM_URL:
                if (DEBUG) Log.i(TAG, "Image from URL");
                if (data != null && !data.equals(""))
                    VolleyUtills.getImageLoader().get(data, new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(final ImageLoader.ImageContainer response, boolean isImmediate) {
                            if (response.getBitmap() != null)
                            {
                                imageView.setImageBitmap(response.getBitmap());
                                if (DEBUG) Log.i(TAG, "response");
                                imageView.startAnimation(AnimationUtils.loadAnimation(context, android.R.anim.fade_in));
                                imageView.getAnimation().setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {
                                        progressBar.setVisibility(View.GONE);
                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        imageView.setVisibility(View.VISIBLE);
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressBar.setVisibility(View.GONE);

                            SuperToast superToast = new SuperToast(context);
                            superToast.setDuration(SuperToast.Duration.MEDIUM);
                            superToast.setBackground(SuperToast.Background.RED);
                            superToast.setTextColor(Color.WHITE);
                            superToast.setAnimations(SuperToast.Animations.FLYIN);
                            superToast.setText("Error while loading");
                            superToast.show();

                            imagePopup.dismiss();
                        }
                    });
                break;

            case LOAD_FROM_PATH:
                progressBar.setVisibility(View.GONE);
                ImageUtils.loadBitmapFromFile(data);
                break;
        }


        // TODO fix popup size to wrap view size.
        imagePopup.setContentView(popupView);
        imagePopup.setBackgroundDrawable(new BitmapDrawable());
        imagePopup.setOutsideTouchable(true);
//        imagePopup.setWidth(500);
//        imagePopup.setHeight(400);

        return imagePopup;
    }

    /** Basic interface for getting callback from the dialog.*/
    public interface DialogInterface<T>{
        public void onFinished(T t);
    }
}
