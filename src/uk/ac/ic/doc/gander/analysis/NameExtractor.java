package uk.ac.ic.doc.gander.analysis;

import java.util.ArrayList;
import java.util.List;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.Name;

import uk.ac.ic.doc.gander.cfg.model.BasicBlock;

public class NameExtractor extends BasicBlockVisitor {

	private List<Name> names = new ArrayList<Name>();

	NameExtractor(BasicBlock block) throws Exception {
		for (SimpleNode statement : block) {
			statement.accept(this);
		}
	}

	/**
	 * Custom-traverse Assign nodes.
	 * 
	 * The default traversal visits the target before the value. NOT what we
	 * want.
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
		names.add(node);
		return null;
	}

	List<Name> operations() {
		return names;
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
