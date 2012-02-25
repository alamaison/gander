package uk.ac.ic.doc.gander.flowinference.call;

import uk.ac.ic.doc.gander.flowinference.argument.Argument;
import uk.ac.ic.doc.gander.flowinference.argument.ArgumentDestination;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

/**
 * Models an single, runtime call as executed by the Python interpreter.
 * 
 * Basically, it coordinates the argument-passing dance.
 * 
 * The code object receiving the call is already determined.
 */
public interface Call {

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
	 * Return the arguments that are passed to a parameter that binds them to
	 * the given variable during this call.
	 * 
	 * The variable must be in the scope of the code object that this call is
	 * invoking.
	 * 
	 * The returned argument may be one of the arguments passed to the call, one
	 * of the default arguments in the callable's declaration or (in the case of
	 * method calls) the type of the bound objects whose method is being
	 * invoked.
	 * 
	 * This will almost always be a single argument (as the receiver, the formal
	 * parameter, is already know). There <em>may</em> be an exceedingly unusal
	 * case where there could be more than one argument: if the object being
	 * called invokes the same code object but in different ways; once as, say,
	 * a plain function and another time as a bound method. We weren't able to
	 * reproduce any such case.
	 * 
	 * The main reason for returning a {@link Result} here rather than a single
	 * argument is for the case where we couldn't determine the argument at all,
	 * in which case we return an infinite result.
	 * 
	 * The reason for using the bound variable here rather than the parameter
	 * itself is that a single parameter can bind multiple variables when the
	 * parameter is a tuple.
	 * 
	 * @param variable
	 *            the variable to which the argument's value is bound
	 * @param goalManager
	 *            allows us to solve the argument mapping using type inference
	 */
	Result<Argument> argumentsBoundToVariable(Variable variable,
			SubgoalManager goalManager);
}
