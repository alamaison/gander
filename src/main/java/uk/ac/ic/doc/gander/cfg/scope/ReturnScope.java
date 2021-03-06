package uk.ac.ic.doc.gander.cfg.scope;

import org.python.pydev.parser.jython.ast.Return;

class ReturnScope extends ScopeWithParent {

    private Return node;

    protected ReturnScope(Return node, Statement previousStatement,
            Statement.Exit trajectory, boolean startInNewBlock, Scope parent) {
        super(parent, previousStatement, trajectory, startInNewBlock);
        this.node = node;
    }

    @Override
    protected Statement doProcess() {
        Statement returnValue = delegate(node.value);

        Statement statement = new Statement();
        statement.inheritInlinksFrom(returnValue);
        statement.inheritAllButFallthroughsFrom(returnValue);
        statement.convertFallthroughsToReturns(returnValue);

        return statement;
    }

}
