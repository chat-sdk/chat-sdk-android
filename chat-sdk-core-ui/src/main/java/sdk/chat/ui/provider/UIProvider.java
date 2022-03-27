package sdk.chat.ui.provider;

import sdk.chat.ui.icons.Icons;

public class UIProvider {

    protected Icons icons = new Icons();
    protected MenuItemProvider menuItemProvider = new MenuItemProvider();

    public Icons icons() {
        return icons;
    }

    public MenuItemProvider menuItems() {
        return menuItemProvider;
    }

}
