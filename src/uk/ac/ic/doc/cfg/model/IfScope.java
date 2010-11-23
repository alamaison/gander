package uk.ac.ic.doc.cfg.model;

import org.python.pydev.parser.jython.ast.If;

public class IfScope extends ScopeWithParent {

	private BasicBlock thenBlock;
	private BasicBlock elseBlock;
	private If node;

	public IfScope(If node, Scope parent) throws Exception {
		super(parent);
		this.node = node;
	}

	@Override
	protected void process() throws Exception {

		// This code is take from If.traverse rather than allowing it to
		// traverse itself as I can't think of a way to distinguish
		// which branch we are in. Although the else branch is held
		// in a 'suite' node in the AST, it doesn't call visitSuite on the
		// visitor.

		if (node.test != null) {
			node.test.accept(this);
		}

		if (node.orelse == null) {
			parent.fallthrough(getCurrentBlock());
		}

		if (node.body != null) {
			thenBlock = newBlock();
			parent.linkAfterCurrent(thenBlock);
			
			setCurrentBlock(thenBlock);
			
			for (int i = 0; i < node.body.length; i++) {
				if (node.body[i] != null) {
					node.body[i].accept(this);
				}
			}
			
			parent.fallthrough(getCurrentBlock());
		}

		if (node.orelse != null) {
			elseBlock = newBlock();
			parent.linkAfterCurrent(elseBlock);
			
			setCurrentBlock(elseBlock);
			
			node.orelse.accept(this);
			
			parent.fallthrough(getCurrentBlock());
		}
	}
}
