package uk.ac.ic.doc.gander.analysis;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.Lambda;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.NameTok;

/**
 * Abstract visitor that traverses the expressions in a basic block.
 * 
 * Expressions in a basic block can be compound expressions that contain other
 * expressions. This visitor digs down into each node so that a subclass can
 * visit any node in the the basic block even if it is nested in another
 * expression.
 * 
 * The exceptions are FunctionDef, ClassDef and Lambda. These nodes have bodies
 * that may include almost any Python statements including statements that
 * change control-flow. When these nodes appear in a basic block, the visitor
 * doesn't traverse into their bodies as those statements are not actually part
 * of the basic block: they are not executed when the basic block executes.
 */
public abstract class BasicBlockTraverser extends BasicBlockVisitor {

	@Override
	public Object visitFunctionDef(FunctionDef node) throws Exception {
		// A nested function def just assigns a callable object to a variable
		// with the same name as the function def. We don't want to traverse
		// into the function body as it isn't being executed as part of the
		// basic block. Instead we treat it as a Name;
		NameTok name = (NameTok) node.name;
		visitName(new Name(name.id, Name.Store, false, name));
		return null;
	}

	@Override
	public Object visitLambda(Lambda node) throws Exception {
		// A lambda is just a callable object. We don't want to traverse
		// into the lambda body as it isn't being executed as part of the
		// basic block.
		return null;
	}

	@Override
	public Object visitClassDef(ClassDef node) throws Exception {
		// A nested class def just assigns a class object to a variable
		// with the same name as the class def. We don't want to traverse
		// into the class implementation as no part of it is being executed
		// in the basic block. Instead we treat it as a Name.
		NameTok name = (NameTok) node.name;
		visitName(new Name(name.id, Name.Store, false, name));
		return null;
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
