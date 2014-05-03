package uk.ac.ic.doc.gander.flowinference.flowgoals.expressionflow;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.model.ModelSite;

/**
 * Models the flow of values across an assignment.
 */
final class AssignmentSituation implements FlowSituation {

    private final Assign node;
    private final ModelSite<?> site;

    /**
     * This situation keeps a reference to the node as the LHS indicates the
     * destination of the flow.
     */
    AssignmentSituation(Assign node, ModelSite<?> site) {
        this.node = node;
        this.site = site;
    }

    /**
     * An expression on the RHS of an assignment can flow to all the targets on
     * the LHS.
     */
    public Result<FlowPosition> nextFlowPositions(SubgoalManager goalManager) {
        Set<FlowPosition> positions = new HashSet<FlowPosition>();

        for (exprType lhsTarget : node.targets) {
            positions.add(new ExpressionPosition(new ModelSite<exprType>(
                    lhsTarget, site.codeObject())));
        }

        return new FiniteResult<FlowPosition>(positions);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((node == null) ? 0 : node.hashCode());
        result = prime * result + ((site == null) ? 0 : site.hashCode());
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
        AssignmentSituation other = (AssignmentSituation) obj;
        if (node == null) {
            if (other.node != null)
                return false;
        } else if (!node.equals(other.node))
            return false;
        if (site == null) {
            if (other.site != null)
                return false;
        } else if (!site.equals(other.site))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "AssignmentSituation [node=" + node + ", site=" + site + "]";
    }

}
