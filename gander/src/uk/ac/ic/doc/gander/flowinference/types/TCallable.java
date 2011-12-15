package uk.ac.ic.doc.gander.flowinference.types;

import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.model.ModelSite;

public interface TCallable extends Type {

	/**
	 * Infers the type of the value returned when this object is called.
	 * 
	 * @param goalManager
	 *            allows us to determine the return type using type inference
	 */
	Result<Type> returnType(SubgoalManager goalManager);

	/**
	 * Returns the offset by which passed arguments are shifted when passed to
	 * the formal parameters
	 */
	int passedArgumentOffset();

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
}
