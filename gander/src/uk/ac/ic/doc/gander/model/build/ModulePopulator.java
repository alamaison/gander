package uk.ac.ic.doc.gander.model.build;

import java.io.IOException;

import org.python.pydev.parser.jython.ParseException;
import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.FunctionDef;

import uk.ac.ic.doc.gander.ast.ScopedAstVisitor;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Namespace;

/**
 * Populate a loadable (module or package) from an AST.
 */
class ModulePopulator extends ScopedAstVisitor<Namespace> {
	private Module loadable;

	// Why do we pass in a namespace rather than making the class generic and
	// creating a new instance of the namespace in createScope(SourceFile)? Because
	// new can't take a generic argument in Java :(
	ModulePopulator(Module loadable) {
		this.loadable = loadable;
	}

	void build(SimpleNode ast) throws ParseException, IOException {

		// The builder should never throw any checked exception apart from
		// parse and io errors.
		// Therefore we swallow any other types of exception and abort
		// noisily (by throwing an unchecked exception) if one occurs.
		try {
			ast.accept(this);
		} catch (ParseException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected Namespace atScope(org.python.pydev.parser.jython.ast.Module node) {
		return loadable;
	}

	@Override
	protected Namespace atScope(FunctionDef node) {
		Function f = new Function(node, getScope());
		Namespace parent = (Namespace) getScope();
		if (parent != null)
			parent.addFunction(f);
		return f;
	}

	@Override
	protected Namespace atScope(ClassDef node) {
		Class c = new Class(node, getScope());
		Namespace parent = (Namespace) getScope();
		if (parent != null)
			parent.addClass(c);
		return c;
	}

	@Override
	public void traverse(SimpleNode node) throws Exception {
		node.traverse(this);
	}

	@Override
	protected Object unhandled_node(SimpleNode node) throws Exception {
		return null;
	}
}
