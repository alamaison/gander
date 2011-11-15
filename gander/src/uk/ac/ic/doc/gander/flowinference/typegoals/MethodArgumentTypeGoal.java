package uk.ac.ic.doc.gander.flowinference.typegoals;

import java.util.List;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.sendersgoals.FunctionSendersGoal;
import uk.ac.ic.doc.gander.flowinference.types.TObject;
import uk.ac.ic.doc.gander.flowinference.types.judgement.SetBasedTypeJudgement;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeConcentrator;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeJudgement;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.ModelSite;

final class MethodArgumentTypeGoal implements TypeGoal {

	private final Model model;
	private final Function method;
	private final String name;

	MethodArgumentTypeGoal(Model model, Function method, String name) {
		assert method.getParentScope() instanceof Class;
		this.model = model;
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
			return new SetBasedTypeJudgement(new TObject((Class) method
					.getParentScope()));
		} else {
			Set<ModelSite<Call>> callSites = goalManager
					.registerSubgoal(new FunctionSendersGoal(model, method));

			assert argumentIndex > 0;

			TypeConcentrator types = new TypeConcentrator();
			for (ModelSite<Call> callSite : callSites) {
				exprType[] args = callSite.astNode().args;
				if (argumentIndex <= args.length) {
					ModelSite<exprType> argument = new ModelSite<exprType>(
							callSite.astNode().args[argumentIndex - 1],
							callSite.codeObject(), callSite.model());
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
		result = prime * result + ((model == null) ? 0 : model.hashCode());
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
		if (model == null) {
			if (other.model != null)
				return false;
		} else if (!model.equals(other.model))
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
