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

	public boolean hasParameterName(String parameterName) {
		return findNamedParameter(parameterName) != null;
	}

	public NamedParameter namedParameter(String parameterName) {

		NamedParameter p = findNamedParameter(parameterName);
		if (p != null) {
			return p;
		} else {
			throw new IllegalArgumentException("Parameter '" + parameterName
					+ "' doesn't exist in " + argsNode.codeObject());
		}
	}

	public FormalParameter parameterAtIndex(int i) {

		if (i >= parameters.size()) {
			throw new IndexOutOfBoundsException(
					"Parameter out of bounds: Index: " + i + " Parameters: "
							+ argsNode);
		}

		ModelSite<exprType> p = parameters.get(i);
		try {
			return (FormalParameter) p.astNode()
					.accept(new ParameterBuilder(i));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private NamedParameter findNamedParameter(String parameterName) {

		for (int i = 0; i < parameters.size(); ++i) {

			FormalParameter p = parameterAtIndex(i);

			if (p instanceof NamedParameter
					&& ((NamedParameter) p).name().equals(parameterName)) {
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
			return makeNamedParameter(index, new ModelSite<Name>(node, argsNode
					.codeObject()));
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

	public List<String> parameterNames() {
		ArrayList<String> args = new ArrayList<String>();
		for (ModelSite<exprType> expr : parameters()) {
			if (expr.astNode() instanceof Name) {
				args.add(((Name) expr.astNode()).id);
			} else {
				/*
				 * TODO: Work out what we want to do ... and why this even
				 * happens. I've seen this be a Tuple, for instance.
				 */
				System.err.println("TODO: odd parameter: " + expr);
			}
		}
		return args;
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
