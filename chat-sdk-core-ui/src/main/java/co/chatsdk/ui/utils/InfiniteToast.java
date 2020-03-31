package co.chatsdk.ui.utils;

import android.content.Context;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * Created by ben on 9/28/17.
 */

public class InfiniteToast {

    Disposable disposable = null;
    Toast toast;

    String text;

    public InfiniteToast (final Context context,final int resId, boolean flash) {
        disposable = Observable.interval(0, flash ? 2500 : 2000, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(aLong -> {
            toast = Toast.makeText(context, resId, Toast.LENGTH_SHORT);
            if (text != null) {
                toast.setText(text);
            }
            toast.show();
        });
    }

    public void cancel () {
        if(disposable != null) {
            disposable.dispose();
        }
    }

    public void setText (String text) {
        this.text = text;
        toast.setText(text);
    }

    public void hide () {
        toast.cancel();
        cancel();
    }

}
