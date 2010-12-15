package uk.ac.ic.doc.cfg.model.scope;

import org.python.pydev.parser.jython.ast.TryExcept;
import org.python.pydev.parser.jython.ast.excepthandlerType;

import uk.ac.ic.doc.cfg.model.BasicBlock;

public class TryExceptScope extends ScopeWithParent {

	private TryExcept node;

	public TryExceptScope(TryExcept node, BasicBlock root, Scope parent) {
		super(parent, root);
		this.node = node;
	}

	@Override
	protected ScopeExits doProcess() throws Exception {

		ScopeExits exits = new ScopeExits();

		// try

		ScopeExits body = new BodyScope(node.body, null, this).process();
		if (body.canRaise()) {
			boolean foundCatchAll = false;
			for (excepthandlerType handler : node.handlers) {

				if (handler.type == null)
					foundCatchAll = true;
				
				ScopeExits handlerBody = new BodyScope(handler.body, null, this)
						.process();
				
				// XXX: This will fail if the no-arg 'return' is the only
				// statement in the try block as body will be empty
				body.linkRaisesTo(handlerBody);

				exits.inheritExitsFrom(handlerBody);
			}
			
			// Unless a handler catches _all_ exception types, we must still
			// propagate the possibility of exceptions upwards
			if (!foundCatchAll) {
				exits.inheritRaisesFrom(body);
			}
		}

		// Don't process the else branch is the body is certain to always
		// raise an exception - the else is dead code in this case
		if (node.orelse != null && body.canFallThrough()) {

			ScopeExits elseBody = new BodyScope(node.orelse.body, null, this)
					.process();
			body.linkFallThroughsTo(elseBody);

			exits.inheritExitsFrom(elseBody);
		} else {
			exits.inheritFallthroughsFrom(body);
		}

		exits.setRoot(body.getRoot());
		return exits;
	}
}
