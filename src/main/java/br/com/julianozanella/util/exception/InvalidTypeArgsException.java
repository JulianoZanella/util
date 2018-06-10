package br.com.julianozanella.util.exception;

/**
 *
 * Invalid or unsupported argument types. Use int, string, double, char or
 * MysqlDate.
 *
 * @author Juliano Zanella
 */
public class InvalidTypeArgsException extends Exception {

    @Override
    public String getMessage() {
        return "Invalid Type of Arguments,\nUse int, string, double, char or MysqlDate.";
    }
}
