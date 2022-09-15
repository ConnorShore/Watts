package util;


public class WattsCallbackStatus {
    public boolean success;
    public String message;

    public WattsCallbackStatus() {
        this.success = true;
        this.message = "";
    }

    public WattsCallbackStatus(String message) {
        this.success = false;
        this.message = message;
    }

    public WattsCallbackStatus(boolean success) {
        this.success = success;
        this.message = "";
    }

    public WattsCallbackStatus(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}