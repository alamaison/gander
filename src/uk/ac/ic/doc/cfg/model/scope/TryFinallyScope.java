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

		// Every method of exiting the try body directs control-flow to
		// the finally body. Therefore we must pass the union of all the
		// exits as the incoming trajectory.
		Statement finallyBody = new BodyScope(node.finalbody.body, tryBody,
				tryBody.allExits(), this).process();

		// The try-body's exit are generally resumed at the end of the finally
		// body. However, if the finally body itself exits in a non-fallthru
		// way, the try-body's exit is cancelled and the finally-body's exit
		// is performed.
		// We only add links to resume the try-body's exits if there is at
		// least one path through the finally-body that falls-through
		if (finallyBody.canFallThrough()) {
			if (tryBody.canFallThrough()) {
				exits.inheritFallthroughsFrom(finallyBody);
			}

			// Any raises in the body fall-through to the finally block and the
			// finally block's fall-throughs become raises.
			if (tryBody.canRaise()) {
				exits.convertFallthroughsToRaises(finallyBody);
			}

			// Any returns in the body fall-through to the finally block and the
			// finally block's fall-throughs become returns.
			if (tryBody.canReturn()) {
				exits.convertFallthroughsToReturns(finallyBody);
			}

			// Any breaks in the body fall-through to the finally block and the
			// finally block's fall-throughs become breaks.
			if (tryBody.canBreak()) {
				exits.convertFallthroughsToBreaks(finallyBody);
			}
		}
		
		// As explained above, any non-fallthrough exits in the finally-body
		// are treated as normal exits.  Fallthroughs are treated as a
		// resumption of the try-body's exit
		exits.inheritAllButFallthroughsFrom(finallyBody);

		assert !tryBody.canContinue(); // currently forbidden by Python spec

		exits.inheritInlinksFrom(tryBody);

		return exits;
	}
}
