package sdk.chat.ui.keyboard;

import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentTransaction;

import java.lang.ref.WeakReference;

import sdk.chat.core.ui.AbstractKeyboardOverlayFragment;
import sdk.chat.ui.ChatSDKUI;
import sdk.chat.ui.R;
import sdk.chat.ui.fragments.ChatFragment;

public class ChatFragmentKeyboardOverlayHelper {

    WeakReference<ChatFragment> chatFragment;

    protected boolean keyboardOverlayActive = false;

    protected KeyboardOverlayOptionsFragment optionsKeyboardOverlayFragment;
    protected AbstractKeyboardOverlayFragment currentKeyboardOverlayFragment;

    public ChatFragmentKeyboardOverlayHelper(ChatFragment fragment) {
        chatFragment = new WeakReference<>(fragment);
        setupKeyboardListeners();
    }

    protected ChatFragment cf() {
        return chatFragment.get();
    }

    protected void setupKeyboardListeners() {

        keyboardAwareView().keyboardShown = () -> {

            if (!keyboardOverlayActive || keyboardOverlayVisible()) {
                setKeyboardOverlayGone();
            } else {
                setKeyboardOverlayVisible();
            }

            // We want the bottom margin to be just the height of the input + reply view
            setChatViewBottomMargin(bottomMargin());

        };

        keyboardAwareView().keyboardHidden = () -> {

            int bottomMargin = bottomMargin();

            if (keyboardOverlayActive) {
                keyboardOverlay().setVisibility(View.VISIBLE);
                bottomMargin += keyboardAwareView().getKeyboardHeight();
            }

            setChatViewBottomMargin(bottomMargin);
        };

        keyboardAwareView().heightUpdater = height -> {
            setKeyboardOverlayHeight(height);
        };
    }

    public void setCurrentOverlay(AbstractKeyboardOverlayFragment overlay) {

        if (overlay == currentKeyboardOverlayFragment) {
            return;
        }

        // Add the keyboard overlay fragment
        if (activity() != null) {

            FragmentTransaction transaction = activity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.keyboardOverlay, overlay).addToBackStack(null).commit();

            currentKeyboardOverlayFragment = overlay;

            currentKeyboardOverlayFragment.setViewSize(
                    keyboardAwareView().getMeasuredWidth(),
                    keyboardAwareView().getKeyboardHeight(),
                    cf().getContext());

        }
    }

    public void showOverlay(AbstractKeyboardOverlayFragment fragment) {
        if (!keyboardAwareView().isKeyboardOpen()) {
            showKeyboardOverlay();
        } else {
            hideKeyboardAndShowKeyboardOverlay();
        }
        setCurrentOverlay(fragment);
    }

    public void toggle() {

        currentOverlay().setViewSize(
                keyboardAwareView().getMeasuredWidth(),
                keyboardAwareView().getKeyboardHeight(),
                cf().getContext());

        // If the keyboard is hidden and the options overlay is not visible
        if (!keyboardAwareView().isKeyboardOpen()) {
            if (keyboardOverlayVisible()) {
                back();
            } else {
                showKeyboardOverlay();
            }
        } else {
            if (keyboardOverlayVisible()) {
                back();
            } else {
                hideKeyboardAndShowKeyboardOverlay();
            }
        }
    }

    public boolean back() {
        if (keyboardOverlayVisible()) {
            if (currentOverlay() != optionsOverlay()) {
                setCurrentOverlay(optionsOverlay());
            } else {
                hideKeyboardOverlayAndShowKeyboard();
            }
            return true;
        }
        return false;
    }

    public void showOptionsKeyboardOverlay() {
        if (currentKeyboardOverlayFragment == null) {
            optionsKeyboardOverlayFragment = ChatSDKUI.provider().keyboardOverlayOptionsFragment(cf());
        }
        setCurrentOverlay(optionsKeyboardOverlayFragment);
    }

    public void setKeyboardOverlayActive(boolean active) {
        keyboardOverlayActive = active;
    }

    public boolean isKeyboardOverlayActive() {
        return keyboardOverlayActive;
    }

    public void setKeyboardOverlayGone() {
        keyboardOverlayActive = false;
        keyboardOverlay().setVisibility(View.GONE);
    }

    public void setKeyboardOverlayVisible() {
        keyboardOverlay().setVisibility(View.VISIBLE);
    }

    public boolean keyboardOverlayVisible() {
        return keyboardOverlay().getVisibility() == View.VISIBLE;
    }

    public void hideKeyboardOverlayAndShowKeyboard() {
        keyboardOverlayActive = false;
        cf().showKeyboard();
    }

    public void hideKeyboardAndShowKeyboardOverlay() {
        keyboardOverlayActive = true;
        cf().hideKeyboard();
    }

    public void showKeyboardOverlay() {
        setKeyboardOverlayActive(true);

        int height = keyboardAwareView().getKeyboardHeight();

        setKeyboardOverlayHeight(height);
        setKeyboardOverlayVisible();
        setChatViewBottomMargin(bottomMargin() + height);
    }

    public void setKeyboardOverlayHeight(int height) {
        ViewGroup.LayoutParams params = keyboardOverlay().getLayoutParams();
        params.height = height;
        keyboardOverlay().setLayoutParams(params);
    }

    public AbstractKeyboardOverlayFragment currentOverlay() {
        return currentKeyboardOverlayFragment;
    }

    public AbstractKeyboardOverlayFragment optionsOverlay() {
        return optionsKeyboardOverlayFragment;
    }

    // Internal convenience methods

    protected void setChatViewBottomMargin(int margin) {
        cf().setChatViewBottomMargin(margin);
    }

    protected int bottomMargin() {
        return cf().bottomMargin();
    }

    protected KeyboardAwareFrameLayout keyboardAwareView() {
        return cf().getKeyboardAwareView();
    }

    protected FragmentContainerView keyboardOverlay() {
        return cf().getKeyboardOverlay();
    }

    protected FragmentActivity activity() {
        return cf().getActivity();
    }

}
