package uk.ac.ic.doc.gander.flowinference.flowgoals.flowsituations;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.ExpressionPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.typegoals.ExpressionTypeGoal;
import uk.ac.ic.doc.gander.flowinference.typegoals.FiniteTypeJudgement;
import uk.ac.ic.doc.gander.flowinference.typegoals.TypeJudgement;
import uk.ac.ic.doc.gander.flowinference.types.TObject;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.ModelSite;

/**
 * Model how a value can flow purely by virtue of being the result of a call.
 * 
 * This does not include how it can flow by being assigned (or otherwise bound)
 * to another expression. It only includes flow that happens through the very
 * act of being a call result. The only example of this is if the call was a
 * constructor call when the value flows to the {@code self} parameter of the
 * class's methods.
 */
final class CallResultSituation implements FlowSituation {

	private final ModelSite<? extends exprType> expression;

	CallResultSituation(ModelSite<? extends exprType> expression) {
		this.expression = expression;
	}

	public Set<FlowPosition> nextFlowPositions(SubgoalManager goalManager) {

		return handleMethodSelfFlow(goalManager);
	}

	private Set<FlowPosition> handleMethodSelfFlow(SubgoalManager goalManager) {

		TypeJudgement types = goalManager
				.registerSubgoal(new ExpressionTypeGoal(expression));
		if (types instanceof FiniteTypeJudgement) {
			Set<FlowPosition> positions = new HashSet<FlowPosition>();

			for (Type type : (FiniteTypeJudgement) types) {
				if (type instanceof TObject) {
					addSelfFromMethods(((TObject) type).getClassInstance(),
							positions);
				}
			}

			return positions;
		} else {
			return Collections.emptySet();
		}
	}

	private void addSelfFromMethods(Class classObject,
			Set<FlowPosition> positions) {
		Collection<Function> methods = classObject.getFunctions().values();

		for (Function method : methods) {
			List<ModelSite<exprType>> parameters = method.asCodeBlock()
					.getFormalParameters();

			if (parameters.size() > 0) {
				ModelSite<exprType> selfParameter = parameters.get(0);
				assert selfParameter.namespace().equals(method);
				positions.add(new ExpressionPosition<exprType>(selfParameter));
			} else {
				// Method is missing its self parameter!
			}
		}
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
		CallResultSituation other = (CallResultSituation) obj;
		if (expression == null) {
			if (other.expression != null)
				return false;
		} else if (!expression.equals(other.expression))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CallResultSituation [expression=" + expression + "]";
	}

}
