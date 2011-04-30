package uk.ac.ic.doc.gander.analysis.inheritance;

import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Model;

public class FreshInheritanceTree implements InheritanceTree {
	private Node root;

	public FreshInheritanceTree(Class klass, Model model) {
		root = new Node(klass, model);
	}

	public Node getTree() {
		return root;
	}

}
