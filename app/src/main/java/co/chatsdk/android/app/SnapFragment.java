package co.chatsdk.android.app;

import android.content.Intent;

import androidx.annotation.LayoutRes;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import co.chatsdk.core.dao.Thread;
import co.chatsdk.core.events.NetworkEvent;
import co.chatsdk.core.interfaces.ThreadType;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.ui.threads.ThreadsFragment;
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
