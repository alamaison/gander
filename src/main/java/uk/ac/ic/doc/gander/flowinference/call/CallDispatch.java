package uk.ac.ic.doc.gander.flowinference.call;

import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyObject;
import uk.ac.ic.doc.gander.flowinference.argument.Argument;
import uk.ac.ic.doc.gander.flowinference.argument.ArgumentDestination;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

/**
 * Models an single, runtime call as executed by the Python interpreter.
 * 
 * Basically, it coordinates the argument-passing dance.
 * 
 * The code object receiving the call is already determined.
 */
public interface CallDispatch {

	/**
	 * Returns the destinations to which the given argument is passed at this
	 * call.
	 * 
	 * This will almost always be a single destination but very rarely, such as
	 * in the case of calling a class constructor where the constructor has been
	 * multiply defined, there may be more than one receiving parameter.
	 */
	Result<ArgumentDestination> destinationsReceivingArgument(
			Argument argument, SubgoalManager goalManager);

	/**
	 * Return the objects that may be bound to the given variable during this
	 * call.
	 * 
	 * The variable must be in the scope of the code object that this call is
	 * invoking.
	 * 
	 * The returned object may be one of the arguments passed to the call, an
	 * object contained within an argument passed to the call, one of the
	 * default arguments in the callable's declaration or (in the case of method
	 * calls) the object whose method is being invoked.
	 * 
	 * The reason for framing the query using the bound variable here rather
	 * than the parameter itself is that a single parameter can bind multiple
	 * variables when the parameter is a tuple.
	 * 
	 * @param variable
	 *            the variable to which the argument's value is bound
	 * @param goalManager
	 *            allows us to solve the argument mapping using type inference
	 */
	Result<PyObject> objectsBoundToVariable(Variable variable,
			SubgoalManager goalManager);

	InvokableCodeObject receiver();
}
