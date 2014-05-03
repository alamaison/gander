package uk.ac.ic.doc.gander.cfg.scope;

import org.python.pydev.parser.jython.ast.Yield;

class YieldScope extends ScopeWithParent {

    private Yield node;

    protected YieldScope(Yield node, Statement previousStatement,
            Statement.Exit trajectory, boolean startInNewBlock, Scope parent) {
        super(parent, previousStatement, trajectory, startInNewBlock);
        this.node = node;
    }

    @Override
    protected Statement doProcess() {

        Statement value = delegate(node.value);

        // This isn't really how yield works but, for our purposes, it
        // may suffice.
        // TODO: Handle yield correctly
        Statement statement = new Statement();
        statement.inheritInlinksFrom(value);
        statement.inheritExitsFrom(value);
        return statement;
    }

}
