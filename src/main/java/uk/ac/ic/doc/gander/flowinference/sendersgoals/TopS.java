package uk.ac.ic.doc.gander.flowinference.sendersgoals;

import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.flowinference.result.Top;
import uk.ac.ic.doc.gander.model.ModelSite;

/**
 * Result representing all expressions calling an value.
 * 
 * Otherwise known as Top, this is used when we don't know what might call the
 * function. This is a conservative approximation.
 */
final class TopS extends Top<ModelSite<Call>> {

    static final TopS INSTANCE = new TopS();

    @Override
    public String toString() {
        return "⊤s";
    }

    private TopS() {
    }

}
