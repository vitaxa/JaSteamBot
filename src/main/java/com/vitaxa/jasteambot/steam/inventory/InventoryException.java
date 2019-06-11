package com.vitaxa.jasteambot.steam.inventory;

public class InventoryException extends Exception {

    private static final long serialVersionUID = -2963606920023969978L;

    public InventoryException() {
    }

    public InventoryException(String message) {
        super(message);
    }

    public InventoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public InventoryException(Throwable cause) {
        super(cause);
    }
}
