package uk.ac.ic.doc.gander.model.name_binding;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.VisitorBase;

/**
 * Find which names being bound in an AST.
 * 
 * Traverses into the bodies of nested code blocks such as a {@link FunctionDef}
 * or {@link ClassDef}. This is most useful when establishing bindings for a
 * whole module.
 * 
 * How to react when a name is found is left to the subclasses.
 */
public abstract class BoundNameFinder extends VisitorBase {

	protected abstract void onNameBound(String name);

	@Override
	public void traverse(SimpleNode node) throws Exception {
		// Do not traverse. We delegate visiting the tree to the inner
		// BoundNameVisitor
	}

	@Override
	protected Object unhandled_node(SimpleNode node) throws Exception {
		return node.accept(new BoundNameVisitor() {

			@Override
			public Object visitFunctionDef(FunctionDef node) throws Exception {
				Object result = super.visitFunctionDef(node);
				node.traverse(this);
				return result;
			}

			@Override
			public Object visitClassDef(ClassDef node) throws Exception {
				Object result = super.visitClassDef(node);
				node.traverse(this);
				return result;
			}
			
			// TODO: Lambdas and genexps

			@Override
			protected void onNameBound(String name) {
				BoundNameFinder.this.onNameBound(name);
			}
		});
	}

}
