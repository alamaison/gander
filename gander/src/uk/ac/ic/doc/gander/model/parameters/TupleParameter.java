package uk.ac.ic.doc.gander.model.parameters;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.Tuple;
import org.python.pydev.parser.jython.ast.VisitorBase;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.argument.Argument;
import uk.ac.ic.doc.gander.flowinference.argument.ArgumentDestination;
import uk.ac.ic.doc.gander.flowinference.argument.NullArgument;
import uk.ac.ic.doc.gander.flowinference.callframe.StackFrame;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.typegoals.TopT;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

final class TupleParameter implements FormalParameter {

	private final int index;
	private final ModelSite<Tuple> parameter;
	private final ModelSite<exprType> defaultValue;

	TupleParameter(int index, ModelSite<Tuple> parameter,
			ModelSite<exprType> defaultValue) {
		assert index >= 0;
		assert parameter != null;
		assert parameter.codeObject() instanceof InvokableCodeObject;

		this.index = index;
		this.parameter = parameter;
		this.defaultValue = defaultValue;
	}

	@Override
	public InvokableCodeObject codeObject() {
		return (InvokableCodeObject) parameter.codeObject();
	}

	@Override
	public ArgumentDestination passage(Argument argument) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean acceptsArgumentByPosition(int position) {
		return position == index;
	}

	@Override
	public boolean acceptsArgumentByKeyword(String keyword) {
		return false;
	}

	@Override
	public Set<Variable> boundVariables() {

		final Set<Variable> variables = new HashSet<Variable>();

		try {
			parameter.astNode().accept(new VisitorBase() {

				@Override
				public Object visitName(Name node) throws Exception {

					variables.add(new Variable(node.id, parameter.codeObject()));
					return null;

				}

				@Override
				protected Object unhandled_node(SimpleNode node)
						throws Exception {
					return null;
				}

				@Override
				public void traverse(SimpleNode node) throws Exception {
					/*
					 * Tuple parameter binding names can be deeply nested so
					 * traverse
					 */
					node.traverse(this);
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return variables;
	}

	@Override
	public Set<Argument> argumentsPassedAtCall(StackFrame<Argument> callFrame,
			SubgoalManager goalManager) {

		if (index < callFrame.knownPositions().size()) {

			return Collections.singleton(callFrame.knownPositions().get(index));

		} else if (callFrame.includesUnknownPositions()) {

			return Collections.<Argument> singleton(UnknownArgument.INSTANCE);

		} else if (defaultValue != null) {
			/*
			 * Default arguments are (theoretically) added regardless of whether
			 * there is an expanded iterable that may pass something here
			 * because we can't be sure it will statically. We don't do that
			 * here because the expanded iterable already made us give up
			 * trying.
			 */
			return Collections.<Argument> singleton(new DefaultArgument(
					defaultValue));
		} else {

			/*
			 * Big no-no. We've run out of argument and there is no default to
			 * plug the gap. The program is wrong.
			 */
			System.err.println("PROGRAM ERROR: Too few arguments passed to "
					+ parameter.codeObject() + " at " + callFrame);
			return Collections.<Argument> singleton(NullArgument.INSTANCE);
		}
	}

	@Override
	public Result<Type> objectsPassedAtCall(StackFrame<Argument> stackFrame,
			Variable variable, SubgoalManager goalManager) {
		if (variable == null)
			throw new NullPointerException("Variable required");

		/*
		 * Tuple arguments are extracted from the (possibly nested) iterable
		 * passed to it. We can't reason about what might be in it.
		 */
		if (boundVariables().contains(variable)) {
			return TopT.INSTANCE;
		} else {
			return FiniteResult.bottom();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((parameter == null) ? 0 : parameter.hashCode());
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
		TupleParameter other = (TupleParameter) obj;
		if (parameter == null) {
			if (other.parameter != null)
				return false;
		} else if (!parameter.equals(other.parameter))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TupleParameter [index=" + index + ", parameter=" + parameter
				+ ", defaultValue=" + defaultValue + "]";
	}

}
