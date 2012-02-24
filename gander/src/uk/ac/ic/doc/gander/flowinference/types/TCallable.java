package uk.ac.ic.doc.gander.flowinference.types;

import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.flowinference.argument.Argument;
import uk.ac.ic.doc.gander.flowinference.argument.ArgumentDestination;
import uk.ac.ic.doc.gander.flowinference.argument.CallsiteArgument;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;
import uk.ac.ic.doc.gander.model.parameters.FormalParameter;

public interface TCallable extends Type {

	/**
	 * Infers the type of the value returned when this object is called.
	 * 
	 * @param goalManager
	 *            allows us to determine the return type using type inference
	 */
	Result<Type> returnType(SubgoalManager goalManager);

	/**
	 * Return the arguments that are passed to the given parameter when called
	 * from the given call-site.
	 * 
	 * The parameter must belong to one of the code objects that receives calls
	 * to this callable.
	 * 
	 * The argument may be one of the arguments passed to the call, one of the
	 * default arguments in the callable's declaration or (in the case of method
	 * calls) the type of the bound objects whose method is being invoked.
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
	 * @param parameter
	 *            the parameter declared in one of the callable's responding
	 *            code objects
	 * @param callSite
	 *            an expression known to result in a call to this callable
	 * @param goalManager
	 *            allows us to solve the argument mapping using type inference
	 */
	Result<Argument> argumentsPassedToParameter(FormalParameter parameter,
			ModelSite<Call> callSite, SubgoalManager goalManager);

	/**
	 * Returns the destinations to which the given argument is passed when
	 * calling this type of callable from a callsite.
	 * 
	 * This is effectively {@code parametersReceivingArgument()} but Python's,
	 * weird and wonder parameter passing possibilities mean that an argument
	 * may end up <em>inside</em> a parameter rather than at the parameter
	 * itself. {@link ArgumentDestination} abstracts over that.
	 * 
	 * This will almost always be a single destination but very rarely, such as
	 * in the case of calling a class constructor where the constructor has been
	 * multiply defined, there may be more than one receiving parameter.
	 */
	Result<ArgumentDestination> destinationsReceivingArgument(
			CallsiteArgument argument, SubgoalManager goalManager);

	/**
	 * Returns the destinations to which the given argument is passed when
	 * calling this type of callable internally.
	 * 
	 * This will almost always be a single destination but very rarely, such as
	 * in the case of calling a class constructor where the constructor has been
	 * multiply defined, there may be more than one receiving parameter.
	 */
	Result<ArgumentDestination> destinationsReceivingArgument(
			Argument argument, SubgoalManager goalManager);

	/**
	 * Returns the codeObjects that might be invoked when this type of callable
	 * object is called.
	 */
	Result<InvokableCodeObject> codeObjectsInvokedByCall(
			SubgoalManager goalManager);

	/**
	 * Returns the flow positions that the result of calling an object of this
	 * type can flow to in one step purely by virtue of being the result of a
	 * call.
	 * 
	 * This does not include how it can flow by being assigned (or otherwise
	 * bound) to another expression. It only includes flow that happens through
	 * the very act of being a call result. The only example of this is if the
	 * call was a constructor call when the value flows to the {@code self}
	 * parameter of the class's methods.
	 * 
	 * @param goalManager
	 *            allows us to determine the flow positions using type inference
	 */
	Result<FlowPosition> flowPositionsCausedByCalling(SubgoalManager goalManager);

	/**
	 * Returns the flow positions that the LHS of an attribute might flow to
	 * when this callable is called as that attribute.
	 * 
	 * In other words, where does the magically-inserted self argument flow.
	 */
	Result<FlowPosition> flowPositionsOfHiddenSelfArgument(
			SubgoalManager goalManager);
}
