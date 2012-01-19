package uk.ac.ic.doc.gander.flowinference.flowgoals;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.ast.AstParentNodeFinder;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.expressionflow.ExpressionPosition;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Result.Processor;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.ClassCO;

/**
 * Goal modelling the flow of class instances in the first step after creation.
 * 
 * This is the set of all constructor calls that could call the given class.
 */
final class InstanceCreationStepGoal implements FlowStepGoal {

	private final ClassCO klass;

	InstanceCreationStepGoal(ClassCO klass) {
		this.klass = klass;
	}

	public Result<FlowPosition> initialSolution() {
		return FiniteResult.bottom();
	}

	/**
	 * Calls to any of the places the metaclass object flows to result in new
	 * instances of this class.
	 */
	public Result<FlowPosition> recalculateSolution(SubgoalManager goalManager) {

		return new InstanceCreationStepGoalSolver(klass, goalManager)
				.solution();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((klass == null) ? 0 : klass.hashCode());
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
		InstanceCreationStepGoal other = (InstanceCreationStepGoal) obj;
		if (klass == null) {
			if (other.klass != null)
				return false;
		} else if (!klass.equals(other.klass))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "InstanceCreationStepGoal [klass=" + klass + "]";
	}

}

final class InstanceCreationStepGoalSolver {

	private final Processor<ModelSite<? extends exprType>> processor = new Processor<ModelSite<? extends exprType>>() {

		public void processInfiniteResult() {
			constructors = TopFp.INSTANCE;
		}

		public void processFiniteResult(
				Set<ModelSite<? extends exprType>> classReferences) {
			Set<FlowPosition> positions = new HashSet<FlowPosition>();

			for (ModelSite<? extends exprType> classSite : classReferences) {
				SimpleNode parent = AstParentNodeFinder.findParent(classSite
						.astNode(), classSite.codeObject().ast());

				if (parent instanceof Call) {
					ModelSite<Call> constructor = new ModelSite<Call>(
							(Call) parent, classSite.codeObject());
					positions.add(new ExpressionPosition<Call>(constructor));
				}
			}
			constructors = new FiniteResult<FlowPosition>(positions);
		}
	};

	private Result<FlowPosition> constructors;

	InstanceCreationStepGoalSolver(ClassCO klass, SubgoalManager goalManager) {

		Result<ModelSite<? extends exprType>> classReferences = goalManager
				.registerSubgoal(new FlowGoal(new CodeObjectDefinitionPosition(klass)));

		classReferences.actOnResult(processor);
	}

	public Result<FlowPosition> solution() {
		return constructors;
	}
}
