package uk.ac.ic.doc.gander.flowinference.flowgoals;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.flowsituations.FlowSituation;
import uk.ac.ic.doc.gander.flowinference.flowgoals.flowsituations.FlowSituationFinder;
import uk.ac.ic.doc.gander.flowinference.typegoals.ExpressionTypeGoal;
import uk.ac.ic.doc.gander.flowinference.types.TObject;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.flowinference.types.judgement.SetBasedTypeJudgement;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeJudgement;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.ModelSite;

/**
 * Finds the next step of an expression's flow based on its flow situation.
 */
final class ExpressionFlowStepGoal<T extends exprType> implements FlowStepGoal {

	private final ModelSite<T> expression;

	public ExpressionFlowStepGoal(ModelSite<T> expression) {
		this.expression = expression;
	}

	public Set<FlowPosition> initialSolution() {
		return Collections.emptySet();
	}

	public Set<FlowPosition> recalculateSolution(SubgoalManager goalManager) {

		FlowSituation situation = FlowSituationFinder
				.findFlowSituation(expression);
		if (situation != null) {
			return situation.nextFlowPositions(goalManager);
		} else {
			return handleMethodSelfFlow(goalManager);
			// return Collections.emptySet();
		}
	}

	private Set<FlowPosition> handleMethodSelfFlow(SubgoalManager goalManager) {

		TypeJudgement types = goalManager
				.registerSubgoal(new ExpressionTypeGoal(expression));
		if (types instanceof SetBasedTypeJudgement) {
			Set<FlowPosition> positions = new HashSet<FlowPosition>();

			for (Type type : ((SetBasedTypeJudgement) types)
					.getConstituentTypes()) {
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
				assert selfParameter.getEnclosingScope().equals(method);
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
		ExpressionFlowStepGoal<?> other = (ExpressionFlowStepGoal<?>) obj;
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
