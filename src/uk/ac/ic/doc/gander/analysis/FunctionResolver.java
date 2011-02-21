package uk.ac.ic.doc.gander.analysis;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.VisitorBase;

import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Module;

/**
 * Given a call, attempt to find the function being called.
 */
class FunctionResolver {

	// TODO: deal with situation where function is not a simple
	// variable name. It might be qualified with a module name, for
	// instance.

	private Function function;
	private Model model;
	private Module enclosingModule;
	private Function enclosingFunction;

	FunctionResolver(Call call, Function enclosingFunction,
			Module enclosingModule, Model model) throws Exception {
		this.enclosingFunction = enclosingFunction;
		this.enclosingModule = enclosingModule;
		this.model = model;
		function = (Function) call.func.accept(new FunctionResolutionVisitor());
	}

	private class FunctionResolutionVisitor extends VisitorBase {

		@Override
		public Object visitAttribute(Attribute node) throws Exception {
			TypeResolver typer = new TypeResolver(model, enclosingModule);
			Type type = typer.typeOf(node.value, enclosingFunction);
			if (type != null
					&& type instanceof uk.ac.ic.doc.gander.flowinference.types.TModule) {
				Module module = model.getTopLevelPackage().getModules().get(
						((Name) node.value).id);
				if (module != null) {
					return module.getFunctions().get(((NameTok) node.attr).id);
				}
			}
			return null;
		}

		@Override
		public Object visitName(Name node) throws Exception {
			// TODO: Handle case where name doesn't correspond to a function
			// in the local module. This can happen with builtins for
			// instance.
			return enclosingModule.getFunctions().get(node.id);
		}

		@Override
		public void traverse(SimpleNode node) throws Exception {
			// Don't traverse by default
		}

		@Override
		protected Object unhandled_node(SimpleNode node) throws Exception {
			// null means function couldn't be resolved
			return null;
		}
	}

	public Function getFunction() {
		return function;
	}
}