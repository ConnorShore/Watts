package util;

@FunctionalInterface
public interface WattsCallback<T, R>{
    R apply(T var, WattsCallbackStatus status);
}