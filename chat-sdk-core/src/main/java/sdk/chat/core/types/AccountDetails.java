package sdk.chat.core.types;

import java.util.HashMap;

/**
 * Created by benjaminsmiley-andrews on 03/07/2017.
 */

public class AccountDetails {

    /**
     * For social login, user Firebase UI
     */
    public enum Type {
        Username,
        Anonymous,
        Register,
        Custom,
    }

    public Type type;
    public String username;
    public String password;
    public String token;
    public HashMap<String, String> meta = new HashMap<>();

    public static AccountDetails username(String username, String password) {
        AccountDetails a = new AccountDetails();
        a.type = Type.Username;
        a.username = username;
        a.password = password;
        return a;
    }

    public static AccountDetails signUp(String username, String password) {
        AccountDetails a = new AccountDetails();
        a.type = Type.Register;
        a.username = username;
        a.password = password;
        return a;
    }

    public static AccountDetails anonymous() {
        AccountDetails a = new AccountDetails();
        a.type = Type.Anonymous;
        return a;
    }

    public static AccountDetails token(String token) {
        AccountDetails a = new AccountDetails();
        a.type = Type.Custom;
        a.token = token;
        return a;
    }

    public boolean areValid() {
        return username != null && !username.isEmpty() && password != null && !password.isEmpty();
    }

    public void setMetaValue(String key, String value) {
        meta.put(key, value);
    }

    public String getMetaValue (String key) {
        return meta.get(key);
    }

}
