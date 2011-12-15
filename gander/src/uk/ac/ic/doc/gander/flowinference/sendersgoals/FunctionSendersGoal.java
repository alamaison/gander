package uk.ac.ic.doc.gander.flowinference.sendersgoals;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.ast.AstParentNodeFinder;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.CodeObjectCreationPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowGoal;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.RedundancyEliminator;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Result.Processor;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.CallableCodeObject;
import uk.ac.ic.doc.gander.model.codeobject.ClassCO;
import uk.ac.ic.doc.gander.model.codeobject.NamedCodeObject;

/**
 * Find any callsites that could call the given function.
 */
public class FunctionSendersGoal implements SendersGoal {
	private final CallableCodeObject callable;

	public FunctionSendersGoal(CallableCodeObject callable) {
		this.callable = callable;
	}

	public Result<ModelSite<Call>> initialSolution() {
		return FiniteResult.bottom();
	}

	public Result<ModelSite<Call>> recalculateSolution(
			final SubgoalManager goalManager) {
		return new FunctionSendersGoalSolver(callable, goalManager).solution();
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

final class FunctionSendersGoalSolver {

	private Result<ModelSite<Call>> callSites;

	public FunctionSendersGoalSolver(CallableCodeObject callable,
			SubgoalManager goalManager) {

		RedundancyEliminator<ModelSite<? extends exprType>> positions = new RedundancyEliminator<ModelSite<? extends exprType>>();
		positions.add(goalManager.registerSubgoal(new FlowGoal(
				new CodeObjectCreationPosition(callable))));

		/*
		 * The value of an __init__ method's code object can be considered as
		 * 'flowing to' the positions of the methods class object.
		 */
		if (!positions.isFinished()
				&& callable instanceof NamedCodeObject
				&& callable.parent() instanceof ClassCO
				&& ((NamedCodeObject) callable).declaredName().equals(
						"__init__")) {
			positions.add(goalManager
					.registerSubgoal(new FlowGoal(
							new CodeObjectCreationPosition((ClassCO) callable
									.parent()))));
		}

		positions.result().actOnResult(
				new Processor<ModelSite<? extends exprType>>() {

					public void processInfiniteResult() {
						callSites = TopS.INSTANCE;
					}

					public void processFiniteResult(
							Set<ModelSite<? extends exprType>> positions) {
						Set<ModelSite<Call>> callSitePositions = new HashSet<ModelSite<Call>>();
						for (ModelSite<? extends exprType> expression : positions) {
							SimpleNode parent = AstParentNodeFinder.findParent(
									expression.astNode(), expression
											.codeObject().ast());
							if (parent instanceof Call) {
								callSitePositions
										.add(new ModelSite<Call>((Call) parent,
												expression.codeObject()));
							}
						}
						callSites = new FiniteResult<ModelSite<Call>>(
								callSitePositions);
					}
				});

	}

	Result<ModelSite<Call>> solution() {
		return callSites;
	}

}
