package uk.ac.ic.doc.gander;

import org.python.pydev.parser.jython.SimpleNode;

import uk.ac.ic.doc.gander.model.Namespace;

public class ScopedAstNode {
	public ScopedAstNode(SimpleNode node, Namespace scope) {
		this.node = node;
		this.scope = scope;
	}

	public SimpleNode getNode() {
		return node;
	}

	public Namespace getScope() {
		return scope;
	}

	private SimpleNode node;
	private Namespace scope;
}