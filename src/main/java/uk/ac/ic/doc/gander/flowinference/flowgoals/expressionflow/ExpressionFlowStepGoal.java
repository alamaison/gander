package uk.ac.ic.doc.gander.flowinference.flowgoals.expressionflow;

import java.util.Set;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowStepGoal;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.RedundancyEliminator;
import uk.ac.ic.doc.gander.model.ModelSite;

/**
 * Finds the next step of an expression's flow based on its flow situation.
 */
final class ExpressionFlowStepGoal implements FlowStepGoal {

    private final ModelSite<? extends exprType> expression;

    public ExpressionFlowStepGoal(ModelSite<? extends exprType> expression) {
        this.expression = expression;
    }

    public Result<FlowPosition> initialSolution() {
        return FiniteResult.bottom();
    }

    public Result<FlowPosition> recalculateSolution(SubgoalManager goalManager) {

        return new ExpressionFlowStepGoalSolver(expression, goalManager)
                .solution();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((expression == null) ? 0 : expression.hashCode());
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
        ExpressionFlowStepGoal other = (ExpressionFlowStepGoal) obj;
        if (expression == null) {
            if (other.expression != null)
                return false;
        } else if (!expression.equals(other.expression))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ExpressionFlowStepGoal [expression=" + expression + "]";
    }

}

final class ExpressionFlowStepGoalSolver {

    private final RedundancyEliminator<FlowPosition> nextPositions = new RedundancyEliminator<FlowPosition>();

    public ExpressionFlowStepGoalSolver(
            ModelSite<? extends exprType> expression, SubgoalManager goalManager) {

        Set<FlowSituation> situations = FlowSituationFinder
                .findFlowSituations(expression);

        for (FlowSituation flowSituation : situations) {
            nextPositions.add(flowSituation.nextFlowPositions(goalManager));
            if (nextPositions.isFinished())
                break;
        }
    }

    public Result<FlowPosition> solution() {
        return nextPositions.result();
    }

}
