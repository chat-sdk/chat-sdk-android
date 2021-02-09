package sdk.chat.core.interfaces;

public interface IKeyStorage {

    void put(String key, String value);
    void put(String key, int value);
    void put(String key, long value);
    void save(String username, String password);
    String get(String key);
    int getInt(String key);
    long getLong(String key);
    void clear();
    void remove(String key);
}
