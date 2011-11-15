package uk.ac.ic.doc.gander.flowinference.flowgoals;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.model.ModelSite;

/**
 * A flow position where the value has reached an expression.
 * 
 * @param <T>
 *            type of the expression's AST node
 */
public final class ExpressionPosition<T extends exprType> implements
		FlowPosition {

	private final ModelSite<T> site;

	public ExpressionPosition(ModelSite<T> site) {
		this.site = site;
	}

	public ModelSite<T> getSite() {
		return site;
	}

	public FlowStepGoal nextStepGoal() {
		return new ExpressionFlowStepGoal<T>(site);
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
		ExpressionPosition<?> other = (ExpressionPosition<?>) obj;
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