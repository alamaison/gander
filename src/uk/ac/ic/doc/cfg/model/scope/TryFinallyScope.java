package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.ast.TryFinally;

public class TryFinallyScope extends ScopeWithParent {

	private TryFinally node;

	public TryFinallyScope(TryFinally node, Statement previousStatement,
			Statement.Exit trajectory, boolean startInNewBlock, Scope parent) {
		super(parent, previousStatement, trajectory, startInNewBlock);
		this.node = node;
	}

	@Override
	protected Statement doProcess() throws Exception {

		Statement exits = new Statement();

		// try

		Statement tryBody = new BodyScope(node.body, previousStatement(),
				trajectory(), this).process();

		// finally

		Statement finallyBody = new BodyScope(node.finalbody.body, tryBody,
				tryBody.fallthroughs(), this).process();
		
		if (tryBody.canFallThrough()) {
			exits.inheritFallthroughsFrom(finallyBody);
		}

		// Any raises in the body fall-through to the finally block and the
		// finally block's fall-throughs become raises.
		if (tryBody.canRaise()) {
			tryBody.linkRaisesTo(finallyBody);
			exits.convertFallthroughsToRaises(finallyBody);
		}

		// Any returns in the body fall-through to the finally block and the
		// finally block's fall-throughs become returns.
		if (tryBody.canReturn()) {
			tryBody.linkReturnsTo(finallyBody);
			exits.convertFallthroughsToReturns(finallyBody);
		}

		// Any breaks in the body fall-through to the finally block and the
		// finally block's fall-throughs become breaks.
		if (tryBody.canBreak()) {
			tryBody.linkBreaksTo(finallyBody);
			exits.convertFallthroughsToBreaks(finallyBody);
		}

		exits.inheritInlinksFrom(tryBody);

		return exits;
	}
}
