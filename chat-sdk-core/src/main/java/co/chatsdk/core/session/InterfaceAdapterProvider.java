package co.chatsdk.core.session;

import co.chatsdk.core.interfaces.InterfaceAdapter;

public interface InterfaceAdapterProvider {
    Class<? extends InterfaceAdapter> getInterfaceAdapter();
}
