package uk.ac.ic.doc.cfg.model.scope;

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

		BodyScope scope = new BodyScope(node.body, this);
		scope.process();

		if (node.orelse != null) {
			scope = new BodyScope(node.orelse.body, this);
			scope.process();
			parent.tail(null);
		} else {
			// When no else branch, control falls through directly from the test
			// block
			parent.fallthrough(getCurrentBlock());
		}
	}
}
