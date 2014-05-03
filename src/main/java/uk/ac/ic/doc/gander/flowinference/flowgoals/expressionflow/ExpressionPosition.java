package uk.ac.ic.doc.gander.flowinference.flowgoals.expressionflow;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowStepGoal;
import uk.ac.ic.doc.gander.model.ModelSite;

/**
 * A flow position where the value has reached an expression.
 */
public final class ExpressionPosition implements FlowPosition {

    private final ModelSite<? extends exprType> site;

    public ExpressionPosition(ModelSite<? extends exprType> site) {
        this.site = site;
    }

    public ModelSite<? extends exprType> getSite() {
        return site;
    }

    public FlowStepGoal nextStepGoal() {
        return new ExpressionFlowStepGoal(site);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
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
        ExpressionPosition other = (ExpressionPosition) obj;
        if (site == null) {
            if (other.site != null)
                return false;
        } else if (!site.equals(other.site))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ExpressionPosition [site=" + site + "]";
    }

}
