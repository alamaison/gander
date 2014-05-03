package uk.ac.ic.doc.gander.model.codeobject;

import java.util.AbstractSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.ClassDef;
import org.python.pydev.parser.jython.ast.DictComp;
import org.python.pydev.parser.jython.ast.FunctionDef;
import org.python.pydev.parser.jython.ast.GeneratorExp;
import org.python.pydev.parser.jython.ast.Lambda;
import org.python.pydev.parser.jython.ast.SetComp;
import org.python.pydev.parser.jython.ast.VisitorBase;

import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Model;

/**
 * Nested code object builder.
 * 
 * Turns an AST node into a set of the code objects declared <em>directly</em>
 * beneath it.
 */
final class NestedCodeObjectFinder extends AbstractSet<NestedCodeObject> {

    private final CodeObject parentCodeObject;
    private final Set<NestedCodeObject> nestedCodeObjects = new HashSet<NestedCodeObject>();
    private final Model model;

    NestedCodeObjectFinder(CodeObject parentCodeObject, Model model) {
        assert parentCodeObject != null;
        assert model != null;

        this.parentCodeObject = parentCodeObject;
        this.model = model;

        try {
            /*
             * We ignore the top of the tree because that is the current code
             * block so we traverse rather than accept
             */
            parentCodeObject.ast().traverse(new Finder());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterator<NestedCodeObject> iterator() {
        return codeObjects().iterator();
    }

    @Override
    public int size() {
        return codeObjects().size();
    }

    private Set<NestedCodeObject> codeObjects() {
        return Collections.unmodifiableSet(nestedCodeObjects);
    }

    private class Finder extends VisitorBase {

        @Override
        public Object visitClassDef(ClassDef node) throws Exception {
            ClassCO classCodeObject = new ClassCO(node, parentCodeObject);
            nestedCodeObjects.add(classCodeObject);
            Class namespace = new Class(classCodeObject, model);
            classCodeObject.setNamespace(namespace);
            namespace.addNestedCodeObjects();

            /*
             * we only want the immediately nested code objects to don't
             * traverse further
             */
            return null;
        }

        @Override
        public Object visitFunctionDef(FunctionDef node) throws Exception {
            FunctionCO functionCodeObject = new FunctionCO(node,
                    parentCodeObject);
            nestedCodeObjects.add(functionCodeObject);
            Function namespace = new Function(functionCodeObject, model);
            functionCodeObject.setNamespace(namespace);
            namespace.addNestedCodeObjects();

            /*
             * we only want the immediately nested code objects to don't
             * traverse further
             */
            return null;
        }

        @Override
        public Object visitDictComp(DictComp node) throws Exception {
            // TODO: comprehension creates new, anonymous, code object
            return null;
        }

        @Override
        public Object visitGeneratorExp(GeneratorExp node) throws Exception {
            // TODO: comprehension creates new, anonymous, code object
            return null;
        }

        @Override
        public Object visitSetComp(SetComp node) throws Exception {
            // TODO: comprehension creates new, anonymous, code object
            return null;
        }

        @Override
        public Object visitLambda(Lambda node) throws Exception {
            // TODO: lambda creates new, anonymous, code object
            return null;
        }

        @Override
        public void traverse(SimpleNode node) throws Exception {
            /* code object declarations may be nested in if/while/etc */
            node.traverse(this);
        }

        @Override
        protected Object unhandled_node(SimpleNode node) throws Exception {
            return null;
        }

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime
                * result
                + ((parentCodeObject == null) ? 0 : parentCodeObject.hashCode());
        result = prime * result + ((model == null) ? 0 : model.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        NestedCodeObjectFinder other = (NestedCodeObjectFinder) obj;
        if (parentCodeObject == null) {
            if (other.parentCodeObject != null)
                return false;
        } else if (!parentCodeObject.equals(other.parentCodeObject))
            return false;
        if (model == null) {
            if (other.model != null)
                return false;
        } else if (!model.equals(other.model))
            return false;
        return true;
    }

}
