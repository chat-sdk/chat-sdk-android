package firefly.sdk.chat.firebase.rx;

import androidx.annotation.Nullable;

import java.util.NoSuchElementException;

import firefly.sdk.chat.R;
import firefly.sdk.chat.namespace.Fl;

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
            throw new NoSuchElementException(Fl.y.context().getString(R.string.error_no_value));
        }
        return optional;
    }
}
