package app.xmpp.adapter.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.cardview.widget.CardView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import app.xmpp.adapter.R;
import app.xmpp.adapter.XMPPManager;
import app.xmpp.adapter.utils.ServerKeyStorage;
import app.xmpp.adapter.utils.XMPPServer;
import butterknife.BindView;
import sdk.chat.ui.fragments.BaseFragment;
import io.reactivex.annotations.NonNull;
import sdk.chat.core.utils.Device;
import sdk.chat.core.utils.StringChecker;

public class XMPPConfigureFragment extends BaseFragment {

    TextInputEditText addressTextInput;
    TextInputLayout addressTextInputLayout;
    TextInputEditText domainTextInput;
    TextInputLayout domainTextInputLayout;
    TextInputEditText portTextInput;
    TextInputLayout portTextInputLayout;
    TextInputEditText resourceTextInput;
    TextInputLayout resourceTextInputLayout;
    CardView cardView;

    ServerKeyStorage keyStorage;

    @Override
    protected int getLayout() {
        return R.layout.fragment_xmpp_configure;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        addressTextInput = view.findViewById(R.id.addressTextInput);
        addressTextInputLayout = view.findViewById(R.id.addressTextInputLayout);
        domainTextInput = view.findViewById(R.id.domainTextInput);
        domainTextInputLayout = view.findViewById(R.id.domainTextInputLayout);
        portTextInput = view.findViewById(R.id.portTextInput);
        portTextInputLayout = view.findViewById(R.id.portTextInputLayout);
        resourceTextInput = view.findViewById(R.id.resourceTextInput);
        resourceTextInputLayout = view.findViewById(R.id.resourceTextInputLayout);
        cardView = view.findViewById(R.id.cardView);

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
                resourceTextInput.setText(Device.name());
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
