package uk.ac.ic.doc.cfg.model;

import org.python.pydev.parser.jython.SimpleNode;

public class BlockScope extends ScopeWithParent {

	private SimpleNode[] nodes;

	public BlockScope(SimpleNode[] nodes, Scope parent) {
		super(parent);
		this.nodes = nodes;
	}

	@Override
	protected void begin() {
		setCurrentBlock(null);
	}

	@Override
	protected void process() throws Exception {
		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i] != null) {
				nodes[i].accept(this);
			}
		}

		// As well as the blocks that subscopes explicitly add to the
		// fallthrough, the last statement in a block should be added to the
		// fallthrough. However, the subscopes don't know if they are the last
		// one so can't do it themselves.
		// They each call tail() on their parent (us) and we use that to
		// add the fallthrough on their behalf.

		BasicBlock tail = getCurrentBlock();
		if (tail != null && !breakoutQueue.contains(tail))
			parent.fallthrough(tail);
	}
}
