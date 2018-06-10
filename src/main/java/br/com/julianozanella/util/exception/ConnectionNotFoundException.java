package br.com.julianozanella.util.exception;

/**
 * Unable to Connect Exception. Make sure you have create the Connection. 
 * @author Juliano Zanella
 */
public class ConnectionNotFoundException extends Exception {

    @Override
    public String getMessage() {
        return "Plesa connect first!";
    }
}
