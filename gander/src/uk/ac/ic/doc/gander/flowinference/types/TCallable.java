package uk.ac.ic.doc.gander.flowinference.types;

import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.model.Argument;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.FormalParameter;

public interface TCallable extends Type {

	/**
	 * Infers the type of the value returned when this object is called.
	 * 
	 * @param goalManager
	 *            allows us to determine the return type using type inference
	 */
	Result<Type> returnType(SubgoalManager goalManager);

	/**
	 * Return the type of argument that is passed to the named parameter of this
	 * callable when called from the given call-site.
	 * 
	 * The argument may be one of the arguments passed to the call, one of the
	 * default arguments in the callable's declaration or (in the case of method
	 * calls) the type of the bound objects whose method is being invoked.
	 * 
	 * @param parameterName
	 *            the name of the parameter declared in the callable
	 * @param callSite
	 *            an expression known to result in a call to this callable
	 * @param goalManager
	 *            allows us to solve the argument mapping using type inference
	 * 
	 * @return the type of value passed to the named parameter when this
	 *         callable is invoked from the given call-site
	 */
	Result<Type> typeOfArgumentAtNamedParameter(String parameterName,
			ModelSite<Call> callSite, SubgoalManager goalManager);

	/**
	 * Returns the parameters to which the given argument is passed when calling
	 * this type of callable.
	 * 
	 * This will almost always be a single parameter but very rarely, such as in
	 * the case of calling a class constructor where the constructor has been
	 * multiply defined, there may be more than one receiving parameter.
	 */
	Result<FormalParameter> formalParametersReceivingArgument(Argument argument,
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
}
