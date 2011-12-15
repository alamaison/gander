package uk.ac.ic.doc.gander.model.codeobject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.python.pydev.parser.jython.ast.Name;
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

	public NamedParameter namedParameter(String parameterName) {
		List<ModelSite<exprType>> parameters = parameters();
		
		for (int i = 0; i < parameters.size(); ++i) {
			ModelSite<exprType> p = parameters.get(i);

			if (p.astNode() instanceof Name
					&& ((Name) p.astNode()).id.equals(parameterName)) {

				return new NamedParameter(i, parameterName, defaults().get(i));
			}
		}
		
		return null;
	}

	private FormalParameters() {
		this.argsNode = null;
		this.parameters = Collections.emptyList();
		this.defaults = Collections.emptyList();
	}

	public List<ModelSite<exprType>> defaults() {
		return Collections.unmodifiableList(defaults);
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
							((CallableCodeObject) argsNode.codeObject())
									.parent()));
				} else {
					defaultSites.add(null); // null default means no default
				}
			}
		}

		return defaultSites;
	}
}
