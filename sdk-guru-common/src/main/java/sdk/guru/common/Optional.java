package sdk.guru.common;

import io.reactivex.annotations.Nullable;

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
        return optional;
    }

    public static <T> Optional<T> empty() {
        return new Optional<>();
    }

    public static <T> Optional<T> with(T value) {
        return new Optional<>(value);
    }

}
