package uk.ac.ic.doc.gander.flowinference.types;

import uk.ac.ic.doc.gander.flowinference.argument.Argument;
import uk.ac.ic.doc.gander.flowinference.argument.ArgumentDestination;
import uk.ac.ic.doc.gander.flowinference.argument.CallsiteArgument;
import uk.ac.ic.doc.gander.flowinference.call.CallDispatch;
import uk.ac.ic.doc.gander.flowinference.callframe.StackFrame;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.result.Result;

public interface TCallable extends Type {

	/**
	 * Infers the type of the value returned when this object is called.
	 * 
	 * @param goalManager
	 *            allows us to determine the return type using type inference
	 */
	Result<Type> returnType(SubgoalManager goalManager);

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
	 * Returns the argument representing the potential for a hidden 'self' value
	 * to be passed during calls.
	 */
	Argument selfArgument();

	/**
	 * Returns the runtime calls that might be made when this callable object is
	 * called.
	 * 
	 * @param callFrame
	 *            the stack frame containing the arguments passed to the object
	 *            when called; these may be modified before they appear at the
	 *            receiving code object
	 * @param goalManager
	 *            the type inference engine as resolving when the call is
	 *            dispatched requires type inference
	 */
	Result<CallDispatch> dispatches(StackFrame<Argument> callFrame,
			SubgoalManager goalManager);
}
