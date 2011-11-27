package uk.ac.ic.doc.gander.flowinference.sendersgoals;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.ast.AstParentNodeFinder;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.CodeObjectPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowGoal;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.ModelSite;

/**
 * Find any callsites that could call the given function.
 */
public class FunctionSendersGoal implements SendersGoal {
	private final Function callable;

	public FunctionSendersGoal(Function callable) {
		this.callable = callable;
	}

	public Set<ModelSite<Call>> initialSolution() {
		return Collections.emptySet();
	}

	public Set<ModelSite<Call>> recalculateSolution(
			final SubgoalManager goalManager) {
		Set<ModelSite<Call>> callSites = new HashSet<ModelSite<Call>>();

		Set<ModelSite<? extends exprType>> positions = goalManager
				.registerSubgoal(new FlowGoal(new CodeObjectPosition(callable)));
		if (positions == null) {
			return null;
		} else {
			for (ModelSite<? extends exprType> expression : positions) {
				SimpleNode parent = AstParentNodeFinder.findParent(expression
						.astNode(), expression.codeObject().ast());
				if (parent instanceof Call) {
					callSites.add(new ModelSite<Call>((Call) parent, expression
							.codeObject()));
				}
			}
		}

		return callSites;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((callable == null) ? 0 : callable.hashCode());
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
		FunctionSendersGoal other = (FunctionSendersGoal) obj;
		if (callable == null) {
			if (other.callable != null)
				return false;
		} else if (!callable.equals(other.callable))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FunctionSendersGoal [callable=" + callable + "]";
	}

}
