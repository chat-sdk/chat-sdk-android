package co.chatsdk.core.utils;

import java.util.ArrayList;

/**
 * Created by ben on 8/25/17.
 */

public class DisposableListenerList {

    private ArrayList<IsDisposable> disposables = new ArrayList<>();

    public void add (IsDisposable d) {
        disposables.add(d);
    }

    public void remove (IsDisposable d) {
        disposables.remove(d);
    }

    public void dispose () {
        for(IsDisposable d : disposables) {
            d.dispose();
        }
        disposables.clear();
    }


}
