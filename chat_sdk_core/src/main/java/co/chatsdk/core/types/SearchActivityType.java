package co.chatsdk.core.types;

/**
 * Created by ben on 10/9/17.
 */

public class SearchActivityType {

    public String title;
    public Class className;

    public SearchActivityType (Class className, String title) {
        this.className = className;
        this.title = title;
    }
}
