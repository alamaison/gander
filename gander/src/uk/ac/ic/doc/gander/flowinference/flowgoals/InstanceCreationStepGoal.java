package uk.ac.ic.doc.gander.flowinference.flowgoals;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.ast.AstParentNodeFinder;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.ModelSite;

/**
 * Goal modelling the flow of class instances in the first step after creation.
 * 
 * This is the set of all constructor calls that could call the given class.
 */
final class InstanceCreationStepGoal implements FlowStepGoal {

	private final Class klass;
	private final Model model;

	InstanceCreationStepGoal(Class klass, Model model) {
		this.klass = klass;
		this.model = model;
	}

	public Set<FlowPosition> initialSolution() {
		return Collections.emptySet();
	}

	/**
	 * Calls to any of the places the metaclass object flows to result in new
	 * instances of this class.
	 */
	public Set<FlowPosition> recalculateSolution(SubgoalManager goalManager) {

		Set<ModelSite<? extends exprType>> classReferences = goalManager
				.registerSubgoal(new FlowGoal(new CodeObjectPosition(klass,
						model)));

		Set<FlowPosition> constructors = new HashSet<FlowPosition>();

		for (ModelSite<? extends exprType> classSite : classReferences) {
			SimpleNode parent = AstParentNodeFinder.findParent(classSite
					.astNode(), classSite.codeObject().getAst());

			if (parent instanceof Call) {
				ModelSite<Call> constructor = new ModelSite<Call>(
						(Call) parent, classSite.codeObject(), model);
				constructors.add(new ExpressionPosition<Call>(constructor));
			}
		}

		return constructors;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((klass == null) ? 0 : klass.hashCode());
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
		InstanceCreationStepGoal other = (InstanceCreationStepGoal) obj;
		if (klass == null) {
			if (other.klass != null)
				return false;
		} else if (!klass.equals(other.klass))
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
		return "InstanceCreationStepGoal [klass=" + klass + "]";
	}
	
}
