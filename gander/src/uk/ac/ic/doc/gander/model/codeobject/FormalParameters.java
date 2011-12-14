package uk.ac.ic.doc.gander.model.codeobject;

import java.util.ArrayList;
import java.util.List;

import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.argumentsType;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.model.ModelSite;

public final class FormalParameters {

	public static final FormalParameters EMPTY_PARAMETERS = new FormalParameters();

	private final ModelSite<argumentsType> argsNode;

	FormalParameters(ModelSite<argumentsType> argsNode) {
		this.argsNode = argsNode;
	}

	public List<ModelSite<exprType>> parameters() {

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

	private FormalParameters() {
		this.argsNode = null;
	}

	public List<ModelSite<exprType>> defaults() {

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
}
