package com.braunster.chatsdk.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Utils;
import com.braunster.chatsdk.Utils.volley.VolleyUtills;
import com.braunster.chatsdk.activities.LoginActivity;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BFacebookManager;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.object.BError;
import com.facebook.model.GraphUser;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by itzik on 6/17/2014.
 */
public class ProfileFragment extends BaseFragment implements TextView.OnEditorActionListener, View.OnClickListener{

    private static final int PHOTO_PICKER_ID = 100;

    private static final String TAG = ProfileFragment.class.getSimpleName();
    private static boolean DEBUG = true;

    private EditText etName, etMail, etPhone;
    private boolean isNameTouched = false, isEmailTouched = false, isPhoneTouched = false;

//    private ProfilePictureView profilePictureView;
    private CircleImageView profileCircleImageView;

    private BUser user;
    private Integer loginType;
    public static ProfileFragment newInstance() {
        ProfileFragment f = new ProfileFragment();
        Bundle b = new Bundle();
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (DEBUG) Log.d(TAG, "onCreateView");
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        mainView = inflater.inflate(R.layout.chat_sdk_activity_profile, null);

        this.user = BNetworkManager.sharedManager().getNetworkAdapter().currentUser();

        initViews();

        loginType = (Integer) BNetworkManager.sharedManager().getNetworkAdapter().getLoginInfo().get(BDefines.Prefs.AccountTypeKey);

        switch (loginType)
        {
            case BDefines.BAccountType.Facebook:
                getDetailsFromFacebook();
                break;

            case BDefines.BAccountType.Password:
                notFacebookLogin();
                // Use facebook profile picture only if has no other picture saved.
                setProfilePic(BNetworkManager.sharedManager().getNetworkAdapter().currentUser().getMetaPicture());
                break;

            case BDefines.BAccountType.Twitter:
                break;

        }

        setDetails();


        return mainView;
    }

    @Override
    public void initViews(){
        etName = (EditText) mainView.findViewById(R.id.chat_sdk_et_name);
        etMail = (EditText) mainView.findViewById(R.id.chat_sdk_et_mail);
        etPhone = (EditText) mainView.findViewById(R.id.chat_sdk_et_phone_number);
//        profilePictureView = (ProfilePictureView) mainView.findViewById(R.id.profile_pic);
        profileCircleImageView = (CircleImageView) mainView.findViewById(R.id.chat_sdk_circle_ing_profile_pic);
    }

    @Override
    public void onResume() {
        if (DEBUG) Log.d(TAG, "onResume");
        super.onResume();
        etName.setOnEditorActionListener(this);
        etPhone.setOnEditorActionListener(this);
        etMail.setOnEditorActionListener(this);

        etName.setOnClickListener(this);
        etPhone.setOnClickListener(this);
        etMail.setOnClickListener(this);

        profileCircleImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                pickIntent();
                return false;
            }

