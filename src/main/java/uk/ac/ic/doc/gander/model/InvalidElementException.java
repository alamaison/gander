package uk.ac.ic.doc.gander.model;

import java.io.File;

public class InvalidElementException extends Exception {

    private static final long serialVersionUID = 3343833136152625354L;
    private String message;
    private File offender;

    public InvalidElementException(String message, File offender) {
        this.message = message;
        this.offender = offender;
    }

    @Override
    public String getMessage() {
        return message + ": " + offender.getAbsolutePath();
    }
}
