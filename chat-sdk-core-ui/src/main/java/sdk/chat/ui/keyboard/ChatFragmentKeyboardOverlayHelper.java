package sdk.chat.ui.keyboard;

import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentTransaction;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import sdk.chat.core.session.ChatSDK;
import sdk.chat.core.ui.AbstractKeyboardOverlayFragment;
import sdk.chat.ui.R;
import sdk.chat.ui.fragments.ChatFragment;

public class ChatFragmentKeyboardOverlayHelper {

     public interface Listener {
         void willShowOverlay(String key);
         void didShowOverlay(String key);
         void didHideOverlay(String key);
     }

    protected WeakReference<ChatFragment> chatFragment;

    protected boolean keyboardOverlayActive = false;

    protected KeyboardOverlayOptionsFragment optionsKeyboardOverlayFragment;
    protected AbstractKeyboardOverlayFragment currentKeyboardOverlayFragment;

    protected List<Listener> listeners = new ArrayList<>();

    public ChatFragmentKeyboardOverlayHelper(ChatFragment fragment) {
        chatFragment = new WeakReference<>(fragment);
        setupKeyboardListeners();
    }

    protected ChatFragment cf() {
        return chatFragment.get();
    }

    protected void setupKeyboardListeners() {

        keyboardAwareView().keyboardShownListeners.add(() -> {

            if (!keyboardOverlayActive || keyboardOverlayVisible()) {
                setKeyboardOverlayGone();
            } else {
                setKeyboardOverlayVisible();
            }

            // We want the bottom margin to be just the height of the input + reply view
//            setChatViewBottomMargin(bottomMargin());
            updateChatViewBottomMargin();

        });

        keyboardAwareView().keyboardHiddenListeners.add(() -> {

//            int bottomMargin = bottomMargin();

            if (keyboardOverlayActive) {
                setKeyboardOverlayVisible();
//                keyboardOverlay().setVisibility(View.VISIBLE);
//                bottomMargin += keyboardAwareView().getKeyboardHeight();
            }

            updateChatViewBottomMargin();
//            setChatViewBottomMargin(bottomMargin);
        });

        keyboardAwareView().heightUpdater = height -> {
            setKeyboardOverlayHeight(height);
        };
    }

    public boolean setCurrentOverlay(AbstractKeyboardOverlayFragment overlay) {

        if (overlay == currentKeyboardOverlayFragment) {
            return false;
        }

        // Add the keyboard overlay fragment
        if (activity() != null) {

            if (currentKeyboardOverlayFragment != null && currentKeyboardOverlayFragment.getView() != null) {
                currentKeyboardOverlayFragment.getView().setVisibility(View.INVISIBLE);
            }

            FragmentTransaction transaction = activity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.keyboardOverlay, overlay).addToBackStack(null).commit();

            currentKeyboardOverlayFragment = overlay;

            if (currentKeyboardOverlayFragment.getView() != null) {
                currentKeyboardOverlayFragment.getView().setVisibility(View.VISIBLE);
            }

            currentKeyboardOverlayFragment.setViewSize(
                    keyboardAwareView().getMeasuredWidth(),
                    keyboardAwareView().getKeyboardHeight(),
                    cf().getContext());

        }
        return true;
    }

    public void showOverlay(AbstractKeyboardOverlayFragment fragment) {
        if (setCurrentOverlay(fragment)) {

            for (Listener l: listeners) {
                l.willShowOverlay(fragment.key());
            }

            if (!keyboardAwareView().isKeyboardOpen()) {
                showKeyboardOverlay();
            } else {
                hideKeyboardAndShowKeyboardOverlay();
            }
        }
    }

    public void toggle() {

        boolean overlayDidChange = showOptionsKeyboardOverlay();

        currentOverlay().setViewSize(
                keyboardAwareView().getMeasuredWidth(),
                keyboardAwareView().getKeyboardHeight(),
                cf().getContext());

        // If the keyboard is hidden and the options overlay is not visible
        if (!keyboardAwareView().isKeyboardOpen()) {
            if (keyboardOverlayVisible()) {
                if (!overlayDidChange) {
                    back();
                }
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

//    public void showOptionsKeyboardOverlay() {
//        if (currentKeyboardOverlayFragment == null) {
//            optionsKeyboardOverlayFragment = ChatSDKUI.provider().keyboardOverlayProvider().keyboardOverlayOptionsFragment(cf());
//        }
//        setCurrentOverlay(optionsKeyboardOverlayFragment);
//    }

    public boolean showOptionsKeyboardOverlay() {
        if (optionsKeyboardOverlayFragment == null) {
            optionsKeyboardOverlayFragment = ChatSDK.feather().instance(KeyboardOverlayOptionsFragment.class);
            optionsKeyboardOverlayFragment.setHandler(cf());
        }
        return setCurrentOverlay(optionsKeyboardOverlayFragment);
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
        for (Listener l: listeners) {
            l.didHideOverlay(currentOverlayKey());
        }
    }

    public void setKeyboardOverlayVisible() {
        keyboardOverlay().setVisibility(View.VISIBLE);
        for (Listener l: listeners) {
            l.didShowOverlay(currentOverlayKey());
        }
    }

    public void hideKeyboardOverlay() {
        setKeyboardOverlayGone();
        updateChatViewBottomMargin();
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

        updateChatViewBottomMargin();
//        setChatViewBottomMargin(bottomMargin() + height);
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

//    protected void setChatViewBottomMargin(int margin) {
//        cf().setChatViewBottomMargin(margin);
//    }

    protected void updateChatViewBottomMargin() {
//        cf().setChatViewBottomMargin(margin);
        cf().updateChatViewMargins(false);
    }

//    protected int bottomMargin() {
//        return cf().bottomMargin();
//    }

    protected KeyboardAwareFrameLayout keyboardAwareView() {
        return cf().getKeyboardAwareView();
    }

    protected FragmentContainerView keyboardOverlay() {
        return cf().getKeyboardOverlay();
    }

    protected FragmentActivity activity() {
        return cf().getActivity();
    }

    public void addListener(Listener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public String currentOverlayKey() {
        return currentOverlay() != null ? currentOverlay().key() : null;
    }
}
