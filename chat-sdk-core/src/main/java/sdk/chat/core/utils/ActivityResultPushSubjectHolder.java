package sdk.chat.core.utils;


import com.jakewharton.rxrelay2.PublishRelay;

/**
 * We have to use this because in some versions of Android when the camera
 * displays the chat activity is destroyed so we lose the publish subject
 */
public class ActivityResultPushSubjectHolder {

    private static PublishRelay<ActivityResult> instance = PublishRelay.create();

    public static PublishRelay<ActivityResult> shared() {
        return instance;
    }

}
