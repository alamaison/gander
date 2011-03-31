package uk.ac.ic.doc.gander.cfg.model.scope;

import org.python.pydev.parser.jython.ast.Dict;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.cfg.model.scope.Statement.Exit;

public class DictScope extends ScopeWithParent {

	private Dict node;

	public DictScope(Dict node, Statement previousStatement, Exit trajectory,
			boolean startInNewBlock, Scope parent) {
		super(parent, previousStatement, trajectory, startInNewBlock);
		this.node = node;
	}

	@Override
	protected Statement doProcess() throws Exception {
		assert node.keys.length == node.values.length;
		int len = node.keys.length;

		// Dictionary displays are key/datum pairs evaluated left-to-right
		exprType[] elements = new exprType[len * 2];
		for (int i = 0; i < len; i++) {
			elements[2 * i] = node.keys[i];
			elements[2 * i + 1] = node.values[i];
		}

		return delegate(elements);
	}
}
