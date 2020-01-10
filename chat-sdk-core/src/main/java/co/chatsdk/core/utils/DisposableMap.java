package co.chatsdk.core.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import io.reactivex.disposables.Disposable;

public class DisposableMap {

    protected class DisposableList {
        private final List<Disposable> disposables = Collections.synchronizedList(new ArrayList<>());

        public void add (Disposable d) {
            disposables.add(d);
        }

        public void remove (Disposable d) {
            disposables.remove(d);
        }

        public void dispose () {
            synchronized (disposables) {
                for (Disposable disposable : disposables) {
                    disposable.dispose();
                }
                disposables.clear();
            }
        }

    }

    protected static String DefaultKey = "def";

    protected HashMap<Object, DisposableList> map = new HashMap<>();


    public void put(Object key, Disposable disposable) {
        get(key).add(disposable);
    }

    /**
     * Dispose of the disposables associated with a key
     * @param key for the disposables
     */
    public void dispose(Object key) {
        get(key).dispose();
    }

    protected DisposableList get(Object key) {
        DisposableList list = map.get(key);
        if (list == null) {
            list = new DisposableList();
            map.put(key, list);
        }
        return list;
    }

    public void add(Disposable disposable) {
        if (disposable != null) {
            DisposableList list = get(DefaultKey);
            if (list == null) {
                list = new DisposableList();
                this.map.put(DefaultKey, list);
            }
            get(DefaultKey).add(disposable);
        }
    }

    /**
     * Dispose of the default list - accessed when you call add()
     */
    public void dispose() {
        get(DefaultKey).dispose();
    }

    /**
     * Dispose of all disposables
     */
    public void disposeAll() {
        for (Object key: map.keySet()) {
            get(key).dispose();
        }
    }
}

