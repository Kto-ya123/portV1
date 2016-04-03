package by.bsuir.threads.exception;


public class ShipException extends Exception {
    public ShipException(String message) {
        super(message);
    }

    public ShipException(String message, Throwable cause) {
        super(message, cause);
    }
}
