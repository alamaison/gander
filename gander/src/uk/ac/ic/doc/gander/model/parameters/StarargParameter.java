package uk.ac.ic.doc.gander.model.parameters;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.argumentsType;

import uk.ac.ic.doc.gander.flowinference.argument.Argument;
import uk.ac.ic.doc.gander.flowinference.argument.ArgumentDestination;
import uk.ac.ic.doc.gander.flowinference.callsite.InternalCallsite;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.TopFp;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

final class StarargParameter implements FormalParameter {

	private final ModelSite<argumentsType> argsNode;
	private final int starargIndex;

	StarargParameter(ModelSite<argumentsType> argsNode, int starargIndex) {
		assert argsNode.codeObject() instanceof InvokableCodeObject;
		assert starargIndex >= 0;

		this.argsNode = argsNode;
		this.starargIndex = starargIndex;
	}

	@Override
	public InvokableCodeObject codeObject() {
		return (InvokableCodeObject) argsNode.codeObject();
	}

	@Override
	public ArgumentDestination passage(Argument argument) {

		return new ArgumentDestination() {

			@Override
			public Result<FlowPosition> nextFlowPositions() {
				return TopFp.INSTANCE;
			}
		};
	}

	@Override
	public Set<Argument> argumentsPassedAtCall(InternalCallsite callsite,
			SubgoalManager goalManager) {

		Set<Argument> arguments = new HashSet<Argument>();

		/*
		 * The starargs parameter eats all the arguments passed to a position
		 * equal or higher than the starargs parameter (basically the positions
		 * where it has run out of positional parameters). So we ask keep asking
		 * for positional arguments until there are no more to give (argument
		 * becomes null)
		 */
		for (int i = starargIndex;; ++i) {
			Argument argument = callsite.argumentExplicitlyPassedAtPosition(i);
			if (argument != null) {
				arguments.add(argument);
			} else {
				break;
			}
		}

		/*
		 * Also, some or all of an expanded iterable argument's values may be
		 * passed to the stararg parameter.
		 * 
		 * TODO: this is tricky because we should really keep track of the
		 * offset into the expanded iterable somehow.
		 */
		arguments.add(callsite
				.argumentThatCouldExpandIntoPosition(starargIndex));

		if (!arguments.isEmpty()) {
			return arguments;
		} else {
			/*
			 * Even if nothing at the callsite ends up at the stararg, it must
			 * still have a value. This is a default empty tuple.
			 */
			return Collections
					.<Argument> singleton(new DefaultStarargsArgument());
		}
	}

	@Override
	public Set<Variable> boundVariables() {
		Variable bindingVar = new Variable(
				((NameTok) argsNode.astNode().vararg).id, argsNode.codeObject());
		return Collections.singleton(bindingVar);
	}

	@Override
	public boolean acceptsArgumentByPosition(int position) {
		return position >= starargIndex;
	}

	@Override
	public boolean acceptsArgumentByKeyword(String keyword) {
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((argsNode == null) ? 0 : argsNode.hashCode());
		result = prime * result + starargIndex;
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
		StarargParameter other = (StarargParameter) obj;
		if (argsNode == null) {
			if (other.argsNode != null)
				return false;
		} else if (!argsNode.equals(other.argsNode))
			return false;
		if (starargIndex != other.starargIndex)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "StarargParameter [argsNode=" + argsNode + ", starargIndex="
				+ starargIndex + "]";
	}

}
