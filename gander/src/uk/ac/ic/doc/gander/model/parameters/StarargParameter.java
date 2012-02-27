package uk.ac.ic.doc.gander.model.parameters;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.argumentsType;

import uk.ac.ic.doc.gander.flowinference.argument.Argument;
import uk.ac.ic.doc.gander.flowinference.argument.ArgumentDestination;
import uk.ac.ic.doc.gander.flowinference.callframe.StackFrame;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.TopFp;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

/**
 * Model of a parameter that mops up all the positional arguments passed to the
 * procedure that weren't captured by one of the other parameters.
 * 
 * In Python this looks like {@code arg} in {@code def proc(x, y, *arg)}. The
 * excess argument are packed into a tuple in order of increasing position. For
 * example, if {@code proc} were called as {@code proc(1, 2, 3, 4)},
 * {@code args} would be the tuple {@code (3, 4)}.
 */
final class StarargParameter implements FormalParameter {

	private final ModelSite<argumentsType> argsNode;

	StarargParameter(ModelSite<argumentsType> argsNode) {
		assert argsNode.codeObject() instanceof InvokableCodeObject;

		this.argsNode = argsNode;
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
	public Set<Argument> argumentsPassedAtCall(StackFrame<Argument> callFrame,
			SubgoalManager goalManager) {

		if (callFrame.includesUnknownPositions()) {

			/*
			 * If the call includes arguments whose position is not known, we
			 * give up because the stararg could contain anything.
			 */
			return Collections.<Argument> singleton(UnknownArgument.INSTANCE);

		} else if (starargIndex() >= callFrame.knownPositions().size()) {

			/*
			 * The starargs parameter eats all the arguments passed to a
			 * position equal or higher than the starargs parameter (basically
			 * the positions where it has run out of positional parameters).
			 */

			List<Argument> leftovers = callFrame.knownPositions().subList(
					starargIndex(), callFrame.knownPositions().size());
			return new HashSet<Argument>(leftovers);

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
				((NameTok) argsNode.astNode().vararg).id, codeObject());
		return Collections.singleton(bindingVar);
	}

	@Override
	public boolean acceptsArgumentByPosition(int position) {
		return position >= starargIndex();
	}

	@Override
	public boolean acceptsArgumentByKeyword(String keyword) {
		return false;
	}

	private int starargIndex() {
		return argsNode.astNode().args.length;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((argsNode == null) ? 0 : argsNode.hashCode());
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
		return true;
	}

	@Override
	public String toString() {
		return "StarargParameter [argsNode=" + argsNode + "]";
	}

}
