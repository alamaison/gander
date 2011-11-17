package uk.ac.ic.doc.gander.flowinference.typegoals;

import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.types.TClass;
import uk.ac.ic.doc.gander.flowinference.types.TFunction;
import uk.ac.ic.doc.gander.flowinference.types.TObject;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.flowinference.types.judgement.SetBasedTypeJudgement;
import uk.ac.ic.doc.gander.flowinference.types.judgement.Top;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeJudgement;
import uk.ac.ic.doc.gander.model.ModelSite;

public final class ReturnTypeGoal implements TypeGoal {

	private final ModelSite<Call> callSite;

	public ReturnTypeGoal(ModelSite<Call> callSite) {
		this.callSite = callSite;
	}

	public TypeJudgement initialSolution() {
		return SetBasedTypeJudgement.BOTTOM;
	}

	public TypeJudgement recalculateSolution(SubgoalManager goalManager) {
		ModelSite<exprType> callable = new ModelSite<exprType>(callSite
				.astNode().func, callSite.codeObject());

		ExpressionTypeGoal callableTyper = new ExpressionTypeGoal(callable);
		TypeJudgement callableTypes = goalManager
				.registerSubgoal(callableTyper);

		if (callableTypes instanceof SetBasedTypeJudgement) {

			Set<Type> types = ((SetBasedTypeJudgement) callableTypes)
					.getConstituentTypes();
			if (types.size() == 1) {
				Type callableType = types.iterator().next();
				if (callableType instanceof TClass) {
					/*
					 * Calling a class is a constructor call. Constructors are
					 * special functions so we can infer the return type
					 * immediately. It is an instance of the class being called.
					 */
					return new SetBasedTypeJudgement(new TObject(
							((TClass) callableType).getClassInstance()));
				} else if (callableType instanceof TFunction) {
					FunctionReturnTypeGoal typer = new FunctionReturnTypeGoal(
							((TFunction) callableType).getFunctionInstance());
					return goalManager.registerSubgoal(typer);
				}
			}
			/*
			 * TODO: Handle the case where there is more than one possible type.
			 * Just union the possible results.
			 */
		}

		return new Top();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((callSite == null) ? 0 : callSite.hashCode());
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
		ReturnTypeGoal other = (ReturnTypeGoal) obj;
		if (callSite == null) {
			if (other.callSite != null)
				return false;
		} else if (!callSite.equals(other.callSite))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ReturnTypeGoal [callSite=" + callSite + "]";
	}

}
