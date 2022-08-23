package util;


public class WattsCallbackStatus {
    public boolean success;
    public String message;

    public WattsCallbackStatus(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public WattsCallbackStatus(boolean success) {
        this.success = success;
        this.message = "";
    }
}