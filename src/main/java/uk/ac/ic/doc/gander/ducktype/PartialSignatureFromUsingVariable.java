package uk.ac.ic.doc.gander.ducktype;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Name;

import uk.ac.ic.doc.gander.analysis.BasicBlockTraverser;
import uk.ac.ic.doc.gander.analysis.ssa.SSAVariableSubscripts;
import uk.ac.ic.doc.gander.cfg.Cfg;

/**
 * Given a particular use of a variable, return the part of its signature that
 * is derived from uses occurring solely within the same function.
 */
final class PartialSignatureFromUsingVariable {

    Set<Call> buildSignature(Name variableAtLocation,
            Set<SimpleNode> nodesToSearch, Cfg graph) {
        Set<Call> calls = new HashSet<Call>();

        SSAVariableSubscripts ssa = new SSAVariableSubscripts(graph);
        int permittedSubscript = ssa.subscript(variableAtLocation);

        VariableMatcher matcher = new VariableMatcher(nodesToSearch,
                variableAtLocation, permittedSubscript, ssa);
        calls.addAll(matcher.matchingCalls());

        return calls;
    }

    /**
     * Finds calls that target a given SSA subscripted variable.
     */
    private static class VariableMatcher {

        private final Set<Call> calls = new HashSet<Call>();
        private final Name target;
        private final int subscript;
        private final SSAVariableSubscripts renamer;

        public VariableMatcher(Set<SimpleNode> nodes, Name target,
                int subscript, SSAVariableSubscripts renamer) {
            this.target = target;
            this.subscript = subscript;
            this.renamer = renamer;
            for (SimpleNode node : nodes) {
                try {
                    node.accept(new VariableMatcherVisitor());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        private class VariableMatcherVisitor extends BasicBlockTraverser {

            @Override
            public Object visitCall(Call node) throws Exception {
                if (node.func instanceof Attribute) {
                    Attribute fieldAccess = (Attribute) node.func;
                    if (fieldAccess.value instanceof Name) {
                        Name candidate = (Name) fieldAccess.value;

                        if (isMatch(candidate))
                            calls.add(node);
                    }
                }

                // Calls may contain other calls as parameters so continue
                // digging into AST
                node.traverse(this);

                return null;
            }
        }

        private boolean isMatch(Name candidate) {
            return isNameMatch(candidate) && isSubscriptMatch(candidate);
        }

        private boolean isNameMatch(Name candidate) {
            return target.id.equals(candidate.id);
        }

        private boolean isSubscriptMatch(Name candidate) {
            return subscript == renamer.subscript(candidate);
        }

        Set<Call> matchingCalls() {
            return calls;
        }
    }
}