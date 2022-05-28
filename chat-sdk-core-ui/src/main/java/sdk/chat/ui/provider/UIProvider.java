package sdk.chat.ui.provider;

import sdk.chat.ui.icons.Icons;
import sdk.chat.ui.performance.HolderProvider;
import sdk.chat.ui.utils.ImageLoaderUtil;

public class UIProvider {

    protected Icons icons = new Icons();
    protected MenuItemProvider menuItemProvider = new MenuItemProvider();
    protected ChatOptionProvider chatOptionProvider = new ChatOptionProvider();
    protected SaveProvider saveProvider = new SaveProvider();
    protected HolderProvider holderProvider = new HolderProvider();
    protected ImageLoaderUtil imageLoaderUtil = new ImageLoaderUtil();

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
    public HolderProvider holderProvider() {
        return holderProvider;
    }
    public ImageLoaderUtil imageLoader() {
        return imageLoaderUtil;
    }

}
