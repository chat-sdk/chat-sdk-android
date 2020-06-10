package sdk.chat.core.dao;

public interface Action<T> {
    void run(T object);
}
