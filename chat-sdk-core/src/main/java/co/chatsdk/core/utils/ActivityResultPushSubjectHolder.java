package co.chatsdk.core.utils;

import io.reactivex.subjects.PublishSubject;

/**
 * We have to use this because in some versions of Android when the camera
 * displays the chat activity is destroyed so we lose the publish subject
 */
public class ActivityResultPushSubjectHolder {

    private static PublishSubject<ActivityResult> instance = PublishSubject.create();

    public static PublishSubject<ActivityResult> shared() {
        return instance;
    }

}
