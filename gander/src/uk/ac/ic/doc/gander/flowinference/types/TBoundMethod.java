package uk.ac.ic.doc.gander.flowinference.types;

import java.util.Collections;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.Namespace;
import uk.ac.ic.doc.gander.flowinference.argument.Argument;
import uk.ac.ic.doc.gander.flowinference.argument.ArgumentDestination;
import uk.ac.ic.doc.gander.flowinference.argument.CallsiteArgument;
import uk.ac.ic.doc.gander.flowinference.argument.SelfCallsiteArgument;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.expressionflow.ReceivingParameterPositioner;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.typegoals.ExpressionTypeGoal;
import uk.ac.ic.doc.gander.flowinference.typegoals.NamespaceNameTypeGoal;
import uk.ac.ic.doc.gander.flowinference.typegoals.TopT;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;
import uk.ac.ic.doc.gander.model.parameters.FormalParameter;
import uk.ac.ic.doc.gander.model.parameters.NamedParameter;

public final class TBoundMethod implements TCallable {

	private final InvokableCodeObject unboundMethod;
	private final TObject instance;

	public TBoundMethod(InvokableCodeObject unboundMethod, TObject instance) {
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
		return new FunctionReturnTypeSolver(goalManager, unboundMethod)
				.solution();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Members on a method are returned directly from the method's function
	 * object's namespace.
	 */
	@Override
	public Result<Type> memberType(String memberName, SubgoalManager goalManager) {

		NamespaceName member = new NamespaceName(memberName,
				unboundMethod.fullyQualifiedNamespace());
		return goalManager.registerSubgoal(new NamespaceNameTypeGoal(member));
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Reading a member from a methods delegates the lookup to the wrapped
	 * unbound function object.
	 */
	@Override
	public Set<Namespace> memberReadableNamespaces() {
		return Collections.<Namespace> singleton(unboundMethod
				.fullyQualifiedNamespace());
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

	@Override
	public Result<Type> typeOfArgumentPassedToParameter(
			FormalParameter parameter, ModelSite<Call> callSite,
			SubgoalManager goalManager) {

		if (parameter.equals(selfParameter())) {
			/*
			 * When a bound method is called the argument passed to the first
			 * parameter is the bound instance.
			 */
			return new FiniteResult<Type>(Collections.singleton(instance));
		} else {
			if (parameter instanceof NamedParameter) {
				ModelSite<exprType> passedArgument = expressionFromArgumentList(
						callSite, ((NamedParameter) parameter).index() - 1,
						(NamedParameter) parameter);
				if (passedArgument != null) {
					return goalManager.registerSubgoal(new ExpressionTypeGoal(
							passedArgument));
				} else {
					return TopT.INSTANCE;
				}
			} else {
				return TopT.INSTANCE;
			}
		}
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
				.mapToActualArgument(new MethodStylePassingStrategy());

		ArgumentDestination parameter = actualArgument
				.passArgumentAtCall(unboundMethod);

		if (parameter != null) {
			return new FiniteResult<ArgumentDestination>(
					Collections.singleton(parameter));
		} else {
			return FiniteResult.bottom();
		}
	}

	@Deprecated
	@Override
	public FormalParameter selfParameter() {
		try {
			return unboundMethod.formalParameters().passByPosition(0);
		} catch (IndexOutOfBoundsException e) {
			System.err.println("UNTYPABLE: Unable to find self parameter in "
					+ unboundMethod + ": ");
			e.printStackTrace();
			return null;
		}
	}

	private ModelSite<exprType> expressionFromArgumentList(
			ModelSite<Call> callSite, int index, NamedParameter parameter) {

		if (index < callSite.astNode().args.length) {

			return new ModelSite<exprType>(callSite.astNode().args[index],
					callSite.codeObject());

		} else {
			/*
			 * Fewer argument were passed to the function than are declared in
			 * its signature. It's probably expecting default arguments.
			 */
			ModelSite<exprType> defaultValue = parameter.defaultValue();
			if (defaultValue != null) {
				return defaultValue;
			} else {
				/* No default. The program is wrong. */
				System.err
						.println("PROGRAM ERROR: Too few arguments passed to "
								+ unboundMethod + " at " + callSite);
				return null;
			}
		}
	}

	@Override
	public Result<InvokableCodeObject> codeObjectsInvokedByCall(
			SubgoalManager goalManager) {
		return new FiniteResult<InvokableCodeObject>(
				Collections.singleton(unboundMethod));
	}

	@Override
	public Result<FlowPosition> flowPositionsCausedByCalling(
			SubgoalManager goalManager) {
		return FiniteResult.bottom();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * When this method is called as an attribute of another object, that object
	 * flows to its code object's self parameter.
	 */
	@Override
	public Result<FlowPosition> flowPositionsOfHiddenSelfArgument(
			SubgoalManager goalManager) {

		Result<ArgumentDestination> selfDestinations = destinationsReceivingArgument(
				new SelfCallsiteArgument(), goalManager);

		return selfDestinations
				.transformResult(new ReceivingParameterPositioner());
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
