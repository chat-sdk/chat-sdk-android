package sdk.chat.core.session;



import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sdk.chat.core.base.BaseNetworkAdapter;
import sdk.chat.core.module.Module;
import sdk.chat.core.interfaces.InterfaceAdapter;
import sdk.guru.common.BaseConfig;

public class ConfigBuilder<T> extends BaseConfig<T> {

    public Class<? extends BaseNetworkAdapter> networkAdapter = null;
    public Class<? extends InterfaceAdapter> interfaceAdapter = null;

    public Config<ConfigBuilder<T>> config = new Config<>(this);

    public List<Module> modules = new ArrayList<>();

    public ConfigBuilder(T onBuild) {
        super(onBuild);
    }

    public ConfigBuilder<T> setNetworkAdapter (@NonNull Class<? extends BaseNetworkAdapter> networkAdapterClass) {
        this.networkAdapter = networkAdapterClass;
        return this;
    }

    public ConfigBuilder<T> setInterfaceAdapter (@NonNull Class<? extends InterfaceAdapter> interfaceAdapterClass) {
        this.interfaceAdapter = interfaceAdapterClass;
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
