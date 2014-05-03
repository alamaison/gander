package uk.ac.ic.doc.gander.analysis;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyFunction;
import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyObject;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.OldNamespace;

/**
 * Given a call, attempt to find the function being called.
 */
public class FunctionResolver {

	private Function function;
	private OldNamespace enclosingFunction;
	private TypeResolver types;

	public FunctionResolver(Call call, OldNamespace enclosingScope,
			TypeResolver types) {
		this.enclosingFunction = enclosingScope;
		this.types = types;
		function = resolveCall(call);
	}

	private Function resolveCall(Call call) {
		PyObject type = types.typeOf(new ModelSite<exprType>(call.func,
				enclosingFunction.codeObject()));
		if (type != null && type instanceof PyFunction) {
			return ((PyFunction) type).getFunctionInstance();
		}

		return null;
	}

	public Function getFunction() {
		return function;
	}
}