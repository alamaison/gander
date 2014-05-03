package uk.ac.ic.doc.gander.model.parameters;

import java.util.Collections;
import java.util.Set;

import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.argumentsType;

import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyInstance;
import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyObject;
import uk.ac.ic.doc.gander.flowinference.argument.Argument;
import uk.ac.ic.doc.gander.flowinference.argument.ArgumentDestination;
import uk.ac.ic.doc.gander.flowinference.callframe.StackFrame;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.TopFp;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

/**
 * Model of a parameter that mops up all the keyword arguments passed to the
 * procedure that weren't captured by one of the other parameters.
 * 
 * In Python this looks like {@code arg} in {@code def proc(x, y, *arg)}. The
 * excess argument are packed into a tuple in order of increasing position. For
 * example, if {@code proc} were called as {@code proc(1, 2, 3, 4)},
 * {@code args} would be the tuple {@code (3, 4)}.
 */
final class KwargParameter implements FormalParameter {

    private final ModelSite<argumentsType> argsNode;

    KwargParameter(ModelSite<argumentsType> argsNode) {
        assert argsNode != null;
        this.argsNode = argsNode;
    }

    @Override
    public InvokableCodeObject codeObject() {
        return (InvokableCodeObject) argsNode.codeObject();
    }

    @Override
    public ArgumentDestination passage(Argument argument) {

        return new ArgumentDestination() {

            @Override
            public Result<FlowPosition> nextFlowPositions() {
                return TopFp.INSTANCE;
            }
        };
    }

    @Override
    public Set<Variable> boundVariables() {
        Variable variable = new Variable(
                ((NameTok) argsNode.astNode().kwarg).id, codeObject());
        return Collections.singleton(variable);
    }

    @Override
    public Set<Argument> argumentsPassedAtCall(StackFrame<Argument> callFrame,
            SubgoalManager goalManager) {
        return Collections.emptySet();
    }

    @Override
    public Result<PyObject> objectsPassedAtCall(StackFrame<Argument> stackFrame,
            Variable variable, SubgoalManager goalManager) {
        if (variable == null)
            throw new NullPointerException("Variable required");

        /*
         * Regardless of what may be passed to the kwvarargs parameter (even if
         * it's nothing) it will always bind a dictionary to its variable.
         */
        if (boundVariables().contains(variable)) {

            PyObject tuple = new PyInstance(variable.codeObject().model()
                    .builtinDictionary());

            return new FiniteResult<PyObject>(Collections.singleton(tuple));
        } else {
            return FiniteResult.bottom();
        }
    }

    @Override
    public boolean acceptsArgumentByPosition(int position) {
        return false;
    }

    @Override
    public boolean acceptsArgumentByKeyword(String keyword) {
        /* The kwarg parameter accepts all leftover keyword parameters */
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((argsNode == null) ? 0 : argsNode.hashCode());
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
        KwargParameter other = (KwargParameter) obj;
        if (argsNode == null) {
            if (other.argsNode != null)
                return false;
        } else if (!argsNode.equals(other.argsNode))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "KwargParameter [argsNode=" + argsNode + "]";
    }

}
