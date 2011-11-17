package uk.ac.ic.doc.gander.flowinference.typegoals;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.types.judgement.SetBasedTypeJudgement;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeConcentrator;
import uk.ac.ic.doc.gander.flowinference.types.judgement.TypeJudgement;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Namespace;
import uk.ac.ic.doc.gander.model.codeblock.CodeBlock;

final class ParameterTypeGoal implements TypeGoal {

	private final Namespace enclosingScope;
	private final String name;

	ParameterTypeGoal(Namespace enclosingScope, String name) {
		this.enclosingScope = enclosingScope;
		this.name = name;
	}

	public TypeJudgement initialSolution() {
		return SetBasedTypeJudgement.BOTTOM;
	}

	public TypeJudgement recalculateSolution(SubgoalManager goalManager) {
		CodeBlock codeBlock = enclosingScope.asCodeBlock();

		TypeConcentrator type = new TypeConcentrator();

		if (codeBlock.getNamedFormalParameters().contains(name)) {

			/*
			 * The first parameter of a function in a class (usually called
			 * self) is always an instance of the class so we can trivially
			 * infer its type
			 */
			if (enclosingScope instanceof Function) {
				if (enclosingScope.getParentScope() instanceof Class) {
					type.add(goalManager
							.registerSubgoal(new MethodArgumentTypeGoal((Function) enclosingScope,
									name)));

				} else {
					type.add(goalManager
							.registerSubgoal(new FunctionArgumentTypeGoal(
									(Function) enclosingScope, name)));
				}

			} else {
				assert false;
				// TODO: work out if we need to handle other possibilities
			}
		}

		return type.getJudgement();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((enclosingScope == null) ? 0 : enclosingScope.hashCode());
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
		ParameterTypeGoal other = (ParameterTypeGoal) obj;
		if (enclosingScope == null) {
			if (other.enclosingScope != null)
				return false;
		} else if (!enclosingScope.equals(other.enclosingScope))
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
		return "ParameterTypeGoal [enclosingScope=" + enclosingScope
				+ ", name=" + name + "]";
	}

}
