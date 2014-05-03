package uk.ac.ic.doc.gander.flowinference;

public final class TypeError extends Exception {

    public TypeError(String string, Object relevantData) {
        super(string + ": " + relevantData);
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

}
