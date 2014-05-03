package uk.ac.ic.doc.gander.analysis;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.cfg.BasicBlock;

public class CallFinder {

    private final Set<Call> calls = new HashSet<Call>();
    private final EventHandler eventHandler;

    public interface EventHandler {
        void foundCall(Call call);
    }

    public CallFinder(BasicBlock block, EventHandler eventHandler) {
        if (block == null)
            throw new NullPointerException("Need basic block to search");
        if (eventHandler == null)
            throw new NullPointerException("Callback not optional");

        this.eventHandler = eventHandler;

        for (SimpleNode statement : block) {
            try {
                statement.accept(new FinderVisitor());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Iterable<Call> calls() {
        return calls;
    }

    private class FinderVisitor extends BasicBlockTraverser {

        @Override
        public Object visitCall(Call node) throws Exception {

            eventHandler.foundCall(node);
            node.traverse(this);
            return null;
        }
    }
}