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
import uk.ac.ic.doc.gander.model.CallArgumentMapper;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.Namespace;
import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.codeobject.FormalParameter;
import uk.ac.ic.doc.gander.model.codeobject.FunctionCO;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;
import uk.ac.ic.doc.gander.model.codeobject.NamedParameter;

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

	public String getName() {
		return getFunctionInstance().getFullName();
	}

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
	public Result<Type> memberType(String memberName, SubgoalManager goalManager) {

		NamespaceName member = new NamespaceName(memberName,
				functionObject.fullyQualifiedNamespace());
		return goalManager.registerSubgoal(new NamespaceNameTypeGoal(member));
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<Namespace> memberReadableNamespaces() {
		return Collections.<Namespace> singleton(functionObject
				.fullyQualifiedNamespace());
	}

	/**
	 * {@inheritDoc}
	 */
	public Namespace memberWriteableNamespace() {
		return functionObject.fullyQualifiedNamespace();
	}

	@Override
	public Result<Type> typeOfArgumentPassedToParameter(
			FormalParameter parameter, ModelSite<Call> callSite,
			SubgoalManager goalManager) {

		if (parameter instanceof NamedParameter) {
			ModelSite<exprType> passedArgument = expressionFromArgumentList(
					callSite, ((NamedParameter) parameter).index(),
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
								+ functionObject + " at " + callSite);
				return null;
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Ordinal arguments passed to a call to an unbound method are passed
	 * directly to the parameter of the receiver with the same index.
	 */
	public Result<FormalParameter> formalParametersReceivingArgument(
			final Argument argument, SubgoalManager goalManager) {

		if (argument == null) {
			throw new NullPointerException("Argument is not optional");
		}

		FormalParameter parameter = argument
				.passArgumentAtCall(new CallArgumentMapper() {

					@Override
					public FormalParameter parameterAtIndex(int argumentIndex) {
						return functionObject.formalParameters()
								.parameterAtIndex(argumentIndex);
					}

					@Override
					public FormalParameter namedParameter(String parameterName) {
						if (functionObject.formalParameters().hasParameterName(
								parameterName)) {
							return functionObject.formalParameters()
									.namedParameter(parameterName);
						} else {
							System.err.println("No matching parameter in "
									+ functionObject
									+ " for argument passed by keyword: "
									+ argument);
							return null;
						}
					}
				});

		if (parameter != null) {
			return new FiniteResult<FormalParameter>(Collections
					.singleton(parameter));
		} else {
			return TopP.INSTANCE;
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Even if an unbound function object is retrieved as an attribute of
	 * another object and called, that other object doesn't flow anywhere. In
	 * other words, an unbound function object has no self parameter.
	 */
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

	public Result<FlowPosition> flowPositionsCausedByCalling(
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
