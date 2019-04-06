package co.chatsdk.core.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import io.reactivex.disposables.Disposable;

/**
 * Created by ben on 8/25/17.
 */

public class DisposableList {

    private final List<Disposable> disposables = Collections.synchronizedList(new ArrayList<>());

    public void add (Disposable d) {
        disposables.add(d);
    }

    public void remove (Disposable d) {
        disposables.remove(d);
    }

    public void dispose () {
        synchronized (disposables) {
            Iterator<Disposable> iterator = disposables.iterator();
            while (iterator.hasNext()) {
                iterator.next().dispose();
            }
            disposables.clear();
        }
    }

}
