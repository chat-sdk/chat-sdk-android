package sdk.chat.core.session;

import sdk.chat.core.interfaces.InterfaceAdapter;

public interface InterfaceAdapterProvider {
    Class<? extends InterfaceAdapter> getInterfaceAdapter();
}
