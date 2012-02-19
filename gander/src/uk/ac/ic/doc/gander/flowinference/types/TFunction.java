package uk.ac.ic.doc.gander.flowinference.types;

import java.util.Collections;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.flowinference.Argument;
import uk.ac.ic.doc.gander.flowinference.ArgumentPassage;
import uk.ac.ic.doc.gander.flowinference.Namespace;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.typegoals.NamespaceNameTypeGoal;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.codeobject.FormalParameter;
import uk.ac.ic.doc.gander.model.codeobject.FunctionCO;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;

/**
 * Model of Python function object types.
 * 
 * This also models unbound method types as they seem to behave basically the
 * same way except the type of object passed to their first parameter is
 * enforced.
 */
public class TFunction implements TCodeObject, TCallable {

	private final FunctionCO functionObject;

	public TFunction(FunctionCO functionInstance) {
		if (functionInstance == null) {
			throw new NullPointerException("Code object required");
		}

		this.functionObject = functionInstance;
	}

	@Override
	public FunctionCO codeObject() {
		return functionObject;
	}

	@Deprecated
	public TFunction(Function functionInstance) {
		this(functionInstance.codeObject());
	}

	@Deprecated
	public Function getFunctionInstance() {
		return functionObject.oldStyleConflatedNamespace();
	}

	@Override
	public String getName() {
		return getFunctionInstance().getFullName();
	}

	@Override
	public Result<Type> returnType(SubgoalManager goalManager) {
		return new FunctionReturnTypeSolver(goalManager, functionObject)
				.solution();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Members on a function are returned directly from the function object's
	 * namespace.
	 */
	@Override
	public Result<Type> memberType(String memberName, SubgoalManager goalManager) {

		NamespaceName member = new NamespaceName(memberName,
				functionObject.fullyQualifiedNamespace());
		return goalManager.registerSubgoal(new NamespaceNameTypeGoal(member));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Namespace> memberReadableNamespaces() {
		return Collections.<Namespace> singleton(functionObject
				.fullyQualifiedNamespace());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Namespace memberWriteableNamespace() {
		return functionObject.fullyQualifiedNamespace();
	}

	@Override
	public Result<Type> typeOfArgumentPassedToParameter(
			FormalParameter parameter, ModelSite<Call> callSite,
			SubgoalManager goalManager) {

		return parameter.typeAtCall(callSite, goalManager);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Positional arguments passed to a call to a plain function or an unbound
	 * method are passed directly to the parameter of the receiver with the same
	 * index.
	 */
	@Override
	public Result<ArgumentPassage> destinationsReceivingArgument(
			Argument argument, SubgoalManager goalManager) {

		if (argument == null) {
			throw new NullPointerException("Argument is not optional");
		}

		ArgumentPassage parameter = argument.passArgumentAtCall(functionObject,
				new FunctionStylePassingStrategy());

		return new FiniteResult<ArgumentPassage>(
				Collections.singleton(parameter));
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Even if an unbound function object is retrieved as an attribute of
	 * another object and called, that other object doesn't flow anywhere. In
	 * other words, an unbound function object has no self parameter.
	 */
	@Deprecated
	@Override
	public FormalParameter selfParameter() {
		return null;
	}

	@Override
	public Result<InvokableCodeObject> codeObjectsInvokedByCall(
			SubgoalManager goalManager) {
		return new FiniteResult<InvokableCodeObject>(
				Collections.singleton(functionObject));
	}

	@Override
	public Result<FlowPosition> flowPositionsCausedByCalling(
			SubgoalManager goalManager) {
		return FiniteResult.bottom();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * When an unbound function is called as an attribute of another object,
	 * nothing special happens. That object doesn't flow anywhere.
	 */
	@Override
	public Result<FlowPosition> flowPositionsOfHiddenSelfArgument(
			SubgoalManager goalManager) {

		return FiniteResult.bottom();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((functionObject == null) ? 0 : functionObject.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof TFunction))
			return false;
		TFunction other = (TFunction) obj;
		if (functionObject == null) {
			if (other.functionObject != null)
				return false;
		} else if (!functionObject.equals(other.functionObject))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TFunction [" + getName() + "]";
	}

}
