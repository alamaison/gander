package uk.ac.ic.doc.gander.analysis.inheritance;

import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.model.Class;

public class FreshInheritanceTree implements InheritanceTree {
	private Node root;

	public FreshInheritanceTree(Class klass, TypeResolver resolver) {
		root = new Node(klass, resolver);
	}

	public Node getTree() {
		return root;
	}

}
