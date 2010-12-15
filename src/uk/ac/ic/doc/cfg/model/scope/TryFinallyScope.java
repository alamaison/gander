package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.ast.TryFinally;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public class TryFinallyScope extends ScopeWithParent {

	private TryFinally node;

	public TryFinallyScope(TryFinally node, BasicBlock root, Scope parent) {
		super(parent, root);
		this.node = node;
	}

	@Override
	protected ScopeExits doProcess() throws Exception {

		ScopeExits exits = new ScopeExits();

		// try

		ScopeExits body = new BodyScope(node.body, null, this).process();

		// finally

		ScopeExits finallyBody = new BodyScope(node.finalbody.body, null, this)
				.process();
		if (body.canFallThrough()) {
			body.linkFallThroughsTo(finallyBody);
			exits.inheritFallthroughsFrom(finallyBody);
		}

		// Any raises in the body fall-through to the finally block and the
		// finally block's fall-throughs become raises.
		if (body.canRaise()) {
			body.linkRaisesTo(finallyBody);
			exits.convertFallthroughsToRaises(finallyBody);
		}

		// Any returns in the body fall-through to the finally block and the
		// finally block's fall-throughs become returns.
		if (body.canReturn()) {
			body.linkReturnsTo(finallyBody);
			exits.convertFallthroughsToReturns(finallyBody);
		}

		// Any breaks in the body fall-through to the finally block and the
		// finally block's fall-throughs become breaks.
		if (body.canBreak()) {
			body.linkBreaksTo(finallyBody);
			exits.convertFallthroughsToBreaks(finallyBody);
		}

		exits.setRoot(body.getRoot());
		return exits;
	}
}
