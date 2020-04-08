package co.chatsdk.core.session;

import org.greenrobot.greendao.annotation.NotNull;

import java.util.ArrayList;
import java.util.List;

import co.chatsdk.core.base.BaseNetworkAdapter;
import co.chatsdk.core.handlers.Module;
import co.chatsdk.core.interfaces.InterfaceAdapter;
import sdk.guru.common.BaseConfig;

public class ConfigBuilder<T> extends BaseConfig<T> {

    public Class<? extends BaseNetworkAdapter> networkAdapter = null;
    public Class<? extends InterfaceAdapter> interfaceAdapter = null;

    public Config<ConfigBuilder<T>> config = new Config<>(this);

    public List<Module> modules = new ArrayList<>();

    public ConfigBuilder(T onBuild) {
        super(onBuild);
    }

    public ConfigBuilder<T> setNetworkAdapter (@NotNull Class<? extends BaseNetworkAdapter> networkAdapterClass) {
        this.networkAdapter = networkAdapterClass;
        return this;
    }

    public ConfigBuilder<T> setInterfaceAdapter (@NotNull Class<? extends InterfaceAdapter> interfaceAdapterClass) {
        this.interfaceAdapter = interfaceAdapterClass;
        return this;
    }

    public Config<ConfigBuilder<T>> configure() {
        return config;
    }

    public ConfigBuilder<T> addModule(Module module) {
        modules.add(module);
        return this;
    }

    public ConfigBuilder<T> addModules(List<Module> modules) {
        this.modules.addAll(modules);
        return this;
    }


}
