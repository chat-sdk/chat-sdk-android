package sdk.chat.core.types;

import android.app.Activity;
import android.content.Intent;

/**
 * Created by ben on 10/9/17.
 */

public class SearchActivityType {

    public String title;
    public Class className;
    public int requestCode;

    public SearchActivityType(Class className, String title, int requestCode) {
        this.className = className;
        this.title = title;
        this.requestCode = requestCode;
    }

    public SearchActivityType(Class className, String title) {
        this(className, title, -1);
    }

    public void startFrom(Activity activity) {
        Intent intent = new Intent(activity, className);
        if (requestCode >= 0) {
            activity.startActivityForResult(intent, requestCode);
        } else {
            activity.startActivity(intent);
        }
    }
}
