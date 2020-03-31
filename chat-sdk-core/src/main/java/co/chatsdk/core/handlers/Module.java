package co.chatsdk.core.handlers;

import android.content.Context;

import androidx.annotation.Nullable;

import org.greenrobot.greendao.annotation.NotNull;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface Module {
    void activate(@NotNull Context context);
    String getName();
}
