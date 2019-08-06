package co.chatsdk.core.handlers;

/**
 * Created by ben on 10/3/17.
 */

public interface ConfigurationHandler {

    boolean booleanForKey (String key);
    int integerForKey (String key);
    long longForKey (String key);
    float floatForKey (String key);
    String stringForKey (String key);

}
