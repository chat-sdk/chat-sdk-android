package sdk.chat.ui.provider;

import android.app.Activity;

import sdk.chat.ui.icons.Icons;
import sdk.chat.ui.performance.HolderProvider;
import sdk.chat.ui.utils.ImageLoaderUtil;
import sdk.chat.ui.views.PopupImageView;

public class UIProvider {

    public static interface PopupImageViewProvider {
        public PopupImageView provide(Activity context);
    }

    protected Icons icons = new Icons();
    protected MenuItemProvider menuItemProvider = new MenuItemProvider();
    protected ChatOptionProvider chatOptionProvider = new ChatOptionProvider();
    protected SaveProvider saveProvider = new SaveProvider();
    protected HolderProvider holderProvider = new HolderProvider();
    protected ImageLoaderUtil imageLoaderUtil = new ImageLoaderUtil();

    protected PopupImageViewProvider popupImageViewProvider = PopupImageView::new;

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

    public PopupImageView popupImageView(Activity context) {
        return popupImageViewProvider.provide(context);
    }

    public void setIcons(Icons icons) {
        this.icons = icons;
    }

    public void setMenuItemProvider(MenuItemProvider menuItemProvider) {
        this.menuItemProvider = menuItemProvider;
    }

    public void setChatOptionProvider(ChatOptionProvider chatOptionProvider) {
        this.chatOptionProvider = chatOptionProvider;
    }

    public void setSaveProvider(SaveProvider saveProvider) {
        this.saveProvider = saveProvider;
    }

    public void setHolderProvider(HolderProvider holderProvider) {
        this.holderProvider = holderProvider;
    }

    public void setImageLoaderUtil(ImageLoaderUtil imageLoaderUtil) {
        this.imageLoaderUtil = imageLoaderUtil;
    }

    public void setPopupImageViewProvider(PopupImageViewProvider popupImageViewProvider) {
        this.popupImageViewProvider = popupImageViewProvider;
    }


}
