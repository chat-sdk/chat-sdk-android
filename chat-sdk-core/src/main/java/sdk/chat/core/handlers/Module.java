package sdk.chat.core.handlers;

import android.content.Context;

import org.greenrobot.greendao.annotation.NotNull;

/**
 * Created by SimonSmiley-Andrews on 01/05/2017.
 */

public interface Module {
    void activate(@NotNull Context context);
    String getName();
}
