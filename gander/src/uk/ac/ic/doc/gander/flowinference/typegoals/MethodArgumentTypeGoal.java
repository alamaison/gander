package uk.ac.ic.doc.gander.flowinference.typegoals;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.sendersgoals.FunctionSendersGoal;
import uk.ac.ic.doc.gander.flowinference.types.TObject;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.ModelSite;

final class MethodArgumentTypeGoal implements TypeGoal {

	private final Function method;
	private final String name;

	MethodArgumentTypeGoal(Function method, String name) {
		assert method.getParentScope() instanceof Class;
		this.method = method;
		this.name = name;
	}

	public TypeJudgement initialSolution() {
		return SetBasedTypeJudgement.BOTTOM;
	}

	public TypeJudgement recalculateSolution(SubgoalManager goalManager) {
		/*
		 * The first parameter of a function in a class (usually called self) is
		 * always an instance of the class so we can trivially infer its type
		 */
		int argumentIndex = findArgumentIndexInFunction(method, name);

		if (argumentIndex == 0) {
			return new SetBasedTypeJudgement(Collections.singleton(new TObject(
					(Class) method.getParentScope())));
		} else {
			Set<ModelSite<Call>> callSites = goalManager
					.registerSubgoal(new FunctionSendersGoal(method));

			assert argumentIndex > 0;

			TypeConcentrator types = new TypeConcentrator();
			for (ModelSite<Call> callSite : callSites) {
				exprType[] args = callSite.astNode().args;
				if (argumentIndex <= args.length) {
					ModelSite<exprType> argument = new ModelSite<exprType>(
							callSite.astNode().args[argumentIndex - 1],
							callSite.codeObject());
					types.add(goalManager
							.registerSubgoal(new ExpressionTypeGoal(argument)));
				} else {
					// TODO: probably using the default argument
				}
				if (types.isFinished())
					break;
			}

			return types.getJudgement();
		}
	}

	private static int findArgumentIndexInFunction(Function function,
			String argument) {
		List<String> args = function.asCodeBlock().getNamedFormalParameters();

		for (int i = 0; i < args.size(); ++i) {
			if (args.get(i).equals(argument))
				return i;
		}

		return -1;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		MethodArgumentTypeGoal other = (MethodArgumentTypeGoal) obj;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MethodArgumentTypeGoal [method=" + method + ", name=" + name
				+ "]";
	}

}
