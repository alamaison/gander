package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.ast.If;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public class IfScope extends ScopeWithParent {

	private If node;

	public IfScope(If node, BasicBlock root, Scope parent) throws Exception {
		super(parent, root);
		this.node = node;
	}

	@Override
	protected ScopeExits doProcess() throws Exception {

		// if

		ScopeExits condition = (ScopeExits) node.test.accept(this);

		// then

		BodyScope scope = new BodyScope(node.body, null, this);
		ScopeExits thenBody = scope.process();

		// link the test block to the then body
		assert condition.getFallthroughQueue().size() == 1;
		if (thenBody.getRoot() != null) {
			for (BasicBlock b : condition.getFallthroughQueue()) {
				b.link(thenBody.getRoot());
			}
		}

		ScopeExits exits = new ScopeExits();
		exits.union(thenBody);

		// else

		if (node.orelse == null) {
			// When no else branch, control falls through directly from the test
			// block
			for (BasicBlock b : condition.getFallthroughQueue()) {
				exits.fallthrough(b);
			}
		} else {
			scope = new BodyScope(node.orelse.body, null, this);
			ScopeExits elseBody = scope.process();

			// link the test block to the else body
			if (elseBody.getRoot() != null) {
				for (BasicBlock b : condition.getFallthroughQueue()) {
					b.link(elseBody.getRoot());
				}
			}
			exits.union(elseBody);
		}

		exits.setRoot(condition.getRoot());
		return exits;
	}
}
