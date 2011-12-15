package uk.ac.ic.doc.gander.flowinference.types;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.typegoals.ExpressionTypeGoal;
import uk.ac.ic.doc.gander.flowinference.typegoals.NamespaceNameTypeGoal;
import uk.ac.ic.doc.gander.flowinference.typegoals.TopT;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.codeobject.FunctionCO;
import uk.ac.ic.doc.gander.model.codeobject.NamedParameter;

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

	public int passedArgumentOffset() {
		return 0;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Members on a function are returned directly from the function object's
	 * namespace.
	 */
	public Result<Type> memberType(String memberName, SubgoalManager goalManager) {

		NamespaceName member = new NamespaceName(memberName, functionObject
				.fullyQualifiedNamespace());
		return goalManager.registerSubgoal(new NamespaceNameTypeGoal(member));
	}

	public Result<Type> typeOfArgumentAtNamedParameter(String parameterName,
			ModelSite<Call> callSite, SubgoalManager goalManager) {

		NamedParameter parameter = functionObject.formalParameters()
				.namedParameter(parameterName);

		if (parameter != null) {
			ModelSite<exprType> passedArgument = expressionFromArgumentList(
					callSite, parameter.index(), parameter);
			if (passedArgument != null) {
				return goalManager.registerSubgoal(new ExpressionTypeGoal(
						passedArgument));
			} else {
				return TopT.INSTANCE;
			}
		} else {
			/*
			 * We couldn't find the named parameter. The program is wrong.
			 */
			System.err.println("PROGRAM ERROR: Could not find parameter '"
					+ parameterName + "' in " + functionObject);
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
