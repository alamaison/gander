package uk.ac.ic.doc.gander.flowinference.types;

import java.util.Set;

import uk.ac.ic.doc.gander.flowinference.Namespace;
import uk.ac.ic.doc.gander.flowinference.argument.Argument;
import uk.ac.ic.doc.gander.flowinference.argument.ArgumentDestination;
import uk.ac.ic.doc.gander.flowinference.argument.CallsiteArgument;
import uk.ac.ic.doc.gander.flowinference.argument.SelfCallsiteArgument;
import uk.ac.ic.doc.gander.flowinference.call.CallDispatch;
import uk.ac.ic.doc.gander.flowinference.callframe.StackFrame;
import uk.ac.ic.doc.gander.flowinference.callframe.StrategyBasedStackFrame;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;

public final class TBoundMethod implements TCallable {

	private final TCallable unboundMethod;
	private final TObject instance;

	public TBoundMethod(TCallable unboundMethod, TObject instance) {
		if (unboundMethod == null)
			throw new NullPointerException(
					"Bound method must have a corresponding unbound method");
		if (instance == null)
			throw new NullPointerException(
					"Bound methods are bound to an instance of a class");

		// unboundMethod's parent is not necessarily a class

		this.unboundMethod = unboundMethod;
		this.instance = instance;
	}

	@Override
	public String getName() {
		return "<bound method " + instance.getName() + "." + unboundMethod
				+ ">";
	}

	@Override
	public Result<Type> returnType(SubgoalManager goalManager) {
		return unboundMethod.returnType(goalManager);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Members on a method are returned directly from the method's function
	 * object's namespace.
	 */
	@Override
	public Result<Type> memberType(String memberName, SubgoalManager goalManager) {
		return unboundMethod.memberType(memberName, goalManager);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Reading a member from a methods delegates the lookup to the wrapped
	 * unbound function object.
	 */
	@Override
	public Set<Namespace> memberReadableNamespaces() {
		return unboundMethod.memberReadableNamespaces();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * It is a type error to try to write to a member of a bound method, even if
	 * that member was readable (by delegating to the unbound method @see
	 * memberReadableNamespaces). It throws an {@code AttributeError}.
	 */
	@Override
	public Namespace memberWriteableNamespace() {
		System.err.println("UNTYPABLE: Cannot write to an attribute "
				+ "of a bound method :" + this);
		return null;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Positional arguments passed to a call to a bound method are passed to the
	 * parameter of the receiver that is one further along the parameter list
	 * than the argument's position.
	 */
	@Override
	public Result<ArgumentDestination> destinationsReceivingArgument(
			CallsiteArgument argument, SubgoalManager goalManager) {

		if (argument == null) {
			throw new NullPointerException("Argument is not optional");
		}

		Argument actualArgument = argument
				.mapToActualArgument(new MethodStylePassingStrategy(instance));

		return destinationsReceivingArgument(actualArgument, goalManager);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Internally, the adjusted positional arguments are passed on to the
	 * unbound method.
	 */
	@Override
	public Result<ArgumentDestination> destinationsReceivingArgument(
			Argument argument, SubgoalManager goalManager) {

		if (argument == null) {
			throw new NullPointerException("Argument is not optional");
		}

		return unboundMethod.destinationsReceivingArgument(argument,
				goalManager);
	}

	@Override
	public Result<CallDispatch> dispatches(StackFrame<Argument> callFrame,
			SubgoalManager goalManager) {

		StackFrame<Argument> methodCall = new StrategyBasedStackFrame(
				callFrame, new MethodStylePassingStrategy(instance));

		return unboundMethod.dispatches(methodCall, goalManager);
	}

	@Override
	public Result<FlowPosition> flowPositionsCausedByCalling(
			SubgoalManager goalManager) {
		return FiniteResult.bottom();
	}

	@Override
	public Argument selfArgument() {
		return new SelfCallsiteArgument()
				.mapToActualArgument(new MethodStylePassingStrategy(instance));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((instance == null) ? 0 : instance.hashCode());
		result = prime * result
				+ ((unboundMethod == null) ? 0 : unboundMethod.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TBoundMethod other = (TBoundMethod) obj;
		if (instance == null) {
			if (other.instance != null)
				return false;
		} else if (!instance.equals(other.instance))
			return false;
		if (unboundMethod == null) {
			if (other.unboundMethod != null)
				return false;
		} else if (!unboundMethod.equals(other.unboundMethod))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TBoundMethod [" + getName() + "]";
	}

}
