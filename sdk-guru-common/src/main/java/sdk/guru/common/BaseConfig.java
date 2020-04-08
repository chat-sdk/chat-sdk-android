package sdk.guru.common;

public class BaseConfig<T> {

    protected T onBuild;

    public BaseConfig(T onBuild) {
        this.onBuild = onBuild;
    }

    public T build() {
        return onBuild;
    }
}
