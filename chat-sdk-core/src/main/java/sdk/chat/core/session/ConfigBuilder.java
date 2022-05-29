package sdk.chat.core.session;


import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sdk.chat.core.base.BaseNetworkAdapter;
import sdk.chat.core.interfaces.InterfaceAdapter;
import sdk.chat.core.module.Module;
import sdk.guru.common.BaseConfig;

public class ConfigBuilder<T> extends BaseConfig<T> {

    public Class<? extends BaseNetworkAdapter> networkAdapter = null;
    public Class<? extends InterfaceAdapter> interfaceAdapter = null;

    public Config<ConfigBuilder<T>> config = new Config<>(this);

    public List<Module> modules = new ArrayList<>();

    public ConfigBuilder(T onBuild) {
        super(onBuild);
    }

    public ConfigBuilder<T> setNetworkAdapter (@Nullable Class<? extends BaseNetworkAdapter> networkAdapterClass) {
        if (networkAdapterClass != null) {
            this.networkAdapter = networkAdapterClass;
        }
        return this;
    }

    public ConfigBuilder<T> setInterfaceAdapter (@Nullable Class<? extends InterfaceAdapter> interfaceAdapterClass) {
        if (interfaceAdapterClass != null) {
            this.interfaceAdapter = interfaceAdapterClass;
        }
        return this;
    }

    public Config<ConfigBuilder<T>> builder() {
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

    public ConfigBuilder<T> addModules(Module... modules) {
        this.modules.addAll(Arrays.asList(modules));
        return this;
    }

    public Config config() {
        return config;
    }

}
