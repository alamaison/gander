package uk.ac.ic.doc.gander.cfg.scope;

import org.python.pydev.parser.jython.SimpleNode;

class BodyScope extends ScopeWithParent {

    private SimpleNode[] nodes;

    protected BodyScope(SimpleNode[] nodes, Statement previousStatement,
            Statement.Exit trajectory, boolean startInNewBlock, Scope parent) {
        super(parent, previousStatement, trajectory, startInNewBlock);
        this.nodes = nodes;
    }

    @Override
    protected Statement doProcess() {
        Statement body = null;
        Statement lastStatement = null;

        if (nodes != null) {
            for (SimpleNode node : nodes) {
                if (node == null)
                    continue;

                Statement statement;
                if (lastStatement == null)
                    statement = delegate(node);
                else
                    statement = buildGraph(node, lastStatement,
                            lastStatement.fallthroughs());

                // The first statement we process decides the inlinks for the
                // entire body
                if (body == null) {
                    body = new Statement();
                    body.inheritInlinksFrom(statement);
                }

                // Fallthroughs are handles by passing them to the next
                // statement as the incoming trajectory. We union all other
                // exits as we don't handles them at this level
                body.inheritAllButFallthroughsFrom(statement);

                lastStatement = statement;

                // An empty fallthrough set from the last statement indicates
                // that any remaining statements in this body are dead code. We
                // don't bother processing those.
                if (statement.fallthroughs().isEmpty())
                    break;
            }
        }

        // Only push up last statement's fallthroughs as the others are
        // dealt with by the body processing loop above
        if (lastStatement != null) {
            assert !body.inlinks().isEmpty();
            body.inheritFallthroughsFrom(lastStatement);
        } else {
            if (body == null)
                body = new Statement();
            body.inlinks().inherit(trajectory());
            body.fallthroughs().inherit(trajectory());
        }

        assert body.exitSize() > 0;
        return body;
    }
}
