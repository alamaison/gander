package uk.ac.ic.doc.gander.cfg.scope;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.BinOp;

import uk.ac.ic.doc.gander.cfg.scope.Statement.Exit;

public class BinOpScope extends ScopeWithParent {

    private BinOp node;

    public BinOpScope(BinOp node, Statement previousStatement,
            Exit trajectory, boolean startInNewBlock, Scope parent) {
        super(parent, previousStatement, trajectory, startInNewBlock);
        this.node = node;
    }

    @Override
    protected Statement doProcess() {
        return delegate(new SimpleNode[]{ node.left, node.right });
    }

}
