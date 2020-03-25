package co.chatsdk.core.session;

import org.greenrobot.greendao.annotation.NotNull;

import co.chatsdk.core.base.BaseNetworkAdapter;

public interface NetworkAdapterProvider {
    @NotNull Class<? extends BaseNetworkAdapter> getNetworkAdapter();
}
