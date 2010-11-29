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

		BasicBlock testBlock = getCurrentBlock();
		
		node.test.accept(this);
		
		if (node.body != null) {
			BlockScope scope = new BlockScope(node.body, this);
			scope.process();

			// When no else branch, control falls through directly from the test
			// block
			if (node.orelse == null) {
				assert getCurrentBlock() != null;
				fallthrough(getCurrentBlock());
			}
		}

		if (node.orelse != null) {
			BlockScope scope = new BlockScope(node.orelse.body, this);
			scope.process();
		}
		
		parent.setCurrentBlock(testBlock);
	}
}
