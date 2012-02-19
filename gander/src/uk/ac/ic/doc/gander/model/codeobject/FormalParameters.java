package uk.ac.ic.doc.gander.model.codeobject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.VisitorBase;
import org.python.pydev.parser.jython.ast.argumentsType;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.model.ModelSite;

public final class FormalParameters {

	public static final FormalParameters EMPTY_PARAMETERS = new FormalParameters();

	private final ModelSite<argumentsType> argsNode;
	private final List<FormalParameter> parameters;

	FormalParameters(ModelSite<argumentsType> argsNode) {
		this.argsNode = argsNode;
		this.parameters = buildParameters(argsNode);
	}

	public List<FormalParameter> parameters() {
		return parameters;
	}

	/**
	 * Returns whether the parameter list includes a parameter that could
	 * receive an argument passed with the given keyword.
	 */
	public boolean hasKeywordableParameter(String keyword) {
		return findKeywordableParameter(keyword) != null;
	}

	/**
	 * Returns the parameter that will receive an argument passed with the given
	 * keyword.
	 */
	public NamedParameter keywordableParameter(String keyword) {

		NamedParameter p = findKeywordableParameter(keyword);
		if (p != null) {
			return p;
		} else {
			throw new IllegalArgumentException("Parameter '" + keyword
					+ "' doesn't exist in " + argsNode.codeObject());
		}
	}

	public boolean hasParameterForPosition(int position) {
		return findParameterForPosition(position) != null;
	}

	public FormalParameter passByPosition(int position) {

		FormalParameter p = findParameterForPosition(position);
		if (p != null) {
			return p;
		} else {
			throw new IndexOutOfBoundsException(
					"Parameter out of bounds: Position: " + position
							+ " Parameters: " + argsNode);
		}
	}

	/**
	 * Returns whether the parameter list includes a parameter that will bind a
	 * value to the given variable name.
	 */
	public boolean hasVariableBindingParameter(String variableName) {
		return findVariableBindingParameter(variableName) != null;
	}

	/**
	 * Returns the parameter that will bind a value to the given variable name.
	 */
	public NamedParameter variableBindingParameter(String variableName) {

		NamedParameter p = findVariableBindingParameter(variableName);
		if (p != null) {
			return p;
		} else {
			throw new IllegalArgumentException("No parameter defining '"
					+ variableName + "' exists in " + argsNode.codeObject());
		}
	}

	private FormalParameter findParameterForPosition(int position) {

		if (position < parameters.size()) {

			return parameters.get(position);

		} else {

			if (argsNode.astNode().vararg != null) {
				int starargIndex = position = parameters.size();
				assert starargIndex >= 0;

				return new StarargParameter(argsNode, starargIndex);
			} else {
				return null;
			}
		}
	}

	private NamedParameter findKeywordableParameter(String keyword) {

		for (int i = 0; i < parameters.size(); ++i) {

			FormalParameter p = passByPosition(i);

			if (p instanceof NamedParameter
					&& ((NamedParameter) p).name().equals(keyword)) {
				return (NamedParameter) p;
			}
		}

		return null;
	}

	private NamedParameter findVariableBindingParameter(String variableName) {

		for (int i = 0; i < parameters.size(); ++i) {

			FormalParameter p = passByPosition(i);

			if (p instanceof NamedParameter
					&& ((NamedParameter) p).name().equals(variableName)) {
				return (NamedParameter) p;
			}
		}

		return null;
	}

	private static final class PositionalParameterFactory extends VisitorBase {

		private final int index;
		private final ModelSite<argumentsType> argsNode;

		public PositionalParameterFactory(int index,
				ModelSite<argumentsType> argsNode) {
			this.index = index;
			this.argsNode = argsNode;
		}

		@Override
		public Object visitName(Name node) throws Exception {

			ModelSite<Name> parameterNode = new ModelSite<Name>(node,
					argsNode.codeObject());
			ModelSite<exprType> defaultNode = getDefault(index, argsNode);

			return new NamedParameter(index, parameterNode, defaultNode);
		}

		@Override
		protected Object unhandled_node(SimpleNode node) throws Exception {

			System.err.println("Unknown parameter type: " + node + " in "
					+ argsNode);

			ModelSite<exprType> parameterNode = new ModelSite<exprType>(
					(exprType) node, argsNode.codeObject());

			return new UnrecognisedParameter(parameterNode);
		}

		@Override
		public void traverse(SimpleNode node) throws Exception {
			/* Just mapping so don't traverse */
		}

	}

	private static ModelSite<exprType> getDefault(int parameterIndex,
			ModelSite<argumentsType> argsNode) {

		exprType[] defaults = argsNode.astNode().defaults;

		if (parameterIndex < defaults.length) {
			exprType defaultValue = defaults[parameterIndex];
			if (defaultValue != null) {
				/*
				 * defaults exist in the context of the callable's parent code
				 * object
				 */
				return new ModelSite<exprType>(defaultValue,
						((InvokableCodeObject) argsNode.codeObject()).parent());
			} else {
				return null; // null default means no default
			}
		} else {
			return null;
		}
	}

	private FormalParameters() {
		this.argsNode = null;
		this.parameters = Collections.emptyList();
	}

	private static List<FormalParameter> buildParameters(
			ModelSite<argumentsType> argsNode) {

		List<FormalParameter> bp = new ArrayList<FormalParameter>();

		if (argsNode != null) {
			for (int i = 0; i < argsNode.astNode().args.length; ++i) {

				try {
					bp.add((FormalParameter) argsNode.astNode().args[i]
							.accept(new PositionalParameterFactory(i, argsNode)));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}

		return bp;
	}
}
