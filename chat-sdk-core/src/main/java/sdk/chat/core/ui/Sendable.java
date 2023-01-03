package sdk.chat.core.ui;

import android.app.Activity;
import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;

import io.reactivex.Completable;
import sdk.chat.core.dao.Thread;

public interface Sendable {
    Completable send(Activity activity, ActivityResultLauncher<Intent> launcher, Thread thread);
}
