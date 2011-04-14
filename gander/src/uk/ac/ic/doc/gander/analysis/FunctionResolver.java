package uk.ac.ic.doc.gander.analysis;

import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.flowinference.types.TFunction;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Model;

/**
 * Given a call, attempt to find the function being called.
 */
public class FunctionResolver {

	private Function function;
	private Model model;
	private Function enclosingFunction;

	public FunctionResolver(Call call, Function enclosingFunction, Model model) {
		this.enclosingFunction = enclosingFunction;
		this.model = model;
		function = resolveCall(call);
	}

	private Function resolveCall(Call call) {
		TypeResolver resolver = new TypeResolver(model);
		Type type = resolver.typeOf(call.func, enclosingFunction);
		if (type != null && type instanceof TFunction) {
			return ((TFunction) type).getFunctionInstance();
		}

		return null;
	}

	public Function getFunction() {
		return function;
	}
}