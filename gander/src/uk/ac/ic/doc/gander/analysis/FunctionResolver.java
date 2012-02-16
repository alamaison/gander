package uk.ac.ic.doc.gander.analysis;

import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.flowinference.types.TFunction;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.Function;
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
		Type type = types.typeOf(call.func, enclosingFunction);
		if (type != null && type instanceof TFunction) {
			return ((TFunction) type).getFunctionInstance();
		}

		return null;
	}

	public Function getFunction() {
		return function;
	}
}