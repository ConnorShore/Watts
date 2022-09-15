package util;

@FunctionalInterface
public interface WattsCallback<T>{
    void apply(T var, WattsCallbackStatus status);

    default void apply(T var) {
        apply(var, new WattsCallbackStatus());
    }
}