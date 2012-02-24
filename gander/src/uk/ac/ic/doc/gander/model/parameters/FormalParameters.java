package uk.ac.ic.doc.gander.model.parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.Tuple;
import org.python.pydev.parser.jython.ast.VisitorBase;
import org.python.pydev.parser.jython.ast.argumentsType;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

public final class FormalParameters {

	public static final FormalParameters EMPTY_PARAMETERS = new FormalParameters();

	private final ModelSite<argumentsType> argsNode;
	private final List<FormalParameter> parameters;

	public FormalParameters(ModelSite<argumentsType> argsNode) {
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
	public FormalParameter keywordableParameter(String keyword) {

		FormalParameter p = findKeywordableParameter(keyword);
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
	public boolean hasVariableBindingParameter(Variable variable) {
		return findVariableBindingParameter(variable) != null;
	}

	/**
	 * Returns the parameter that will bind a value to the given variable name.
	 */
	public FormalParameter variableBindingParameter(Variable variable) {

		FormalParameter p = findVariableBindingParameter(variable);
		if (p != null) {
			return p;
		} else {
			throw new IllegalArgumentException("No parameter defining '"
					+ variable + "' exists in " + argsNode.codeObject());
		}
	}

	private FormalParameter findParameterForPosition(int position) {

		for (FormalParameter p : parameters) {

			if (p.acceptsArgumentByPosition(position)) {
				return p;
			}
		}

		return null;
	}

	private FormalParameter findKeywordableParameter(String keyword) {

		for (FormalParameter p : parameters) {

			if (p.acceptsArgumentByKeyword(keyword)) {
				return p;
			}
		}

		return null;
	}

	private FormalParameter findVariableBindingParameter(Variable variable) {

		for (FormalParameter p : parameters) {

			if (p.boundVariables().contains(variable)) {
				return p;
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
			ModelSite<exprType> defaultNode = getDefaultForParameter(index,
					argsNode);

			return new NamedParameter(index, parameterNode, defaultNode);
		}

		@Override
		public Object visitTuple(Tuple node) throws Exception {

			if (node.elts.length == 1 && node.endsWithComma) {

				/* not really a tuple, just brackets */
				return node.elts[0].accept(this);

			} else {

				ModelSite<Tuple> parameterNode = new ModelSite<Tuple>(node,
						argsNode.codeObject());
				ModelSite<exprType> defaultNode = getDefaultForParameter(index,
						argsNode);

				return new TupleParameter(index, parameterNode, defaultNode);
			}
		}

		@Override
		protected Object unhandled_node(SimpleNode node) throws Exception {

			System.err.println("Unknown parameter type: " + node + " in "
					+ argsNode);
			return null;
		}

		@Override
		public void traverse(SimpleNode node) throws Exception {
			/* Just mapping so don't traverse */
		}

	}

	private static ModelSite<exprType> getDefaultForParameter(
			int parameterIndex, ModelSite<argumentsType> argsNode) {

		/*
		 * The defaults array isn't the same size as the parameters (in other
		 * words, the parameters without defaults don't have a null entry in the
		 * array). To calculate the mapping between the parameters and defaults
		 * we must align the end of the defaults with the end of the positional
		 * parameters.
		 */
		exprType[] defaults = argsNode.astNode().defaults;
		int defaultOffset = argsNode.astNode().args.length - defaults.length;
		assert (defaultOffset >= 0);

		if (parameterIndex < defaultOffset) {
			/*
			 * Requested parameter below the bottom of the aligned defaults
			 * array so it not one of the parameters that has a default
			 */
			return null;
		} else {

			int defaultsIndex = parameterIndex - defaultOffset;

			if (defaultsIndex >= 0 && defaultsIndex < defaults.length) {

				exprType defaultValue = defaults[defaultsIndex];

				if (defaultValue != null) {
					/*
					 * defaults exist in the context of the callable's parent
					 * code object
					 */
					return new ModelSite<exprType>(defaultValue,
							((InvokableCodeObject) argsNode.codeObject())
									.parent());
				} else {
					return null; // null default means no default
				}

			} else {
				throw new AssertionError("Mismatch between the defaults "
						+ "and paramter arrays; " + defaultsIndex
						+ " is not a valid index into "
						+ Arrays.toString(defaults));
			}
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

			if (argsNode.astNode().vararg != null) {
				bp.add(new StarargParameter(argsNode));
			}

			if (argsNode.astNode().kwarg != null) {
				bp.add(new KwargParameter(argsNode));
			}

		}

		return bp;
	}
}
