package uk.ac.ic.doc.gander.flowinference.flowgoals;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.result.Top;
import uk.ac.ic.doc.gander.model.ModelSite;

/**
 * Result representing all flow positions.
 * 
 * Otherwise known as Top, this is used when the flow has 'escaped', in other
 * words when our analysis lost track of it.
 */
public final class TopF extends Top<ModelSite<exprType>> {

    public static final TopF INSTANCE = new TopF();

    @Override
    public String toString() {
        return "⊤f";
    }

    private TopF() {
    }

}
