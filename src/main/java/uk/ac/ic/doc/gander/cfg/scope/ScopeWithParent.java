package uk.ac.ic.doc.gander.cfg.scope;

import uk.ac.ic.doc.gander.cfg.BasicBlock;

abstract class ScopeWithParent extends Scope {

    private Scope parent;

    protected ScopeWithParent(Scope parent, Statement previousStatement,
            Statement.Exit trajectory, boolean startInNewBlock) {
        super(previousStatement, trajectory, startInNewBlock);
        this.parent = parent;
    }

    @Override
    protected Statement process() {
        assert !trajectory().isEmpty(); // Don't process unreachable code

        Statement exits = doProcess();

        assert exits.exitSize() > 0;
        assert !exits.inlinks().isEmpty();
        
        return exits;
    }

    protected abstract Statement doProcess();

    @Override
    protected BasicBlock newBlock() {
        return parent.newBlock();
    }
}
