package sdk.chat.test;

import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;

import sdk.chat.android.live.R;
import sdk.chat.core.dao.User;
import sdk.chat.core.session.ChatSDK;
import sdk.chat.ui.activities.BaseActivity;


public class TestActivity extends BaseActivity {

    Button button;
    @Override
    protected int getLayout() {
        return R.layout.activity_test;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        button = findViewById(R.id.testButton);

        button.setOnClickListener(v -> {
            String otherUser = "9tJUx1iT5LQ1bC1J952No6sNZjZ2";
            User user = ChatSDK.db().fetchOrCreateEntityWithEntityID(User.class, otherUser);

//            List<Thread> threads = DaoCore.fetchEntitiesOfClass(Thread.class);
//            for (Thread thread: threads) {
//                thread.cascadeDelete();
//            }

            ChatSDK.thread().create1to1Thread(user, null).subscribe();
        });

    }
}
