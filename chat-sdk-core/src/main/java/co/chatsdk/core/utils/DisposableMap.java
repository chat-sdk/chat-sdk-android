package co.chatsdk.core.utils;

import java.util.HashMap;

import io.reactivex.disposables.Disposable;

public class DisposableMap {

    final private HashMap<String, Disposable> disposableHashMap = new HashMap<>();

    public void add(String key, Disposable disposable) {
        Disposable previousDisposable = disposableHashMap.get(key);
        if (previousDisposable != null) {
            previousDisposable.dispose();
        }
        disposableHashMap.put(key, disposable);
    }

    public void remove(String key) {
        disposableHashMap.remove(key);
    }

    public void dispose(String key) {
        Disposable previousDisposable = disposableHashMap.get(key);
        if (previousDisposable != null) {
            previousDisposable.dispose();
        }
        remove(key);
    }

    public void clear() {
        disposableHashMap.clear();
    }

    public void dispose() {
        for (Disposable disposable : disposableHashMap.values()) {
            if (disposable != null) {
                disposable.dispose();
            }
        }
        clear();
    }

}
