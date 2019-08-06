package co.chatsdk.ui.chat.message_action;

import android.app.Activity;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.util.List;

import co.chatsdk.core.dao.Message;
import co.chatsdk.core.message_action.MessageAction;
import co.chatsdk.core.utils.CrashReportingCompletableObserver;
import co.chatsdk.core.utils.DisposableList;
import co.chatsdk.ui.R;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class MessageActionHandler {

    protected SpeedDialView view;
    protected Message message;
    protected DisposableList disposableList = new DisposableList();

    public MessageActionHandler(SpeedDialView view) {
        this.view = view;
        view.setVisibility(View.INVISIBLE);
        view.setExpansionMode(SpeedDialView.ExpansionMode.BOTTOM);

        view.setOnChangeListener(new SpeedDialView.OnChangeListener() {
            @Override
            public boolean onMainActionSelected() {
                return false;
            }

            @Override
            public void onToggleChanged(boolean isOpen) {
                if (!isOpen) {
                    hide();
                }
            }
        });
    }

    public Single<String> open (List<MessageAction> actions, Activity activity) {
        return Single.create(emitter -> {
            view.setVisibility(View.VISIBLE);

            for (SpeedDialActionItem item : view.getActionItems()) {
                view.removeActionItem(item);
            }
            view.close(false);

            for (int i = 0; i < actions.size(); i++) {
                MessageAction action = actions.get(i);
                view.addActionItem(new SpeedDialActionItem.Builder(i, action.iconResourceId)
                        .setLabel(action.titleResourceId)
                        .create());
            }
            view.setOnActionSelectedListener(actionItem -> {
                MessageAction action = actions.get(actionItem.getId());

                // Execute the action
                disposableList.add(action.execute(activity)
                        .doOnTerminate(() -> disposableList.dispose())
                        .subscribe(() -> {
                    if (action.successMessageId > 0) {
                        emitter.onSuccess(activity.getString(action.successMessageId));
                    } else {
                        emitter.onSuccess("");
                    }
                }, emitter::onError));

                return false;
            });

            view.open();
        });
    }

//    public void showSnackbar (Activity activity, int messageResId) {
//        Snackbar.make(activity.findViewById(android.R.id.content), messageResId, Snackbar.LENGTH_SHORT);
//    }
//
//    public void showSnackbar (Activity activity, String message) {
//        Snackbar.make(activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT);
//    }

    public void hide () {
//        disposableList.dispose();
        view.setVisibility(View.INVISIBLE);
    }
}
