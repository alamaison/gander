package uk.ac.ic.doc.gander;

import org.python.pydev.parser.jython.SimpleNode;

import uk.ac.ic.doc.gander.model.codeobject.CodeObject;

public class ScopedAstNode {
    public ScopedAstNode(SimpleNode node, CodeObject scope) {
        this.node = node;
        this.scope = scope;
    }

    public SimpleNode getNode() {
        return node;
    }

    public CodeObject getScope() {
        return scope;
    }

    private SimpleNode node;
    private CodeObject scope;
}