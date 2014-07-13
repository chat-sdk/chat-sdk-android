package com.braunster.chatsdk.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Utils;
import com.braunster.chatsdk.Utils.volley.VolleyUtills;
import com.braunster.chatsdk.activities.LoginActivity;
import com.braunster.chatsdk.dao.BMetadata;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BFacebookManager;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.firebase.BFirebaseInterface;
import com.braunster.chatsdk.object.BError;
import com.facebook.model.GraphUser;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by itzik on 6/17/2014.
 */
public class ProfileFragment extends BaseFragment implements TextView.OnEditorActionListener{

    private static final int PHOTO_PICKER_ID = 100;

    private static final String TAG = ProfileFragment.class.getSimpleName();
    private static boolean DEBUG = true;

    private static final String S_I_F_NAME = "saved_name_flag";
    private static final String S_I_F_PHONE = "saved_phones_flag";
    private static final String S_I_F_EMAIL = "saved_email_flag";
    private static final String S_I_F_PROFILE = "saved_profile_flag";

    private static final String S_I_D_NAME = "saved_name_data";
    private static final String S_I_D_PHONE = "saved_phones_data";
    private static final String S_I_D_EMAIL = "saved_email_data";

    private EditText etName, etMail, etPhone;
    private boolean isNameTouched = false, isEmailTouched = false, isPhoneTouched = false, isProfilePicChanged;

//    private ProfilePictureView profilePictureView;
    private CircleImageView profileCircleImageView;

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

        initViews();

        loginType = (Integer) BNetworkManager.sharedManager().getNetworkAdapter().getLoginInfo().get(BDefines.Prefs.AccountTypeKey);

        if (savedInstanceState == null)
        {
            loadData();
        }
        else
        {
            isEmailTouched = savedInstanceState.getBoolean(S_I_F_EMAIL);
            isNameTouched = savedInstanceState.getBoolean(S_I_F_NAME);
            isPhoneTouched = savedInstanceState.getBoolean(S_I_F_PHONE);
            isProfilePicChanged = savedInstanceState.getBoolean(S_I_F_PROFILE);

            setDetails(loginType, savedInstanceState);
        }

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
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        if (DEBUG) Log.d(TAG, "onResume");
        super.onResume();

        //region Listening to text changes.
        etName.setOnEditorActionListener(this);
        etPhone.setOnEditorActionListener(this);
        etMail.setOnEditorActionListener(this);

