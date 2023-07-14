package sdk.chat.core.handlers;

import androidx.fragment.app.Fragment;

public interface CallHandler {
    void startCall(Fragment fragment, String userEntityID);
    boolean callEnabled(Fragment fragment, String threadEntityID);

}
