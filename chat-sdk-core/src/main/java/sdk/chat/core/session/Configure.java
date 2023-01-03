package sdk.chat.core.session;

public interface Configure<T> {

    void with(T config) throws Exception;

}