        etMail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
//                Log.e(TAG, "onClick");
//                if (v.getId() == R.id.chat_sdk_et_name)
//                    isNameTouched = true;
//                else if (v.getId() == R.id.chat_sdk_et_mail)
//                    isEmailTouched = true;
//                else if (v.getId() == R.id.chat_sdk_et_phone_number)
//                    isPhoneTouched = true;
                isEmailTouched = true;
            }
        });
        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                isNameTouched = true;
            }
        });
        etPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {;
            }

            @Override
            public void afterTextChanged(Editable s) {
                isPhoneTouched = true;
            }
        });
        //endregion

        // Long click will open the gallery so the user can change is picture.
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
    public void loadData() {
        super.loadData();
        setDetails((Integer) BNetworkManager.sharedManager().getNetworkAdapter().getLoginInfo().get(BDefines.Prefs.AccountTypeKey));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(S_I_F_NAME, isNameTouched);
        outState.putBoolean(S_I_F_EMAIL, isEmailTouched);
        outState.putBoolean(S_I_F_PHONE, isPhoneTouched);
        outState.putBoolean(S_I_F_PROFILE, isProfilePicChanged);

        outState.putString(S_I_D_NAME, etName.getText().toString());
        outState.putString(S_I_D_EMAIL, etMail.getText().toString());
        outState.putString(S_I_D_PHONE, etPhone.getText().toString());
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
                        saveProfilePicToParse(image.getPath());
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


    /** Fetching the user details from the user's metadata.*/
    private void setDetails(int loginType){
        if (etName == null)
        {
            initViews();
        }

        BUser user = BNetworkManager.sharedManager().getNetworkAdapter().currentUser();
        etName.setText(user.getMetaName());
        etPhone.setText(user.metaStringForKey(BDefines.Keys.BPhone));
        etMail.setText(user.getMetaEmail());

        setProfilePic(loginType);
    }

    private void setDetails(int loginType, Bundle bundle){
        etName.setText(bundle.getString(S_I_D_NAME));
        etPhone.setText(bundle.getString(S_I_D_PHONE));
        etMail.setText(bundle.getString(S_I_D_EMAIL));

        setProfilePic(loginType);
    }

    private void setProfilePic(int loginType){
        switch (loginType)
        {
            case BDefines.BAccountType.Facebook:
                getProfileFromFacebook();
                break;

            case BDefines.BAccountType.Password:
                notFacebookLogin();
                // Use facebook profile picture only if has no other picture saved.
//                setProfilePic(BNetworkManager.sharedManager().getNetworkAdapter().currentUser().getMetaPicture());
                setProfilePicFromURL(BNetworkManager.sharedManager().getNetworkAdapter().currentUser().metaStringForKey(BDefines.Keys.BPictureURL));
                break;

            case BDefines.BAccountType.Twitter:
                break;
        }
    }

    private void setProfilePicFromURL(String url){
        // Set default.
        if (url == null)
        {
            profileCircleImageView.setImageResource(R.drawable.icn_user_x_2);
            return;
        }

        VolleyUtills.getImageLoader().get(url, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(final ImageLoader.ImageContainer response, boolean isImmediate) {
                if (response.getBitmap() != null) {
                    // load image into imageview
                    final int size = mainView.findViewById(R.id.frame_profile_image_container).getMeasuredHeight();

                    // If the size of the container is 0 we will wait for the view to do onLayout and only then measure it.
                    if (size == 0)
                    {
                        profileCircleImageView.post(new Runnable() {
                            @Override
                            public void run() {
                                int size = mainView.findViewById(R.id.frame_profile_image_container).getMeasuredHeight();
                                profileCircleImageView.setImageBitmap(scaleImage(response.getBitmap(), size));
                            }
                        });
                    } else profileCircleImageView.setImageBitmap(scaleImage(response.getBitmap(), size));
                }
            }


            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Image Load Error: " + error.getMessage());
            }
        });
    }

    private void saveProfilePicToParse(String path) {
        //  Loading the bitmap
        final Bitmap b = Utils.loadBitmapFromFile(path);

        // Converting file to a JPEG and then to byte array.
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        profileCircleImageView.setVisibility(View.VISIBLE);

        // Saving the image to parse.
        final BUser currentUser = BNetworkManager.sharedManager().getNetworkAdapter().currentUser();
        final ParseFile parseFile = new ParseFile(currentUser.getEntityID().replace("-","") + ".jpeg", byteArray);

        int size = mainView.findViewById(R.id.frame_profile_image_container).getMeasuredHeight();

        // If the size of the container is 0 we will wait for the view to do onLayout and only then measure it.
        if (size == 0)
        {
            profileCircleImageView.post(new Runnable() {
                @Override
                public void run() {
                    int size = mainView.findViewById(R.id.frame_profile_image_container).getMeasuredHeight();
                    profileCircleImageView.setImageBitmap(scaleImage(b, size));
                }
            });
        } else profileCircleImageView.setImageBitmap(scaleImage(b, size));

        // When save is done save the image url in the user metadata.
        parseFile.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null)
                {
                    if (DEBUG) Log.e(TAG, "Parse Exception while saving profile pic: " + parseFile.getName() + " --- " + e.getMessage());
                    return;
                }

                isProfilePicChanged = true;
                currentUser.setMetadataString(BDefines.Keys.BPictureURL, parseFile.getUrl());

                updateProfileIfNeeded();
            }
        });
    }

    private void notFacebookLogin(){
        mainView.findViewById(R.id.chat_sdk_facebook_button).setVisibility(View.GONE);
        mainView.findViewById(R.id.chat_sdk_logout_button).setVisibility(View.VISIBLE);
    }

    private void getProfileFromFacebook(){
        // Use facebook profile picture only if has no other picture saved.
        String imageUrl = BNetworkManager.sharedManager().getNetworkAdapter().currentUser().getMetaPictureUrl();

        if (imageUrl != null)
            setProfilePicFromURL(imageUrl);
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
            }
            else if (v.getId() == R.id.chat_sdk_et_mail){
                isEmailTouched = true;
            }
            else if (v.getId() == R.id.chat_sdk_et_phone_number){
                isPhoneTouched = true;
            }
        }

        updateProfileIfNeeded();

        return false;
    }

    public void updateProfileIfNeeded(){
        boolean toUpdate = isEmailTouched || isPhoneTouched || isNameTouched || isProfilePicChanged;

        Log.v(TAG, "updateProfileIfNeeded, toUpdate: " + toUpdate);

        Log.v(TAG, "updateProfileIfNeeded " + (isEmailTouched?",Email ":"") + (isNameTouched?",Name ":"") + (isPhoneTouched?",Phone ":"") + (isProfilePicChanged?",Pic Changed":""));

//        if (!toUpdate)
//            return;

        List<BMetadata> metadataToPush = new ArrayList<BMetadata>();

        // The current user.
        final BUser bUser =BNetworkManager.sharedManager().getNetworkAdapter().currentUser();

        if (isPhoneTouched)
        {
            String phone = etPhone.getText().toString();
            String curPhone = bUser.metaStringForKey(BDefines.Keys.BPhone);

            if (!phone.equals(curPhone))
            {
                bUser.setMetadataString(BDefines.Keys.BPhone, phone);
                metadataToPush.add(bUser.fetchOrCreateMetadataForKey(BDefines.Keys.BPhone, BMetadata.Type.STRING));
                indexUser(bUser, curPhone, phone);
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
                metadataToPush.add(bUser.fetchOrCreateMetadataForKey(BDefines.Keys.BName, BMetadata.Type.STRING));
                indexUser(bUser, curName, name);
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
                metadataToPush.add(bUser.fetchOrCreateMetadataForKey(BDefines.Keys.BEmail, BMetadata.Type.STRING));
                indexUser(bUser, curEmail, email);
            }

            isEmailTouched = false;
        }

        if (isProfilePicChanged)
        {
//            metadataToPush.add(bUser.fetchOrCreateMetadataForKey(BDefines.Keys.BPicture, BMetadata.Type.IMAGE));
            metadataToPush.add(bUser.fetchOrCreateMetadataForKey(BDefines.Keys.BPictureURL, BMetadata.Type.STRING));
            isProfilePicChanged = false;
        }

        // Push the changed metadata.
        for (BMetadata metadata : metadataToPush)
            BFirebaseInterface.pushEntity(metadata, null);
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



/*    private void setProfilePic(final Bitmap bitmap){
        if (bitmap != null)
        {
            profileCircleImageView.post(new Runnable() {
                @Override
                public void run() {
                    profileCircleImageView.setVisibility(View.INVISIBLE);
                    int size = mainView.findViewById(R.id.frame_profile_image_container).getHeight();

                    setScaledPicToView(bitmap, size, profileCircleImageView);

                    profileCircleImageView.setVisibility(View.VISIBLE);
                }
            });
        }
    }*/

    /*    private void setProfilePic(final String path){
            profileCircleImageView.post(new Runnable() {
                @Override
                public void run() {
                    isProfilePicChanged = true;

                    int size = mainView.findViewById(R.id.frame_profile_image_container).getHeight();

                    Bitmap b = setScaledPicToView(path, size, profileCircleImageView);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    b.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    profileCircleImageView.setVisibility(View.VISIBLE);

                    final BUser currentUser = BNetworkManager.sharedManager().getNetworkAdapter().currentUser();
                    final ParseFile parseFile = new ParseFile(currentUser.getEntityID().replace("-","") + ".jpeg", byteArray);

                    parseFile.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null)
                                if (DEBUG) Log.e(TAG, "Parse Exception while saving profile pic: " + parseFile.getName() + " --- " + e.getMessage());
                            currentUser.setMetadataString(BDefines.Keys.BPictureURL, parseFile.getUrl());
                        }
                    });

    //                currentUser.setMetaPicture(((BitmapDrawable) profileCircleImageView.getDrawable()).getBitmap());
                }
            });
        }*/