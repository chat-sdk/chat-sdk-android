package co.chatsdk.android.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.LayoutRes;
import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.types.MessageSendProgress;
import co.chatsdk.core.types.MessageSendStatus;
import co.chatsdk.ui.threads.ThreadsFragment;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;

public class SnapFragment extends ThreadsFragment {

    FloatingActionButton button;

    @Override
    public void initViews() {
        super.initViews();
        button = mainView.findViewById(R.id.floatingActionButton);
        button.setOnClickListener(v -> {
            Intent i = new Intent(SnapFragment.this.getActivity(), CameraActivity.class);
            startActivity(i);
        });
    }

    @Override
    protected Predicate<NetworkEvent> mainEventFilter() {
        return NetworkEvent.filterSnapThreadsUpdated();
    }

    @Override
    protected List<Thread> getThreads() {
        return ChatSDK.thread().getThreads(ThreadType.Private1to1Snap);
    }

    @Override
    protected @LayoutRes int activityLayout() {
        return R.layout.snap_activity_threads;
    }
}
