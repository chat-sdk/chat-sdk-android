package com.braunster.chatsdk.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Debug;
import com.braunster.chatsdk.Utils.ImageUtils;
import com.braunster.chatsdk.Utils.Utils;
import com.braunster.chatsdk.Utils.volley.VolleyUtills;
import com.braunster.chatsdk.activities.LoginActivity;
import com.braunster.chatsdk.activities.MainActivity;
import com.braunster.chatsdk.dao.BMetadata;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BFacebookManager;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.TwitterManager;
import com.braunster.chatsdk.network.firebase.BFirebaseInterface;
import com.braunster.chatsdk.object.BError;
import com.braunster.chatsdk.parse.ParseUtils;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.parse.ParseException;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by itzik on 6/17/2014.
 */
public class ProfileFragment extends BaseFragment implements TextView.OnEditorActionListener{

    private static final int PHOTO_PICKER_ID = 100;

    private static final String TAG = ProfileFragment.class.getSimpleName();
    private static boolean DEBUG = Debug.ProfileFragment;

    private static final String S_I_F_NAME = "saved_name_flag";
    private static final String S_I_F_PHONE = "saved_phones_flag";
    private static final String S_I_F_EMAIL = "saved_email_flag";
    private static final String S_I_F_PROFILE = "saved_profile_flag";

    private static final String S_I_D_NAME = "saved_name_data";
    private static final String S_I_D_PHONE = "saved_phones_data";
    private static final String S_I_D_EMAIL = "saved_email_data";

    private static final String LOGIN_TYPE = "login_type";

    private EditText etName, etMail, etPhone;
    private boolean isNameTouched = false, isEmailTouched = false, isPhoneTouched = false, isProfilePicChanged;

    private Bundle savedState;
    private CircleImageView profileCircleImageView;
    private ProgressBar progressBar;

    private Integer loginType;

    public static ProfileFragment newInstance() {
        ProfileFragment f = new ProfileFragment();
        Bundle b = new Bundle();
        f.setArguments(b);
        f.setRetainInstance(true);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        savedState = savedInstanceState;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (DEBUG) Log.d(TAG, "onCreateView");

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        initViews(inflater);

        initToast();

        loginType = (Integer) BNetworkManager.sharedManager().getNetworkAdapter().getLoginInfo().get(BDefines.Prefs.AccountTypeKey);

        if (savedState != null)
        {
            Log.d(TAG, "Saved State is not null");
            isEmailTouched = savedState.getBoolean(S_I_F_EMAIL);
            isNameTouched = savedState.getBoolean(S_I_F_NAME);
            isPhoneTouched = savedState.getBoolean(S_I_F_PHONE);
            isProfilePicChanged = savedState.getBoolean(S_I_F_PROFILE);

            setDetails(loginType, savedState);
        }
        else if (savedInstanceState != null)
        {
            Log.d(TAG, "Saved instance is not null");
            isEmailTouched = savedInstanceState.getBoolean(S_I_F_EMAIL);
            isNameTouched = savedInstanceState.getBoolean(S_I_F_NAME);
            isPhoneTouched = savedInstanceState.getBoolean(S_I_F_PHONE);
            isProfilePicChanged = savedInstanceState.getBoolean(S_I_F_PROFILE);

            setDetails(loginType, savedInstanceState);
        }
        else
        {
            Log.d(TAG, "Saved instance is null");
            loadData();
        }

        return mainView;
    }

    public void initViews(LayoutInflater inflater){
        if (inflater != null)
            mainView = inflater.inflate(R.layout.chat_sdk_activity_profile, null);
        else return;

        setupTouchUIToDismissKeyboard(mainView, R.id.chat_sdk_circle_ing_profile_pic);

        // Changing the weight of the view according to orientation.
        // This will make sure (hopefully) there is enough space to show the views in landscape mode.
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE){
            if (DEBUG) Log.d(TAG, "Landscape");
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mainView.findViewById(R.id.linear).getLayoutParams();
            layoutParams.weight = 3;
            mainView.findViewById(R.id.linear).setLayoutParams(layoutParams);
        }
        else
        {
            if (DEBUG) Log.d(TAG, "Portrait");
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mainView.findViewById(R.id.linear).getLayoutParams();
            layoutParams.weight = 2;
            mainView.findViewById(R.id.linear).setLayoutParams(layoutParams);
        }

