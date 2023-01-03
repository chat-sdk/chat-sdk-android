package sdk.chat.core.dao;

public interface MetaValue<T> {

    String getKey();
    T getValue();

    void setKey(String key);
    void setValue (T value);

}
