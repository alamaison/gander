package uk.ac.ic.doc.gander.model.parameters;

import java.util.Set;

import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyObject;
import uk.ac.ic.doc.gander.flowinference.argument.Argument;
import uk.ac.ic.doc.gander.flowinference.argument.ArgumentDestination;
import uk.ac.ic.doc.gander.flowinference.callframe.StackFrame;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

public interface FormalParameter {

	InvokableCodeObject codeObject();

	ArgumentDestination passage(Argument argument);

	Set<Variable> boundVariables();

	Set<Argument> argumentsPassedAtCall(StackFrame<Argument> callsite,
			SubgoalManager goalManager);

	/**
	 * When an argument is passed at a call-site explicitly using the given
	 * position, will this parameter accept it?
	 */
	boolean acceptsArgumentByPosition(int position);

	/**
	 * When an argument is passed at a call-site explicitly using the given
	 * keyword, will this parameter accept it?
	 */
	boolean acceptsArgumentByKeyword(String keyword);

	Result<PyObject> objectsPassedAtCall(StackFrame<Argument> stackFrame,
			Variable variable, SubgoalManager goalManager);

}