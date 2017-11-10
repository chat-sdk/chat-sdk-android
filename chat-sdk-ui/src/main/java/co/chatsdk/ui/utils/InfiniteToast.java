package co.chatsdk.ui.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Created by ben on 9/28/17.
 */

public class InfiniteToast {

    Disposable disposable = null;

    public InfiniteToast (final Context context,final int resId, boolean flash) {
        disposable = Observable.interval(0, flash ? 2500 : 2000, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Long>() {
            @Override
            public void accept(@NonNull Long aLong) throws Exception {
                Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void cancel () {
        if(disposable != null) {
            disposable.dispose();
        }
    }
}
