package uk.ac.ic.doc.gander.flowinference.typegoals;

import java.util.List;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.sendersgoals.FunctionSendersGoal;
import uk.ac.ic.doc.gander.flowinference.types.judgement.SetBasedTypeJudgement;
import uk.ac.ic.doc.gander.flowinference.types.judgement.Top;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeConcentrator;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeJudgement;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.ModelSite;

public class FunctionArgumentTypeGoal implements TypeGoal {

	private final Model model;
	private final Function function;
	private final int argumentIndex;

	public FunctionArgumentTypeGoal(Model model, Function function,
			String argument) {
		this.model = model;
		this.function = function;
		this.argumentIndex = findArgumentIndexInFunction(function, argument);
	}

	public TypeJudgement initialSolution() {
		return SetBasedTypeJudgement.BOTTOM;
	}

	public TypeJudgement recalculateSolution(SubgoalManager goalManager) {
		Set<ModelSite<Call>> callSites = goalManager
				.registerSubgoal(new FunctionSendersGoal(model, function));

		// XXX: HACK. A bit like pruning.
		// if (callSites.size() > 5)
		// return new Top();

		TypeConcentrator types = new TypeConcentrator();
		for (ModelSite<Call> callSite : callSites) {
			if (argumentIndex < callSite.getNode().args.length) {
				types.add(goalManager.registerSubgoal(new ExpressionTypeGoal(
						model, callSite.getEnclosingScope(),
						callSite.getNode().args[argumentIndex])));
			} else {
				/*
				 * Few argument were passed to the function than are declared in
				 * its signature. It's probably expecting default arguments.
				 */
				if (argumentIndex < function.getAst().args.defaults.length) {
					exprType defaultVal = function.getAst().args.defaults[argumentIndex];
					if (defaultVal != null) {
						/*
						 * XXX: Are we sure default arguments are evaluated in
						 * the context of the function's parent?
						 */
						TypeJudgement defaultType = goalManager
								.registerSubgoal(new ExpressionTypeGoal(model,
										function.getParentScope(), defaultVal));
						types.add(defaultType);
					} else {
						/* No default. The program is probably wrong. */
						types.add(new Top());
					}
				} else {
					/*
					 * No idea what's going on here. The defaults array seems to
					 * be smaller than the argument array
					 */
					assert false;
					types.add(new Top());
				}
			}
			if (types.isFinished())
				break;
		}

		return types.getJudgement();
	}

	private static int findArgumentIndexInFunction(Function function,
			String argument) {
		List<ModelSite<exprType>> args = function.asCodeBlock()
				.getFormalParameters();

		for (int i = 0; i < args.size(); ++i) {
			exprType arg = args.get(i).getNode();
			if (arg instanceof Name && ((Name) arg).id.equals(argument))
				return i;
		}

		return -1;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + argumentIndex;
		result = prime * result
				+ ((function == null) ? 0 : function.hashCode());
		result = prime * result + ((model == null) ? 0 : model.hashCode());
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
		FunctionArgumentTypeGoal other = (FunctionArgumentTypeGoal) obj;
		if (argumentIndex != other.argumentIndex)
			return false;
		if (function == null) {
			if (other.function != null)
				return false;
		} else if (!function.equals(other.function))
			return false;
		if (model == null) {
			if (other.model != null)
				return false;
		} else if (!model.equals(other.model))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FunctionArgumentTypeGoal [argumentIndex=" + argumentIndex
				+ ", function=" + function + "]";
	}

}
