package co.chatsdk.core.types;

/**
 * Created by benjaminsmiley-andrews on 03/07/2017.
 */

public class AccountDetails {

    public enum Type {
        Username,
        Facebook,
        Twitter,
        Google,
        Anonymous,
        Register,
        Custom,
    }

    public Type type;
    public String username;
    public String password;
    public String token;

    public static AccountDetails facebook () {
        AccountDetails a = new AccountDetails();
        a.type = Type.Facebook;
        return a;
    }

    public static AccountDetails username (String username, String password) {
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

    public static AccountDetails twitter () {
        AccountDetails a = new AccountDetails();
        a.type = Type.Twitter;
        return a;
    }

    public static AccountDetails google () {
        AccountDetails a = new AccountDetails();
        a.type = Type.Google;
        return a;
    }

    public static AccountDetails anonymous () {
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

    public boolean loginDetailsValid () {
        return username != null && !username.isEmpty() && password != null && !password.isEmpty();
    }

}
