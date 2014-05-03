package uk.ac.ic.doc.gander;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.Name;
import org.python.pydev.parser.jython.ast.Str;
import org.python.pydev.parser.jython.ast.VisitorBase;
import org.python.pydev.parser.jython.ast.exprType;

public class TaggedCallFinder {
    private String variableName;
    private String methodName;
    private String tag;
    private Call call = null;

    /**
     * If we care about target and method names as well as tag.
     * 
     * Does this assume target is a simple variable name?
     */
    public TaggedCallFinder(SimpleNode node, String variableName,
            String methodName, String tag) throws Exception {
        this.variableName = variableName;
        this.methodName = methodName;
        this.tag = tag;
        node.accept(new TagFinder());
    }

    /**
     * If we only care about tag name.
     */
    public TaggedCallFinder(SimpleNode node, String tag) throws Exception {
        this.tag = tag;
        node.accept(new TagFinder());
    }

    boolean isFound() {
        return call != null;
    }

    public Call getTaggedCall() {
        return call;
    }

    static boolean paramsContainTag(Call call, String tag) {
        if (call.args.length < 1)
            return false;

        for (exprType arg : call.args) {
            if (((Str) arg).s.equals(tag))
                return true;
        }

        return false;
    }

    /**
     * Does the call look like a method call and does it match the given
     * criteria.
     * 
     * @param variableName
     *            Expected variable name.
     * @param methodName
     *            Expected method name.
     * @param call
     *            AST Call node being investigated.
     * @return whether the call is a match for the criteria.
     */
    private static boolean isMatchingMethodCall(String variableName,
            String methodName, Call call) {
        if (!CallHelper.isIndirectCall(call))
            return false;
        if (!(CallHelper.indirectCallTarget(call) instanceof Name))
            return false;

        String targetName = ((Name) CallHelper.indirectCallTarget(call)).id;
        if (!targetName.equals(variableName))
            return false;

        return CallHelper.indirectCallName(call).equals(methodName);
    }

    private class TagFinder extends VisitorBase {

        @Override
        public Object visitCall(Call node) throws Exception {

            boolean match;
            if (variableName != null)
                match = isMatchingMethodCall(variableName, methodName, node);
            else
                match = true;

            if (match && paramsContainTag(node, tag)) {
                call = node;
            } else {
                // Calls may contain other calls as parameters so continue
                // digging into AST
                node.traverse(this);
            }
            return null;
        }

        @Override
        protected Object unhandled_node(SimpleNode node) throws Exception {
            return null;
        }

        @Override
        public void traverse(SimpleNode node) throws Exception {
            node.traverse(this);
        }
    }
}