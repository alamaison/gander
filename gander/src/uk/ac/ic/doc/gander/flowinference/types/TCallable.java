package uk.ac.ic.doc.gander.flowinference.types;

import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.flowinference.Argument;
import uk.ac.ic.doc.gander.flowinference.ArgumentPassage;
import uk.ac.ic.doc.gander.flowinference.ArgumentPassingStrategy;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.FormalParameter;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;

public interface TCallable extends Type {

	/**
	 * Infers the type of the value returned when this object is called.
	 * 
	 * @param goalManager
	 *            allows us to determine the return type using type inference
	 */
	Result<Type> returnType(SubgoalManager goalManager);

	/**
	 * Return the type of argument that is passed to the given parameter when
	 * called from the given call-site.
	 * 
	 * The parameter must belong to one of the code objects that receives calls
	 * to this callable.
	 * 
	 * The argument may be one of the arguments passed to the call, one of the
	 * default arguments in the callable's declaration or (in the case of method
	 * calls) the type of the bound objects whose method is being invoked.
	 * 
	 * @param parameter
	 *            the parameter declared in one of the callable's responding
	 *            code objects
	 * @param callSite
	 *            an expression known to result in a call to this callable
	 * @param goalManager
	 *            allows us to solve the argument mapping using type inference
	 * 
	 * @return the type of value passed to the parameter when its code object is
	 *         invoked from the given call-site
	 */
	Result<Type> typeOfArgumentPassedToParameter(FormalParameter parameter,
			ModelSite<Call> callSite, SubgoalManager goalManager);

	/**
	 * Returns the destinations to which the given argument is passed when
	 * calling this type of callable.
	 * 
	 * This will almost always be a single destination but very rarely, such as
	 * in the case of calling a class constructor where the constructor has been
	 * multiply defined, there may be more than one receiving parameter.
	 */
	Result<ArgumentPassage> destinationsReceivingArgument(Argument argument,
			SubgoalManager goalManager);

	/**
	 * Returns the parameter that magically receives the LHS of a call on an
	 * attribute access when that call is received by this type of callable.
	 * 
	 * Otherwise known as the self parameter.
	 * 
	 * @return the parameter or {@code null} if no such parameter exists
	 */
	FormalParameter selfParameter();

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
