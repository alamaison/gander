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
	private final List<ModelSite<exprType>> parameters;
	private final List<ModelSite<exprType>> defaults;

	FormalParameters(ModelSite<argumentsType> argsNode) {
		this.argsNode = argsNode;
		this.parameters = buildParameters();
		this.defaults = buildDefaults();
	}

	public List<ModelSite<exprType>> parameters() {
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

			ModelSite<exprType> p = parameters.get(position);
			try {
				return (FormalParameter) p.astNode().accept(
						new ParameterBuilder(position));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

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

	private NamedParameter makeNamedParameter(int i, ModelSite<Name> p) {
		int firstDefaultOffset = parameters.size() - defaults.size();
		assert firstDefaultOffset >= 0;

		ModelSite<exprType> defaultValue;
		if (i >= firstDefaultOffset) {
			defaultValue = defaults.get(i - firstDefaultOffset);
		} else {
			defaultValue = null;
		}

		return new NamedParameter(i, p, defaultValue);
	}

	private final class ParameterBuilder extends VisitorBase {

		private final int index;

		public ParameterBuilder(int index) {
			this.index = index;
		}

		@Override
		public Object visitName(Name node) throws Exception {
			return makeNamedParameter(index,
					new ModelSite<Name>(node, argsNode.codeObject()));
		}

		@Override
		protected Object unhandled_node(final SimpleNode node) throws Exception {
			System.err.println("Unknown parameter type: " + node + " in "
					+ argsNode);
			return new UnrecognisedParameter(new ModelSite<exprType>(
					(exprType) node, argsNode.codeObject()));
		}

		@Override
		public void traverse(SimpleNode node) throws Exception {
			/* Just mapping so don't traverse */
		}

	}

	private FormalParameters() {
		this.argsNode = null;
		this.parameters = Collections.emptyList();
		this.defaults = Collections.emptyList();
	}


	private List<ModelSite<exprType>> buildParameters() {

		List<ModelSite<exprType>> argumentSites = new ArrayList<ModelSite<exprType>>();
		if (argsNode != null) {
			for (exprType argument : argsNode.astNode().args) {
				// arguments exist in the context of the callable's code object
				argumentSites.add(new ModelSite<exprType>(argument, argsNode
						.codeObject()));
			}
		}

		return argumentSites;
	}

	private List<ModelSite<exprType>> buildDefaults() {

		List<ModelSite<exprType>> defaultSites = new ArrayList<ModelSite<exprType>>();
		if (argsNode != null) {
			for (exprType defaultValue : argsNode.astNode().defaults) {
				if (defaultValue != null) {
					/*
					 * defaults exist in the context of the callable's parent
					 * code object
					 */
					defaultSites.add(new ModelSite<exprType>(defaultValue,
							((InvokableCodeObject) argsNode.codeObject())
									.parent()));
				} else {
					defaultSites.add(null); // null default means no default
				}
			}
		}

		return defaultSites;
	}
}
