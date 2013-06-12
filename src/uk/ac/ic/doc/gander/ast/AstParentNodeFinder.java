package uk.ac.ic.doc.gander.ast;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.VisitorBase;

/**
 * AST search that finds the parent of a given node.
 */
public final class AstParentNodeFinder {

	/**
	 * Return the parent node of the given node if it appears in the tree with
	 * the given root.
	 * 
	 * @param subjectNode
	 *            node whose parent we are looking for
	 * @param searchRoot
	 *            root of tree to search
	 * @return the parent of the given node or {@code null} if the node has no
	 *         parent in the search tree
	 * @throws RuntimeException
	 *             if the subject node is not within the search tree
	 */
	public static SimpleNode findParent(SimpleNode subjectNode,
			SimpleNode searchRoot) {
		try {
			return doFindParent(subjectNode, searchRoot);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static SimpleNode doFindParent(final SimpleNode subjectNode,
			SimpleNode searchRoot) throws Exception {

		ParentFinderVisitor finder = new ParentFinderVisitor(subjectNode);
		searchRoot.accept(finder);
		return finder.getParent();
	}
}

final class ParentFinderVisitor extends VisitorBase {

	private final SimpleNode subjectNode;

	private boolean foundParent = false;
	private SimpleNode lastCandidateParent = null;

	ParentFinderVisitor(SimpleNode subjectNode) {
		this.subjectNode = subjectNode;
	}

	SimpleNode getParent() {
		if (!foundParent) {
			throw new RuntimeException("Did not find parent of " + subjectNode);
		} else {
			return lastCandidateParent;
		}
	}

	@Override
	protected Object unhandled_node(SimpleNode node) throws Exception {
		if (!foundParent) {
			lastCandidateParent = node;

			NodeMatcher finder = new NodeMatcher(subjectNode);
			node.traverse(finder);

			if (finder.nodeWasFoundDuringVisit()) {
				foundParent = true;
			}
		}
		return null;
	}

	@Override
	public void traverse(SimpleNode node) throws Exception {
		if (!foundParent) {
			node.traverse(this);
		}
	}
}