        progressBar = (ProgressBar) mainView.findViewById(R.id.chat_sdk_progressbar);
        etName = (EditText) mainView.findViewById(R.id.chat_sdk_et_name);
        etMail = (EditText) mainView.findViewById(R.id.chat_sdk_et_mail);
        etPhone = (EditText) mainView.findViewById(R.id.chat_sdk_et_phone_number);
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
                if (DEBUG) Log.e(TAG, "After text changed");
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
        profileCircleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickIntent();
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
/*        if (loginType != BDefines.BAccountType.Facebook && loginType != BDefines.BAccountType.Twitter)
            mainView.findViewById(R.id.chat_sdk_logout_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Logout and return to the login activity.
                    BNetworkManager.sharedManager().getNetworkAdapter().logout();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.putExtra(LoginActivity.FLAG_LOGGED_OUT, true);
                    startActivity(intent);
                }
            });*/
    }

    @Override
    public void loadData() {
        super.loadData();
        setDetails((Integer) BNetworkManager.sharedManager().getNetworkAdapter().getLoginInfo().get(BDefines.Prefs.AccountTypeKey));
    }

    @Override
    public void clearData() {
        super.clearData();

        if (mainView != null)
        {
            etName.getText().clear();
            etMail.getText().clear();
            etPhone.getText().clear();

            profileCircleImageView.setImageResource(R.drawable.ic_action_user);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (DEBUG) Log.v(TAG, "onSaveInstanceState");
        //http://stackoverflow.com/a/15314508/2568492
        if (mainView == null)
        {
            if (savedState == null)
                return;

            if (DEBUG) Log.v(TAG, "onSaveInstanceState, Saving from saved state");
            outState.putBoolean(S_I_F_NAME, savedState.getBoolean(S_I_F_NAME));
            outState.putBoolean(S_I_F_EMAIL, savedState.getBoolean(S_I_F_EMAIL));
            outState.putBoolean(S_I_F_PHONE, savedState.getBoolean(S_I_F_PHONE));
            outState.putBoolean(S_I_F_PROFILE, savedState.getBoolean(S_I_F_PROFILE));

            outState.putString(S_I_D_NAME, savedState.getString(S_I_D_NAME) );
            outState.putString(S_I_D_EMAIL, savedState.getString(S_I_D_EMAIL));
            outState.putString(S_I_D_PHONE, savedState.getString(S_I_D_PHONE));
            outState.putInt(LOGIN_TYPE, savedState.getInt(LOGIN_TYPE));

            savedState = null;
            return;
        }

        if (DEBUG) Log.v(TAG, "onSaveInstanceState, saving from local data.");
        outState.putBoolean(S_I_F_NAME, isNameTouched);
        outState.putBoolean(S_I_F_EMAIL, isEmailTouched);
        outState.putBoolean(S_I_F_PHONE, isPhoneTouched);
        outState.putBoolean(S_I_F_PROFILE, isProfilePicChanged);

        outState.putString(S_I_D_NAME, etName.getText().toString());
        outState.putString(S_I_D_EMAIL, etMail.getText().toString());
        outState.putString(S_I_D_PHONE, etPhone.getText().toString());

        outState.putInt(LOGIN_TYPE, savedState.getInt(LOGIN_TYPE));
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
                        showToast("Unable to fetch image");
                        return;
                    }

                    if (image != null) {
                        if (DEBUG) Log.i(TAG, "Image is not null");
                        saveProfilePicToParse(image.getPath(), true);
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item =
                menu.add(Menu.NONE, R.id.action_chat_sdk_logout, 12, "Logout");
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        item.setIcon(R.drawable.icon_light_exit);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        /* Cant use switch in the library*/
        int id = item.getItemId();

        if (id == R.id.action_chat_sdk_logout)
        {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*############################################*/
    /* UI*/
    /** Fetching the user details from the user's metadata.*/
    private void setDetails(int loginType){
        if (mainView == null || getActivity() == null)
        {
            return;
        }

        BUser user = BNetworkManager.sharedManager().getNetworkAdapter().currentUser();
        etName.setText(user.getMetaName());
        etPhone.setText(user.metaStringForKey(BDefines.Keys.BPhone));
        etMail.setText(user.getMetaEmail());

        loadProfilePic(loginType);
    }

    private void setDetails(int loginType, Bundle bundle){
        etName.setText(bundle.getString(S_I_D_NAME));
        etPhone.setText(bundle.getString(S_I_D_PHONE));
        etMail.setText(bundle.getString(S_I_D_EMAIL));

        loadProfilePic(loginType);
    }

    private void loadProfilePic(int loginType){
        profileCircleImageView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        switch (loginType)
        {
            case BDefines.BAccountType.Facebook:
                getProfileFromFacebook();
                break;

            case BDefines.BAccountType.Password:
                setProfilePicFromURL(BNetworkManager.sharedManager().getNetworkAdapter().currentUser().metaStringForKey(BDefines.Keys.BPictureURL));
                break;

            case BDefines.BAccountType.Anonymous:
                setInitialsProfilePic(BDefines.InitialsForAnonymous);

            case BDefines.BAccountType.Twitter:
                getProfileFromTwitter();
                break;
        }
    }

    private void setProfilePic(final Bitmap bitmap){
        if (DEBUG) Log.v(TAG, "setProfilePic, Width: " + bitmap.getWidth() + ", Height: " + bitmap.getHeight());
        // load image into imageview
        final int size = mainView.findViewById(R.id.frame_profile_image_container).getMeasuredHeight();

        // If the size of the container is 0 we will wait for the view to do onLayout and only then measure it.
        if (size == 0)
        {
            profileCircleImageView.post(new Runnable() {
                @Override
                public void run() {
                    int size = mainView.findViewById(R.id.frame_profile_image_container).getMeasuredHeight();
                    profileCircleImageView.setImageBitmap(scaleImage(bitmap, size));
                    progressBar.setVisibility(View.GONE);
                    profileCircleImageView.setVisibility(View.VISIBLE);
                }
            });
        } else
        {
            profileCircleImageView.setImageBitmap(scaleImage(bitmap, size));
            progressBar.setVisibility(View.GONE);
            profileCircleImageView.setVisibility(View.VISIBLE);
        }
    }

    private void setProfilePicFromURL(String url){
        // Set default.
        if (url == null)
        {
            setInitialsProfilePic();
            return;
        }

        VolleyUtills.getImageLoader().get(url, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(final ImageLoader.ImageContainer response, boolean isImmediate) {
                if (response.getBitmap() != null) {
                    setProfilePic(response.getBitmap());
                }
            }


            @Override
            public void onErrorResponse(VolleyError error) {
                if (DEBUG) Log.e(TAG, "Image Load Error: " + error.getMessage());
                setInitialsProfilePic();
            }
        });
    }

    private void saveProfilePicToParse(String path, boolean setAsPic) {
        //  Loading the bitmap
        if (setAsPic)
        {
            final Bitmap b = ImageUtils.loadBitmapFromFile(path);

            if (b == null)
            {
                if (DEBUG) Log.e(TAG, "Cant save image to parse file path is invalid");
                return;
            }
            setProfilePic(b);
        }

        saveProfilePicToParse(path);
    }

    private void saveProfilePicToParse(String path){
        ParseUtils.saveImageFileToParseWithThumbnail(path, BDefines.ImageProperties.PROFILE_PIC_THUMBNAIL_SIZE, new ParseUtils.MultiSaveCompletedListener() {
            @Override
            public void onSaved(ParseException exception, String... data) {
                if (exception != null)
                {
                    if (DEBUG) Log.e(TAG, "Parse Exception while saving profile pic --- " + exception.getMessage());
                    return;
                }

                // Saving the image to parse.
                final BUser currentUser = BNetworkManager.sharedManager().getNetworkAdapter().currentUser();

                isProfilePicChanged = true;
                currentUser.setMetadataString(BDefines.Keys.BPictureURL, data[0]);
                currentUser.setMetadataString(BDefines.Keys.BPictureURLThumbnail, data[1]);

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
        {
            // If there isnt any picture url saved we will save the fb profile pic to parse and save the url to the user.
            BFacebookManager.getUserDetails(new CompletionListenerWithData<GraphUser>() {
                @Override
                public void onDone(GraphUser graphUser) {
                    Log.d(TAG, "Name: " + graphUser.getName());

                    VolleyUtills.getImageLoader().get(BFacebookManager.getPicUrl(graphUser.getId()),
                            new ImageLoader.ImageListener() {
                                @Override
                                public void onResponse(final ImageLoader.ImageContainer response, boolean isImmediate) {
                                    if (response.getBitmap() != null) {
                                        setProfilePic(response.getBitmap());

                                        createTempFileAndSave(response.getBitmap());
                                    }
                                }

                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    setInitialsProfilePic();
                                    showToast("Unable to load user profile pic.");
                                }
                            });
                };

                @Override
                public void onDoneWithError(BError error) {
                    setInitialsProfilePic();
                    showToast("Unable to fetch user details from fb" + (error != null ? error.code == BError.Code.EXCEPTION ? error.message : "" : ""));
                }
            });
        }
    }

    private void getProfileFromTwitter(){
        // Use facebook profile picture only if has no other picture saved.
        String savedUrl = BNetworkManager.sharedManager().getNetworkAdapter().currentUser().getMetaPictureUrl();

        if (StringUtils.isNotEmpty(savedUrl))
            setProfilePicFromURL(savedUrl);
        else {
            String imageUrl = TwitterManager.profileImageUrl;
            if (DEBUG) Log.d(TAG, "Image URL: " + imageUrl);
            if (StringUtils.isNotEmpty(imageUrl))
            {
                // The default image suppied by twitter is 48px on 48px image so we want a bigget one.
                imageUrl = imageUrl.replace("_normal", "");
                VolleyUtills.getImageLoader().get(imageUrl,
                        new ImageLoader.ImageListener() {
                            @Override
                            public void onResponse(final ImageLoader.ImageContainer response, boolean isImmediate) {
                                if (response.getBitmap() != null) {

                                    setProfilePic(response.getBitmap());

                                    createTempFileAndSave(response.getBitmap());
                                }
                            }

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                setInitialsProfilePic();
                                showToast("Unable to load user profile pic.");
                            }
                        }
                );
            }
        }
    }

    private void setInitialsProfilePic(){

        String initials = "";

        String name = BNetworkManager.sharedManager().getNetworkAdapter().currentUser().getMetaName();
        if (DEBUG) Log.v(TAG, "setInitialsProfilePic, Name: " + name);

        if (StringUtils.isEmpty(name))
            initials = BDefines.InitialsForAnonymous;
        else
        {
            String[] splited = name.split("\\s+");
            if (splited.length == 1)
                initials = String.valueOf(name.toUpperCase().charAt(0));
            else if (splited.length >= 2)
                initials = String.valueOf(splited[0].toUpperCase().charAt(0)) + String.valueOf(splited[1].toUpperCase().charAt(0));
            else initials = BDefines.InitialsForAnonymous;
        }

        setInitialsProfilePic(initials);
    }

    private void setInitialsProfilePic(final String initials) {
        if (DEBUG) Log.v(TAG, "setInitialsProfilePic, Initials: " + initials);
        Bitmap bitmap = ImageUtils.getInitialsBitmap(Color.GRAY, Color.BLACK, initials );
        setProfilePic(bitmap);
        createTempFileAndSave(bitmap);
    }

    private boolean createTempFileAndSave(Bitmap bitmap){
        // Saving the image to tmp file.
        try {
            File tmp = File.createTempFile("Pic", ".jpg", getActivity().getCacheDir());
            ImageUtils.saveBitmapToFile(tmp, bitmap);
            if (DEBUG) Log.i(TAG, "Temp file path: " + tmp.getPath());
            saveProfilePicToParse(tmp.getPath(), false);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void logout(){
        // Logout and return to the login activity.

        if (loginType == BDefines.BAccountType.Facebook)
        {
            if (Session.getActiveSession() != null)
            {
                Session.getActiveSession().closeAndClearTokenInformation();
            }
            else
            {
                if (DEBUG) Log.e(TAG, "getActiveSessionIsNull");
                Session.openActiveSessionFromCache(getActivity()).closeAndClearTokenInformation();
            }
        }

        Intent logout = new Intent(MainActivity.Action_Logged_Out);
        getActivity().sendBroadcast(logout);

        BNetworkManager.sharedManager().getNetworkAdapter().logout();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.putExtra(LoginActivity.FLAG_LOGGED_OUT, true);
        startActivity(intent);
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

            if (StringUtils.isEmpty(bUser.getMetadataForKey(BDefines.Keys.BPictureURL, BMetadata.Type.STRING).getValue()))
                setInitialsProfilePic();

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
            metadataToPush.add(bUser.fetchOrCreateMetadataForKey(BDefines.Keys.BPictureURLThumbnail, BMetadata.Type.STRING));
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
                showToast("Cant set index.");
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