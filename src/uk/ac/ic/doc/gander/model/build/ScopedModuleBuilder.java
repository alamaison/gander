/**
 * 
 */
package uk.ac.ic.doc.gander.model.build;

import java.util.Stack;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.VisitorBase;

import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Package;

/**
 * Build and maintain a stack of model scopes by walking a module's AST.
 * 
 * This abstract base class allows subclasses to react to the creation of a new
 * model scope element and gives access to it's parent scope.
 */
abstract class ScopedModuleBuilder extends VisitorBase {

	private Stack<BuildableScope> scopes = new Stack<BuildableScope>();
	private String moduleName;
	private Package parent;

	public ScopedModuleBuilder(String moduleName, Package parent) {
		this.moduleName = moduleName;
		this.parent = parent;
	}

	protected void traverseScope(BuildableScope scope, SimpleNode node)
			throws Exception {
		scopes.push(scope);
		node.traverse(this);
		scopes.pop();
	}

	/**
	 * Return parent scope of last scope created.
	 */
	protected BuildableScope getScope() {
		return (!scopes.empty()) ? scopes.peek() : null;
	}

	protected abstract void createdModule(Module scope);

	protected abstract void createdClass(Class scope);

	protected abstract void createdFunction(Function scope);

	@Override
	public Object visitModule(org.python.pydev.parser.jython.ast.Module node)
			throws Exception {
		Module module = new Module(node, moduleName, parent);

		createdModule(module);
		traverseScope(module, node);

		assert getScope() == null;

		return null;
	}

	@Override
	public Object visitClassDef(ClassDef node) throws Exception {
		Class klass = new Class(node, getScope());

		createdClass(klass);
		traverseScope(klass, node);

		return null;
	}

	@Override
	public Object visitFunctionDef(FunctionDef node) throws Exception {
		Function function = new Function(node, getScope());

		createdFunction(function);
		traverseScope(function, node);

		return null;
	}
}