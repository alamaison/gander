package uk.ac.ic.doc.analysis;

import java.util.ArrayList;
import java.util.List;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.VisitorBase;
import org.python.pydev.parser.jython.ast.expr_contextType;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public class DefUseSeparator extends BasicBlockVisitor {

	private List<IDefUse> ops = new ArrayList<IDefUse>();

	DefUseSeparator(BasicBlock block) throws Exception {
		for (SimpleNode statement : block) {
			statement.accept(this);
		}
	}

	/**
	 * Custom-traverse Assign nodes.
	 * 
	 * The default traversal visits the target before the value. NOT what 
	 * we want.
	 */
	@Override
	public Object visitAssign(Assign node) throws Exception {
		if (node.value != null) {
			node.value.accept(this);
		}
		if (node.targets != null) {
			for (SimpleNode target : node.targets) {
				if (target != null) {
					target.accept(this);
				}
			}
		}
		return null;
	}

	@Override
	public Object visitName(Name node) throws Exception {
		if (node.ctx == expr_contextType.Load) {
			ops.add(new Use(node));
		} else if (node.ctx == expr_contextType.Store) {
			ops.add(new Def(node));
		} else {
			System.err.println("WARNING unhandled name-use context");
		}

		return null;
	}

	List<IDefUse> operations() {
		return ops;
	}

	@Override
	protected Object unhandled_node(SimpleNode node) throws Exception {
		return null;
	}

	@Override
	public void traverse(SimpleNode node) throws Exception {
		node.traverse(this);
	}

}
