package com.braunster.chatsdk.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.dao.BUser;
import com.braunster.chatsdk.interfaces.CompletionListenerWithData;
import com.braunster.chatsdk.network.BFacebookManager;
import com.braunster.chatsdk.network.BNetworkManager;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;

/**
 * Created by itzik on 6/17/2014.
 */
public class ProfileFragment extends BaseFragment {

    private static final String TAG = ProfileFragment.class.getSimpleName();
    private static boolean DEBUG = true;

    private EditText etName, etMail, etPhone;
    private ProfilePictureView profilePictureView;

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

        mainView = inflater.inflate(R.layout.activity_profile, null);

        this.user = BNetworkManager.getInstance().currentUser();

        initViews();

        BFacebookManager.getUserDetails(new CompletionListenerWithData<GraphUser>() {
            @Override
            public void onDone(GraphUser graphUser) {
                Log.d(TAG, "Name: " + graphUser.getName());
                etName.setText(graphUser.getName());
                etMail.setText((String) graphUser.getProperty("email"));
                profilePictureView.setProfileId(graphUser.getId());
            }

            @Override
            public void onDoneWithError() {

            }
        });

        return mainView;
    }

    @Override
    public void initViews(){
        etName = (EditText) mainView.findViewById(R.id.et_name);
        etMail = (EditText) mainView.findViewById(R.id.et_mail);
        etPhone = (EditText) mainView.findViewById(R.id.et_phone_number);
        profilePictureView = (ProfilePictureView) mainView.findViewById(R.id.profile_pic);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
