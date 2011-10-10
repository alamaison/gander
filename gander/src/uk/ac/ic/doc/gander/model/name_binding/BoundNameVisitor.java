package uk.ac.ic.doc.gander.model.name_binding;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.For;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.Import;
import org.python.pydev.parser.jython.ast.ImportFrom;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.TryExcept;
import org.python.pydev.parser.jython.ast.excepthandlerType;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.ast.BindingStatementVisitor;

/**
 * Find which names are bound in an AST.
 * 
 * By default, only searches the local code block. Subclasses can override the
 * visitor to force traversal into a {@link FunctionDef} or {@link ClassDef}.
 * 
 * How to react when a name is found is left to the subclasses.
 */
abstract class BoundNameVisitor extends BindingStatementVisitor {

	protected abstract void onNameBound(String name);

	/*
	 * After extracting the bound names at each node we traverse the node
	 * because, if they have a body like a for-loop, they may nest other
	 * definitions.
	 */

	/*
	 * It might be possible to do this just by overriding visitName and
	 * visitNameTok and looking at their contexts to decide if they are being
	 * used in a binding context but, for the moment, we do it the long way
	 */

	@Override
	public Object visitTryExcept(TryExcept node) throws Exception {
		for (excepthandlerType handler : node.handlers) {
			if (handler.name instanceof Name) {
				onNameBound(((Name) handler.name).id);
			} else {
				// XXX: No idea what happens here. How could the
				// name of the exception object _not_ be a name?
			}
		}

		node.traverse(this);
		return null;
	}

	@Override
	public Object visitImportFrom(ImportFrom node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitImport(Import node) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object visitFunctionDef(FunctionDef node) throws Exception {

		onNameBound(((NameTok) node.name).id);

		/*
		 * Do NOT recurse into the FunctionDef body. Despite appearances, it is
		 * not part of this code block. It is a declaration of the nested
		 * function's code block. Another way to think about it: the nested
		 * function's body is not being 'executed' now whereas this code block's
		 * body is.
		 */

		return null;
	}

	@Override
	public Object visitFor(For node) throws Exception {
		if (node.target instanceof Name) {
			onNameBound(((Name) node.target).id);
		} else {
			// XXX: No idea what happens here. How could the
			// for-loop variable _not_ be a name?
		}

		node.traverse(this);
		return null;
	}

	@Override
	public Object visitClassDef(ClassDef node) throws Exception {

		onNameBound(((NameTok) node.name).id);

		/*
		 * Do NOT recurse into the ClassDef body. Despite appearances, it is not
		 * part of this code block. It is a declaration of the nested class's
		 * code block. Another way to think about it: the nested class's body is
		 * not being 'executed' now whereas this code block's body is.
		 */

		return null;
	}

	@Override
	public Object visitAssign(Assign node) throws Exception {
		for (exprType lhsExpression : node.targets) {
			if (lhsExpression instanceof Name) {
				onNameBound(((Name) lhsExpression).id);
			}
		}

		node.traverse(this);
		return null;
	}

	@Override
	public void traverse(SimpleNode node) throws Exception {
		// Traverse by default so that we catch all bindings even
		// if they are nested
		node.traverse(this);
	}

	@Override
	protected Object unhandled_node(SimpleNode node) throws Exception {
		return null;
	}

}
