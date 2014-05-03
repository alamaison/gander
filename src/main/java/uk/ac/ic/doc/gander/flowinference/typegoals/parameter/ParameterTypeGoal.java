package uk.ac.ic.doc.gander.flowinference.typegoals.parameter;

import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyObject;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.typegoals.TypeGoal;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

/**
 * Find the types that the given parameter may bind to the given variable name.
 * 
 * In Python, a single parameter may bind more than one name which is why the
 * query is framed in this way.
 */
public final class ParameterTypeGoal implements TypeGoal {

    private final InvokableCodeObject invokable;
    private final Variable variable;

    public ParameterTypeGoal(InvokableCodeObject invokable, Variable variable) {
        assert variable != null;
        assert invokable.formalParameters().hasVariableBindingParameter(
                variable);

        this.invokable = invokable;
        this.variable = variable;
    }

    @Override
    public Result<PyObject> initialSolution() {
        return FiniteResult.bottom();
    }

    @Override
    public Result<PyObject> recalculateSolution(SubgoalManager goalManager) {
        if (goalManager == null)
            throw new NullPointerException("Goal manager required");

        return new ParameterTypeGoalSolver(invokable, variable, goalManager)
                .solution();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((invokable == null) ? 0 : invokable.hashCode());
        result = prime * result
                + ((variable == null) ? 0 : variable.hashCode());
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
        ParameterTypeGoal other = (ParameterTypeGoal) obj;
        if (invokable == null) {
            if (other.invokable != null)
                return false;
        } else if (!invokable.equals(other.invokable))
            return false;
        if (variable == null) {
            if (other.variable != null)
                return false;
        } else if (!variable.equals(other.variable))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ParameterTypeGoal [invokable=" + invokable + ", variable="
                + variable + "]";
    }

}
