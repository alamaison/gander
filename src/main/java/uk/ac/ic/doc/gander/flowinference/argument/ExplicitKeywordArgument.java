package uk.ac.ic.doc.gander.flowinference.argument;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyObject;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.typegoals.expression.ExpressionTypeGoal;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;
import uk.ac.ic.doc.gander.model.parameters.FormalParameter;
import uk.ac.ic.doc.gander.model.parameters.FormalParameters;

/**
 * Model an argument, internal to the interpreter, that is being passed to a
 * procedure using a keyword to map it to a parameter.
 */
final class ExplicitKeywordArgument implements KeywordArgument {

    private final String keyword;
    private final ModelSite<exprType> value;

    ExplicitKeywordArgument(String keyword, ModelSite<exprType> value) {
        assert keyword != null;
        assert !keyword.isEmpty();
        assert value != null;

        this.keyword = keyword;
        this.value = value;
    }

    @Override
    public ArgumentDestination passArgumentAtCall(
            final InvokableCodeObject receiver) {

        FormalParameters parameters = receiver.formalParameters();

        if (parameters.hasKeywordableParameter(keyword())) {

            FormalParameter parameter = parameters
                    .keywordableParameter(keyword());
            return parameter.passage(this);

        } else {
            return new UntypableArgumentDestination() {

                @Override
                public Result<FlowPosition> nextFlowPositions() {
                    System.err.println("UNTYPABLE: " + receiver
                            + " has no parameter that accepts a keyword "
                            + "argument called '" + keyword() + "'");

                    return FiniteResult.bottom();
                }
            };
        }
    }

    private String keyword() {
        return keyword;
    }

    @Override
    public Result<PyObject> type(SubgoalManager goalManager) {
        return goalManager.registerSubgoal(new ExpressionTypeGoal(value));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        ExplicitKeywordArgument other = (ExplicitKeywordArgument) obj;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ExplicitKeywordArgument [keyword=" + keyword + ", value="
                + value + "]";
    }

}
