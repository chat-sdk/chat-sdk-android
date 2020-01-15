package co.chatsdk.core.dao;

public interface Action<T> {
    void run(T object);
}
