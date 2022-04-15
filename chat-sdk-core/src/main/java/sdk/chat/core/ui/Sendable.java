package sdk.chat.core.ui;

import android.app.Activity;

import io.reactivex.Completable;
import sdk.chat.core.dao.Thread;

public interface Sendable {
    Completable send(Activity activity, Thread thread);
}
