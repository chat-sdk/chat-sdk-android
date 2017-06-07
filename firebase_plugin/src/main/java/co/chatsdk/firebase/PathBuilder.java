package co.chatsdk.firebase;

/**
 * Created by benjaminsmiley-andrews on 29/05/2017.
 */

public class PathBuilder {

    private String path = "";

    public PathBuilder (String element) {
        path = element;
    }

    public PathBuilder a (String element) {
        path += "/" + element;
        return this;
    }

    public String build () {
        return path;
    }

}
