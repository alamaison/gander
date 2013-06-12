package uk.ac.ic.doc.gander.flowinference.abstractmachine;

import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.flowinference.argument.Argument;
import uk.ac.ic.doc.gander.flowinference.call.CallDispatch;
import uk.ac.ic.doc.gander.flowinference.callframe.StackFrame;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.model.ModelSite;

public interface PyCallable extends PyObject {

	/**
	 * Infers the type of the value returned when this object is called.
	 * 
	 * @param goalManager
	 *            allows us to determine the return type using type inference
	 */
	Result<PyObject> returnType(SubgoalManager goalManager);

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
	Result<FlowPosition> flowPositionsCausedByCalling(
			ModelSite<Call> syntacticCallSite, SubgoalManager goalManager);

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
