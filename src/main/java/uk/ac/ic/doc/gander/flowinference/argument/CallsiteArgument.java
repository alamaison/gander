package uk.ac.ic.doc.gander.flowinference.argument;

import uk.ac.ic.doc.gander.flowinference.callframe.ArgumentPassingStrategy;

public interface CallsiteArgument {

    /**
     * Returns the real argument passed to the receivers.
     * 
     * @param argumentMapper
     *            the strategy that maps call-site arguments to actual arguments
     */
    Argument mapToActualArgument(ArgumentPassingStrategy argumentMapper);
}
