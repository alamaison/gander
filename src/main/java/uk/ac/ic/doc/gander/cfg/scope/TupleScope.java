package uk.ac.ic.doc.gander.cfg.scope;

import org.python.pydev.parser.jython.ast.Tuple;

import uk.ac.ic.doc.gander.cfg.scope.Statement.Exit;

public class TupleScope extends ScopeWithParent {

    private Tuple node;

    public TupleScope(Tuple node, Statement previousStatement,
            Exit trajectory, boolean startInNewBlock, Scope parent) {
        super(parent, previousStatement, trajectory, startInNewBlock);
        this.node = node;
    }

    @Override
    protected Statement doProcess() {
        return delegate(node.elts);
    }

}
