package co.chatsdk.xmpp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.cardview.widget.CardView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import butterknife.BindView;
import co.chatsdk.core.utils.Device;
import co.chatsdk.core.utils.StringChecker;
import co.chatsdk.ui.fragments.BaseFragment;
import co.chatsdk.xmpp.R;
import co.chatsdk.xmpp.R2;
import co.chatsdk.xmpp.XMPPManager;
import co.chatsdk.xmpp.utils.ServerKeyStorage;
import co.chatsdk.xmpp.utils.XMPPServer;
import io.reactivex.annotations.NonNull;

public class XMPPConfigureFragment extends BaseFragment {

    @BindView(R2.id.addressTextInput)
    TextInputEditText addressTextInput;
    @BindView(R2.id.addressTextInputLayout)
    TextInputLayout addressTextInputLayout;
    @BindView(R2.id.domainTextInput)
    TextInputEditText domainTextInput;
    @BindView(R2.id.domainTextInputLayout)
    TextInputLayout domainTextInputLayout;
    @BindView(R2.id.portTextInput)
    TextInputEditText portTextInput;
    @BindView(R2.id.portTextInputLayout)
    TextInputLayout portTextInputLayout;
    @BindView(R2.id.resourceTextInput)
    TextInputEditText resourceTextInput;
    @BindView(R2.id.resourceTextInputLayout)
    TextInputLayout resourceTextInputLayout;
    @BindView(R2.id.cardView)
    CardView cardView;

    ServerKeyStorage keyStorage;

    @Override
    protected int getLayout() {
        return R.layout.fragment_xmpp_configure;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        keyStorage = new ServerKeyStorage(getContext());

        XMPPServer server = XMPPManager.getCurrentServer(getContext());

        // Get old key values
        String address = server.address;
        if (address != null) {
            addressTextInput.setText(address);
        }
        String domain = server.domain;
        if (domain != null) {
            domainTextInput.setText(domain);
        }
        int port = server.port;
        if (port != 0) {
            portTextInput.setText(String.valueOf(port));
        }
        String resource = server.resource;
        if (resource != null) {
            resourceTextInput.setText(resource);
        } else {
            if (getContext() != null) {
                resourceTextInput.setText(Device.name(getContext()));
            }
        }

        return view;
    }

    @Override
    protected void initViews() {

    }

    @Override
    public void clearData() {

    }

    @Override
    public void reloadData() {

    }

    @Override
    public void setTabVisibility (boolean isVisible) {
        super.setTabVisibility(isVisible);
        if (!isVisible) {
            hideKeyboard();
            save();
        }
    }

    public void save() {
        if (!StringChecker.isNullOrEmpty(addressTextInput.getText())) {
            keyStorage.setAddress(addressTextInput.getText().toString());
        }
        if (!StringChecker.isNullOrEmpty(domainTextInput.getText())) {
            keyStorage.setDomain(domainTextInput.getText().toString());
        }
        if (!StringChecker.isNullOrEmpty(portTextInput.getText())) {
            keyStorage.setPort(Integer.parseInt(portTextInput.getText().toString()));
        }
        if (!StringChecker.isNullOrEmpty(resourceTextInput.getText())) {
            keyStorage.setResource(resourceTextInput.getText().toString());
        }
    }

}
