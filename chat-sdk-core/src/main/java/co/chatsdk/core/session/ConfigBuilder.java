package co.chatsdk.core.session;

import android.content.Context;

import org.greenrobot.greendao.annotation.NotNull;

import java.util.ArrayList;
import java.util.List;

import co.chatsdk.core.base.BaseNetworkAdapter;
import co.chatsdk.core.handlers.Module;
import co.chatsdk.core.interfaces.InterfaceAdapter;

public class ConfigBuilder {

    public Class<? extends BaseNetworkAdapter> networkAdapter = null;
    public Class<? extends InterfaceAdapter> interfaceAdapter = null;

    public Context context;

    public Config config = new Config();

    public List<Module> modules = new ArrayList<>();

    public ConfigBuilder(@NotNull Class<? extends BaseNetworkAdapter> networkAdapterClass, @NotNull Class<? extends InterfaceAdapter> interfaceAdapterClass) {
        this.networkAdapter = networkAdapterClass;
        this.interfaceAdapter = interfaceAdapterClass;
    }

    public ConfigBuilder(Configure<Config> configure) {
        configure.with(config);
    }

    public ConfigBuilder addModule(Module module) {
        modules.add(module);
        return this;
    }

    public ConfigBuilder addModules(List<Module> modules) {
        modules.addAll(modules);
        return this;
    }

    public ConfigBuilder configure(Configure<Config> configure) {
        configure.with(config);
        return this;
    }

    public void activate(@NotNull Context context) throws Exception {
        this.context = context;
        ChatSDK.initialize(this);
    }

}
