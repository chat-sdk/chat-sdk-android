package sdk.chat.core.session;

import org.greenrobot.greendao.annotation.NotNull;

import sdk.chat.core.base.BaseNetworkAdapter;

public interface NetworkAdapterProvider {
    @NotNull Class<? extends BaseNetworkAdapter> getNetworkAdapter();
}
