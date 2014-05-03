package uk.ac.ic.doc.gander.analysis.inheritance;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.model.Class;

public class InheritedMethods {

    public interface ErrorHandler {
        void onResolutionFailure(Class klass, exprType baseExpression);

        void onIncestuousBase(Class klass, exprType baseExpression);

    }

    private static class DoNothingErrorHandler implements ErrorHandler {

        public void onResolutionFailure(Class klass, exprType baseExpression) {
        }

        public void onIncestuousBase(Class klass, exprType baseExpression) {
        }

    }

    private InheritanceTree tree;
    private ErrorHandler errorHandler;

    public InheritedMethods(InheritanceTree tree, ErrorHandler errorHandler) {
        this.tree = tree;
        this.errorHandler = errorHandler;
    }

    public InheritedMethods(InheritanceTree tree) {
        this(tree, new DoNothingErrorHandler());
    }

    public Set<String> methodsInTree() {
        Node root = tree.getTree();
        Set<String> methodNames = new HashSet<String>(root.getKlass()
                .getFunctions().keySet());

        extractMethodsNamesFromTree(root, methodNames);

        return methodNames;
    }

    private void extractMethodsNamesFromTree(Node root, Set<String> methodNames) {
        assert root.getKlass().inheritsFrom().length == root.getBases().length;

        Class klass = root.getKlass();

        methodNames.addAll(klass.getFunctions().keySet());

        Node[] bases = root.getBases();

        for (int i = 0; i < bases.length; ++i) {
            Node base = bases[i];

            if (base == null) {
                errorHandler
                        .onResolutionFailure(klass, klass.inheritsFrom()[i]);
            } else if (base.equals(root)) {
                errorHandler.onIncestuousBase(klass, klass.inheritsFrom()[i]);
            } else {
                extractMethodsNamesFromTree(base, methodNames);
            }
        }
    }

}
