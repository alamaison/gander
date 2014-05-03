package uk.ac.ic.doc.gander.ast;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.VisitorBase;

/**
 * Visitor that indicates whether any of the nodes it visited matched a given
 * node.
 * 
 * The visitor does not do any traversal itself so the node will only be found
 * if this visitor is given to it directly.
 */
final class NodeMatcher extends VisitorBase {

	private final SimpleNode subjectNode;
	private boolean foundNode = false;

	NodeMatcher(SimpleNode subjectNode) {
		this.subjectNode = subjectNode;
	}

	boolean nodeWasFoundDuringVisit() {
		return foundNode;
	}

	@Override
	protected Object unhandled_node(SimpleNode node) throws Exception {
		if (node.equals(subjectNode))
			foundNode = true;
		return null;
	}

	@Override
	public void traverse(SimpleNode node) throws Exception {
		// Don't traverse because we only want direct matches
	}
}
