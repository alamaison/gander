package uk.ac.ic.doc.gander.flowinference.flowgoals.expressionflow;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.TopFp;
import uk.ac.ic.doc.gander.flowinference.result.Concentrator;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Concentrator.DatumProcessor;
import uk.ac.ic.doc.gander.flowinference.typegoals.ExpressionTypeGoal;
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

	public Result<FlowPosition> nextFlowPositions(SubgoalManager goalManager) {

		return new CallResultSituationSolver(expression, goalManager)
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

final class CallResultSituationSolver {

	private final Result<FlowPosition> solution;

	CallResultSituationSolver(ModelSite<? extends exprType> expression,
			SubgoalManager goalManager) {

		Result<Type> types = goalManager
				.registerSubgoal(new ExpressionTypeGoal(expression));

		Concentrator<Type, FlowPosition> action = Concentrator.newInstance(
				new ConstructorValueFlower(), TopFp.INSTANCE);
		types.actOnResult(action);
		solution = action.result();
	}

	public Result<FlowPosition> solution() {
		return solution;
	}
}

/**
 * Flows the value produced by contructor calls to the positions of the {@code
 * self} parameter in each method.
 */
final class ConstructorValueFlower implements
		DatumProcessor<Type, FlowPosition> {

	public Result<FlowPosition> process(Type datum) {

		Set<FlowPosition> positions;
		if (datum instanceof TObject) {
			positions = selfPositionsInMethods(((TObject) datum)
					.getClassInstance());
		} else {
			positions = Collections.emptySet();
		}
		return new FiniteResult<FlowPosition>(positions);

	}

	private static Set<FlowPosition> selfPositionsInMethods(Class classObject) {

		Set<FlowPosition> positions = new HashSet<FlowPosition>();

		Collection<Function> methods = classObject.getFunctions().values();

		for (Function method : methods) {
			List<ModelSite<exprType>> parameters = method.codeObject()
					.formalParameters().parameters();

			if (parameters.size() > 0) {
				ModelSite<exprType> selfParameter = parameters.get(0);
				assert selfParameter.codeObject().equals(method.codeObject());
				positions.add(new ExpressionPosition<exprType>(selfParameter));
			} else {
				// Method is missing its self parameter!
			}
		}

		return positions;
	}

}
