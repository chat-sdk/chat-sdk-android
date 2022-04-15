package sdk.chat.core.ui;

public interface KeyboardOverlayHandler {
    void send(Sendable sendable);
    void showOverlay(AbstractKeyboardOverlayFragment fragment);
}
