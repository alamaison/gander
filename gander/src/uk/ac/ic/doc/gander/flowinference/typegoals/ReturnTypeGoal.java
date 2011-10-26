package uk.ac.ic.doc.gander.flowinference.typegoals;

import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.types.TClass;
import uk.ac.ic.doc.gander.flowinference.types.TFunction;
import uk.ac.ic.doc.gander.flowinference.types.TObject;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.flowinference.types.judgement.SetBasedTypeJudgement;
import uk.ac.ic.doc.gander.flowinference.types.judgement.Top;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeJudgement;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Namespace;

public final class ReturnTypeGoal implements TypeGoal {

	private final Model model;
	private final Namespace enclosingScope;
	private final Call node;

	public ReturnTypeGoal(Model model, Namespace enclosingScope, Call node) {
		this.model = model;
		this.enclosingScope = enclosingScope;
		this.node = node;
	}

	public TypeJudgement initialSolution() {
		return SetBasedTypeJudgement.BOTTOM;
	}

	public TypeJudgement recalculateSolution(SubgoalManager goalManager) {
		ExpressionTypeGoal callableTyper = new ExpressionTypeGoal(model,
				enclosingScope, node.func);
		TypeJudgement callableTypes = goalManager
				.registerSubgoal(callableTyper);
		if (callableTypes instanceof SetBasedTypeJudgement) {

			Set<Type> types = ((SetBasedTypeJudgement) callableTypes)
					.getConstituentTypes();
			if (types.size() == 1) {
				Type callable = types.iterator().next();
				if (callable instanceof TClass) {
					/*
					 * Calling a class is a constructor call. Constructors are
					 * special functions so we can infer the return type
					 * immediately. It is an instance of the class being called.
					 */
					return new SetBasedTypeJudgement(new TObject(
							((TClass) callable).getClassInstance()));
				} else if (callable instanceof TFunction) {
					FunctionReturnTypeGoal typer = new FunctionReturnTypeGoal(
							model, ((TFunction) callable).getFunctionInstance());
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
				+ ((enclosingScope == null) ? 0 : enclosingScope.hashCode());
		result = prime * result + ((model == null) ? 0 : model.hashCode());
		result = prime * result + ((node == null) ? 0 : node.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ReturnTypeGoal))
			return false;
		ReturnTypeGoal other = (ReturnTypeGoal) obj;
		if (enclosingScope == null) {
			if (other.enclosingScope != null)
				return false;
		} else if (!enclosingScope.equals(other.enclosingScope))
			return false;
		if (model == null) {
			if (other.model != null)
				return false;
		} else if (!model.equals(other.model))
			return false;
		if (node == null) {
			if (other.node != null)
				return false;
		} else if (!node.equals(other.node))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ReturnTypeGoal [enclosingScope=" + enclosingScope + ", node="
				+ node + "]";
	}

}
