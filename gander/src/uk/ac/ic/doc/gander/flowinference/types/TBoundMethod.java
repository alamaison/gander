package uk.ac.ic.doc.gander.flowinference.types;

import java.util.Collections;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.TopP;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.typegoals.ExpressionTypeGoal;
import uk.ac.ic.doc.gander.flowinference.typegoals.NamespaceNameTypeGoal;
import uk.ac.ic.doc.gander.flowinference.typegoals.TopT;
import uk.ac.ic.doc.gander.model.Argument;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.Namespace;
import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.OrdinalArgument;
import uk.ac.ic.doc.gander.model.codeobject.FormalParameter;
import uk.ac.ic.doc.gander.model.codeobject.FunctionCO;
import uk.ac.ic.doc.gander.model.codeobject.NamedParameter;

public final class TBoundMethod implements TCallable {

	private final FunctionCO unboundMethod;
	private final TObject instance;

	public TBoundMethod(FunctionCO unboundMethod, TObject instance) {
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

	public String getName() {
		return "<bound method " + instance.getName() + "."
				+ unboundMethod.declaredName() + ">";
	}

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
	public Namespace memberWriteableNamespace() {
		return null;
	}

	public Result<Type> typeOfArgumentAtNamedParameter(String parameterName,
			ModelSite<Call> callSite, SubgoalManager goalManager) {

		NamedParameter parameter = unboundMethod.formalParameters()
				.namedParameter(parameterName);

		if (parameter != null) {
			if (parameter.index() == 0) {
				/*
				 * When a bound method is called the argument passed to the
				 * first parameter is the bound instance.
				 */
				return new FiniteResult<Type>(Collections.singleton(instance));
			} else {
				ModelSite<exprType> passedArgument = expressionFromArgumentList(
						callSite, parameter.index() - 1, parameter);
				if (passedArgument != null) {
					return goalManager.registerSubgoal(new ExpressionTypeGoal(
							passedArgument));
				} else {
					return TopT.INSTANCE;
				}
			}
		} else {
			/*
			 * We couldn't find the named parameter. The program is wrong.
			 */
			System.err.println("PROGRAM ERROR: Could not find parameter '"
					+ parameterName + "' in " + unboundMethod);
			return TopT.INSTANCE;
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Ordinal arguments passed to a call to a bound method are passed to the
	 * parameter of the receiver that is one further along the parameter list
	 * than the ordinal.
	 */
	public Result<FormalParameter> formalParametersReceivingArgument(
			Argument argument, SubgoalManager goalManager) {

		if (argument instanceof OrdinalArgument) {

			int ordinal = ((OrdinalArgument) argument).ordinal();
			return new FiniteResult<FormalParameter>(
					Collections.singleton(unboundMethod.formalParameters()
							.parameterAtIndex(ordinal + 1)));
		} else {
			// TODO: keywords and starargs
			return TopP.INSTANCE;
		}
	}

	@Override
	public FormalParameter selfParameter() {
		try {
			return unboundMethod.formalParameters().parameterAtIndex(0);
		} catch (IndexOutOfBoundsException e) {
			System.err.println("Unable to find self parameter in "
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

	public Result<FlowPosition> flowPositionsCausedByCalling(
			SubgoalManager goalManager) {
		return FiniteResult.bottom();
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
