package sdk.chat.core.session;

import androidx.annotation.NonNull;

import sdk.chat.core.base.BaseNetworkAdapter;

public interface NetworkAdapterProvider {
    @NonNull
    Class<? extends BaseNetworkAdapter> getNetworkAdapter();
}
