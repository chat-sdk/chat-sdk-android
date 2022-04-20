package sdk.chat.core.ui;

import android.app.Activity;

import androidx.annotation.Nullable;

public interface KeyboardOverlayHandler {
    void send(Sendable sendable);
    void showOverlay(AbstractKeyboardOverlayFragment fragment);
    @Nullable
    Activity getActivity();
}
