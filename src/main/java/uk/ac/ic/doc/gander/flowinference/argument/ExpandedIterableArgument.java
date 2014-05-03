package uk.ac.ic.doc.gander.flowinference.argument;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyObject;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.typegoals.TopT;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;

final class ExpandedIterableArgument implements PositionalArgument {

    private final ModelSite<exprType> argument;

    ExpandedIterableArgument(ModelSite<exprType> argument) {
        assert argument != null;
        this.argument = argument;
    }

    @Override
    public ArgumentDestination passArgumentAtCall(
            final InvokableCodeObject receiver) {

        return new ArgumentDestination() {

            @Override
            public Result<FlowPosition> nextFlowPositions() {
                /*
                 * Expanding an iterable doesn't flow the iterable anywhere,
                 * just its contents.
                 */
                return FiniteResult.bottom();
            }
        };
    }

    @Override
    public Result<PyObject> type(SubgoalManager goalManager) {
        return TopT.INSTANCE;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((argument == null) ? 0 : argument.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ExpandedIterableArgument other = (ExpandedIterableArgument) obj;
        if (argument == null) {
            if (other.argument != null)
                return false;
        } else if (!argument.equals(other.argument))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ExpandedIterableArgument [argument=" + argument + "]";
    }

}
