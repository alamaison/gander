package uk.ac.ic.doc.gander.flowinference.types;

import java.util.Collections;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.flowinference.Namespace;
import uk.ac.ic.doc.gander.flowinference.argument.Argument;
import uk.ac.ic.doc.gander.flowinference.argument.NullArgument;
import uk.ac.ic.doc.gander.flowinference.call.CallDispatch;
import uk.ac.ic.doc.gander.flowinference.call.DefaultCallDispatch;
import uk.ac.ic.doc.gander.flowinference.callframe.StackFrame;
import uk.ac.ic.doc.gander.flowinference.callframe.StrategyBasedStackFrame;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.typegoals.namespacename.NamespaceNameTypeGoal;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.codeobject.FunctionCO;

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
	public Result<CallDispatch> dispatches(StackFrame<Argument> callFrame,
			SubgoalManager goalManager) {

		StackFrame<Argument> functionCall = new StrategyBasedStackFrame(
				callFrame, FunctionStylePassingStrategy.INSTANCE);

		return new FiniteResult<CallDispatch>(
				Collections.singleton(new DefaultCallDispatch(functionObject,
						functionCall)));
	}

	@Override
	public Result<FlowPosition> flowPositionsCausedByCalling(
			ModelSite<Call> syntacticCallSite, SubgoalManager goalManager) {
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

	@Override
	public Argument selfArgument() {
		return NullArgument.INSTANCE;
	}
}
