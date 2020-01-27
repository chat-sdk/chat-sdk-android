package firestream.chat.firebase.rx;

import androidx.annotation.Nullable;

import java.util.NoSuchElementException;

import firefly.sdk.chat.R;
import firestream.chat.namespace.Fire;

public class Optional<T> {
    private final T optional;

    public Optional() {
        this(null);
    }

    public Optional(@Nullable T optional) {
        this.optional = optional;
    }

    public boolean isEmpty() {
        return this.optional == null;
    }

    public T get() {
        if (optional == null) {
            throw new NoSuchElementException(Fire.internal().context().getString(R.string.error_no_value));
        }
        return optional;
    }
}
