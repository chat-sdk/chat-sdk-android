package com.braunster.chatsdk.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethod;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Utils;
import com.braunster.chatsdk.Utils.volley.VolleyUtills;
import com.braunster.chatsdk.dao.BMessage;
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
public class ProfileFragment extends BaseFragment implements TextView.OnEditorActionListener{

    private static final int PHOTO_PICKER_ID = 100;

    private static final String TAG = ProfileFragment.class.getSimpleName();
    private static boolean DEBUG = true;

    private EditText etName, etMail, etPhone;
//    private ProfilePictureView profilePictureView;
    private CircleImageView profileCircleImageView;

    private BUser user;

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

        mainView = inflater.inflate(R.layout.chat_sdk_activity_profile, null);

        this.user = BNetworkManager.sharedManager().getNetworkAdapter().currentUser();

        initViews();

        Integer loginType = (Integer) BNetworkManager.sharedManager().getNetworkAdapter().getLoginInfo().get(BDefines.Prefs.AccountTypeKey);

        switch (loginType)
        {
            case BDefines.BAccountType.Facebook:
                getDetailsFromFacebook();
                break;

            case BDefines.BAccountType.Password:
                hideFacebookButton();
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
        super.onResume();
        etName.setOnEditorActionListener(this);
        etPhone.setOnEditorActionListener(this);
        etMail.setOnEditorActionListener(this);

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
                        setProfilePic(BNetworkManager.sharedManager().getNetworkAdapter().currentUser().getMetaPicture());
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

    private void hideFacebookButton(){
        mainView.findViewById(R.id.authButton).setVisibility(View.GONE);
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
            // The current user.
            final BUser bUser =BNetworkManager.sharedManager().getNetworkAdapter().currentUser();

            if (v.getId() == R.id.chat_sdk_et_name){
                BNetworkManager.sharedManager().getNetworkAdapter().removeUserFromIndex(bUser, bUser.getMetaName(), new CompletionListener() {
                    @Override
                    public void onDone() {
                        bUser.setMetaName(v.getText().toString());
                        BNetworkManager.sharedManager().getNetworkAdapter().addUserToIndex(bUser, bUser.getMetaName(),null);
                        BNetworkManager.sharedManager().getNetworkAdapter().pushUserWithCallback(null);
                    }

                    @Override
                    public void onDoneWithError() {
                        Toast.makeText(getActivity(), "Cant set name.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else if (v.getId() == R.id.chat_sdk_et_mail){
                BNetworkManager.sharedManager().getNetworkAdapter().removeUserFromIndex(bUser, bUser.getMetaEmail(), new CompletionListener() {
                    @Override
                    public void onDone() {
                        bUser.setMetaEmail(v.getText().toString());
                        BNetworkManager.sharedManager().getNetworkAdapter().addUserToIndex(bUser, bUser.getMetaEmail(),null);
                        BNetworkManager.sharedManager().getNetworkAdapter().pushUserWithCallback(null);
                    }

                    @Override
                    public void onDoneWithError() {
                        Toast.makeText(getActivity(), "Cant set name.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else if (v.getId() == R.id.chat_sdk_et_phone_number){
                BNetworkManager.sharedManager().getNetworkAdapter().removeUserFromIndex(bUser, bUser.metaStringForKey(BDefines.Keys.BPhone), new CompletionListener() {
                    @Override
                    public void onDone() {
                        bUser.setMetadataString(BDefines.Keys.BPhone, v.getText().toString());
                        BNetworkManager.sharedManager().getNetworkAdapter().addUserToIndex(bUser, bUser.metaStringForKey(BDefines.Keys.BPhone),null);
                        BNetworkManager.sharedManager().getNetworkAdapter().pushUserWithCallback(null);
                    }

                    @Override
                    public void onDoneWithError() {
                        Toast.makeText(getActivity(), "Cant set name.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        return false;
    }


}
