package uk.ac.ic.doc.cfg.model;

import org.python.pydev.parser.jython.ast.If;

public class IfScope extends ScopeWithParent {

	private If node;

	public IfScope(If node, Scope parent) throws Exception {
		super(parent);
		this.node = node;
	}

	@Override
	protected void doProcess() throws Exception {

		node.test.accept(this);

		BlockScope scope = new BlockScope(node.body, this);
		scope.process();

		if (node.orelse != null) {
			scope = new BlockScope(node.orelse.body, this);
			scope.process();
			parent.tail(null);
		} else {
			// When no else branch, control falls through directly from the test
			// block
			parent.fallthrough(getCurrentBlock());
		}
	}
}
