package co.chatsdk.core.utils;

import android.content.Intent;

/**
 * Created by ben on 11/9/17.
 */

public class ActivityResult {
    public int requestCode;
    public int resultCode;
    public Intent data;

    public ActivityResult (int requestCode, int resultCode, Intent data) {
        this.requestCode = requestCode;
        this.resultCode = resultCode;
        this.data = data;
    }
}
