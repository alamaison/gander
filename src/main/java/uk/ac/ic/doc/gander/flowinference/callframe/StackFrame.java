package uk.ac.ic.doc.gander.flowinference.callframe;

import java.util.List;
import java.util.Map;

/**
 * Models the knowledge about the values being passed to a procedure.
 * 
 * There are two ways to pass a value to a procedure: by position and by
 * keyword. By, because we are statically simulating a running programs, we
 * can't always know, in detail how the values are passed. We should always know
 * the manner in which they are passed (position or keyword) but we might not
 * know the detail (which position, which keyword). This interface abstracts
 * over this allowing us to reason with as much detail as is available.
 */
public interface StackFrame<Value> {

    /**
     * Returns the values that are passed by position and whose position is
     * known.
     * 
     * @return a list containing the values; the indices in the list are the
     *         positions that the value is passed at; note: this means known
     *         positions are contiguous just like in Python
     */
    List<Value> knownPositions();

    /**
     * Returns the values that are passed by keyword and whose keyword is known.
     * 
     * @return a mapping from keywords to values
     */
    Map<String, Value> knownKeywords();

    /**
     * Returns whether the call includes arguments passed by position but whose
     * exact position cannot be determined statically.
     */
    boolean includesUnknownPositions();

    /**
     * Returns whether the call includes arguments passed by keyword but whose
     * exact keyword cannot be determined statically.
     */
    boolean includesUnknownKeywords();
}