            private void pickIntent(){
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
//                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(Intent.createChooser(intent,
                        "Complete action using"), PHOTO_PICKER_ID);
            }
        });

        // Add logout button click logic if not connected using facebook or twitter.
        if (loginType != BDefines.BAccountType.Facebook && loginType != BDefines.BAccountType.Twitter)
            mainView.findViewById(R.id.chat_sdk_logout_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Logout and return to the login activity.
                    BNetworkManager.sharedManager().getNetworkAdapter().logout();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                }
            });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null)
        {
            if (DEBUG) Log.e(TAG, "onActivityResult, Intent is null");
            return;
        }

        if (DEBUG) Log.v(TAG, "onActivityResult");

        if (requestCode == PHOTO_PICKER_ID)
        {
            switch (resultCode)
            {
                case Activity.RESULT_OK:
                    if (DEBUG) Log.d(TAG, "Result OK");
                    Uri uri = (Uri) data.getData();
                    File image = null;
                    try
                    {
                        image = Utils.getFile(getActivity(), uri);
                    }
                    catch (NullPointerException e){
                        if (DEBUG) Log.e(TAG, "Null pointer when getting file.");
                        Toast.makeText(getActivity(), "Unable to fetch image", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (image != null) {
                        if (DEBUG) Log.i(TAG, "Image is not null");
                        BNetworkManager.sharedManager().getNetworkAdapter().currentUser().setMetaPicture(image);
                        setProfilePic(BitmapFactory.decodeFile(image.getPath()));
                        BNetworkManager.sharedManager().getNetworkAdapter().pushUserWithCallback(null);
                    }
                    else if (DEBUG) Log.e(TAG, "Image is null");

                    break;

                case Activity.RESULT_CANCELED:
                    if (DEBUG) Log.d(TAG, "Result Canceled");
                    break;

                default:
                    if (DEBUG) Log.d(TAG, "Default");
                    break;
            }
        }
    }

    /*############################################*/
    /* UI*/
    private void getDetailsFromFacebook(){
        // Use facebook profile picture only if has no other picture saved.
        Bitmap bitmap = BNetworkManager.sharedManager().getNetworkAdapter().currentUser().getMetaPicture();

        if (bitmap != null)
            setProfilePic(bitmap);
        else
            BFacebookManager.getUserDetails(new CompletionListenerWithData<GraphUser>() {
                @Override
                public void onDone(GraphUser graphUser) {
                    Log.d(TAG, "Name: " + graphUser.getName());
    //                etName.setText(graphUser.getName());
    //                etMail.setText((String) graphUser.getProperty("email"));
    //                //                profilePictureView.setProfileId(graphUser.getId());

                    profileCircleImageView.setImageResource(0);
                    VolleyUtills.getImageLoader().get(BFacebookManager.getPicUrl(graphUser.getId()),
                            VolleyUtills.getImageLoader().getImageListener(profileCircleImageView,
                                    R.drawable.icn_user_x_2, android.R.drawable.stat_notify_error)
                    );
                }

                @Override
                public void onDoneWithError(BError error) {
                    Toast.makeText(getActivity(), "Unable to fetch user details from fb", Toast.LENGTH_SHORT).show();
                }
            });
    }

    /** Fetching the user details from the user's metadata.*/
    private void setDetails(){
        BUser user = BNetworkManager.sharedManager().getNetworkAdapter().currentUser();
        etName.setText(user.getMetaName());
        etPhone.setText(user.metaStringForKey(BDefines.Keys.BPhone));
        etMail.setText(user.getMetaEmail());
    }

    private void notFacebookLogin(){
        mainView.findViewById(R.id.chat_sdk_facebook_button).setVisibility(View.GONE);
        mainView.findViewById(R.id.chat_sdk_logout_button).setVisibility(View.VISIBLE);
    }

    private void setProfilePic(Bitmap bitmap){
        if (bitmap != null)
            profileCircleImageView.setImageBitmap(bitmap);
    }
    /*############################################*/

    @Override
    public boolean onEditorAction(final TextView v, int actionId, KeyEvent event) {
        /* Logic:
        *  First we will remove the index from the index list, To find the index we are using the old metadata value.
        *  After it is deleted we update the metadata locally and then push the user back to the server.*/
        if (actionId == EditorInfo.IME_ACTION_DONE)
        {
            if (v.getId() == R.id.chat_sdk_et_name){
                isNameTouched = true;
                updateIndexAndMetadata();
            }
            else if (v.getId() == R.id.chat_sdk_et_mail){
                isEmailTouched = true;
                updateIndexAndMetadata();
            }
            else if (v.getId() == R.id.chat_sdk_et_phone_number){
                isPhoneTouched = true;
                updateIndexAndMetadata();
            }
        }

        return false;
    }

    @Override
    public void onClick(View v) {
        Log.e(TAG, "onClick");
        if (v.getId() == R.id.chat_sdk_et_name)
            isNameTouched = true;
        else if (v.getId() == R.id.chat_sdk_et_mail)
            isEmailTouched = true;
        else if (v.getId() == R.id.chat_sdk_et_phone_number)
            isPhoneTouched = true;
    }

    public void updateIndexAndMetadata(){
        // The current user.
        final BUser bUser =BNetworkManager.sharedManager().getNetworkAdapter().currentUser();

        boolean toUpdate = isEmailTouched || isPhoneTouched || isNameTouched;

        if (isPhoneTouched)
        {
            String phone = etPhone.getText().toString();
            String curPhone = bUser.metaStringForKey(BDefines.Keys.BPhone);

            if (!phone.equals(curPhone))
            {
                bUser.setMetadataString(BDefines.Keys.BPhone, phone);
                indexUser(user, curPhone, phone);
            }

            isPhoneTouched = false;
        }

        if (isNameTouched)
        {
            String name = etName.getText().toString();
            String curName = bUser.getMetaName();
            if (!name.equals(curName))
            {
                bUser.setMetaName(name);
                indexUser(user, curName, name);
            }

            isNameTouched = false;
        }

        if (isEmailTouched)
        {
            String email = etMail.getText().toString();
            String curEmail = bUser.getMetaEmail();
            if (!email.equals(curEmail))
            {
                bUser.setMetaEmail(email);
                indexUser(user, curEmail, email);
            }

            isEmailTouched = false;
        }

        // Update the user entity.
        if (toUpdate) BNetworkManager.sharedManager().getNetworkAdapter().pushUserWithCallback(null);
    }

    private void indexUser(final BUser user, final String oldIndex, final String newIndex){
        BNetworkManager.sharedManager().getNetworkAdapter().removeUserFromIndex(user, oldIndex, new CompletionListener() {
            @Override
            public void onDone() {
                BNetworkManager.sharedManager().getNetworkAdapter().addUserToIndex(user, newIndex,null);
            }

            @Override
            public void onDoneWithError() {
                Toast.makeText(getActivity(), "Cant set index.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
