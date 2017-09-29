package co.chatsdk.firebase;

/**
 * Created by benjaminsmiley-andrews on 29/05/2017.
 */

public class PathBuilder {

    private String path = "";
    private String value = "";

    public PathBuilder (String element) {
        path = element;
    }

    public PathBuilder append (String element) {
        path += "/" + element;
        return this;
    }

    public PathBuilder value (String value) {
        this.value = value;
        return this;
    }

    public String build () {
        return path;
    }

}
