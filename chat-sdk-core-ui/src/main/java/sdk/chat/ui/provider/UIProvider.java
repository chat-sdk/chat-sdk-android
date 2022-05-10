package sdk.chat.ui.provider;

import sdk.chat.core.ui.KeyboardOverlayHandler;
import sdk.chat.ui.icons.Icons;
import sdk.chat.ui.keyboard.KeyboardOverlayOptionsFragment;

public class UIProvider {

    protected Icons icons = new Icons();
    protected MenuItemProvider menuItemProvider = new MenuItemProvider();
    protected ChatOptionProvider chatOptionProvider = new ChatOptionProvider();
    protected SaveProvider saveProvider = new SaveProvider();

    public Icons icons() {
        return icons;
    }

    public MenuItemProvider menuItems() {
        return menuItemProvider;
    }
    public ChatOptionProvider chatOptions() {
        return chatOptionProvider;
    }
    public SaveProvider saveProvider() {
        return saveProvider;
    }

    public KeyboardOverlayOptionsFragment keyboardOverlayOptionsFragment(KeyboardOverlayHandler sender) {
        return new KeyboardOverlayOptionsFragment(sender);
    }

}
