package uk.ac.ic.doc.gander.flowinference.flowgoals.expressionflow;

import java.util.Collections;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.model.ModelSite;

/**
 * Models the flow of values out of an expression because of nested values
 * occurring in it.
 */
final class ExpressionSituation implements FlowSituation {

	private final ModelSite<? extends exprType> site;

	ExpressionSituation(ModelSite<? extends exprType> site) {
		this.site = site;
	}

	@Override
	public Result<FlowPosition> nextFlowPositions(SubgoalManager goalManager) {

		return new FiniteResult<FlowPosition>(
				Collections.singleton(new ExpressionPosition(site)));
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
		ExpressionSituation other = (ExpressionSituation) obj;
		if (site == null) {
			if (other.site != null)
				return false;
		} else if (!site.equals(other.site))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ExpressionSituation [site=" + site + "]";
	}

}
