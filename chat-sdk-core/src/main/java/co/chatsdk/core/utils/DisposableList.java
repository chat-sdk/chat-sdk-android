package co.chatsdk.core.utils;

import java.util.ArrayList;

import io.reactivex.disposables.Disposable;

/**
 * Created by ben on 8/25/17.
 */

public class DisposableList {

    private ArrayList<Disposable> disposables = new ArrayList<>();

    public void add (Disposable d) {
        disposables.add(d);
    }

    public void remove (Disposable d) {
        disposables.remove(d);
    }

    public void dispose () {
        for(Disposable d : disposables) {
            d.dispose();
        }
        disposables.clear();
    }

}
