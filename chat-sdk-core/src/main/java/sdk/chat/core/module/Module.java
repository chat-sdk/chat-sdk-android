package sdk.chat.core.module;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.List;

import sdk.chat.core.handlers.MessageHandler;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface Module {
    void activate(@NonNull Context context) throws Exception;
    String getName();

    MessageHandler getMessageHandler();

    List<String> requiredPermissions();

    void stop();
}
