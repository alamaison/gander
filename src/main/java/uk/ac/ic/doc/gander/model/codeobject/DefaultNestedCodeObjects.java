package uk.ac.ic.doc.gander.model.codeobject;

import java.util.AbstractSet;
import java.util.Iterator;

import org.python.pydev.parser.jython.ast.stmtType;

import uk.ac.ic.doc.gander.model.Model;

/**
 * Nested code object collection built from parent CodeObject's AST.
 */
final class DefaultNestedCodeObjects extends AbstractSet<NestedCodeObject>
        implements NestedCodeObjects {

    private final CodeObject parentCodeObject;
    private final NestedCodeObjects nestedCodeObjects;

    public DefaultNestedCodeObjects(CodeObject parentCodeObject, Model model) {
        assert parentCodeObject != null;
        assert model != null;

        this.parentCodeObject = parentCodeObject;
        this.nestedCodeObjects = new SetBackedNestedCodeObjects(
                new NestedCodeObjectFinder(parentCodeObject, model));
    }

    @Override
    public Iterator<NestedCodeObject> iterator() {
        return codeObjects().iterator();
    }

    @Override
    public int size() {
        return codeObjects().size();
    }

    @Override
    public NestedCodeObjects namedCodeObjectsDeclaredAs(String declaredName) {
        return codeObjects().namedCodeObjectsDeclaredAs(declaredName);
    }

    @Override
    public NestedCodeObject findCodeObjectMatchingAstNode(stmtType ast) {
        return codeObjects().findCodeObjectMatchingAstNode(ast);
    }

    private NestedCodeObjects codeObjects() {
        return nestedCodeObjects;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime
                * result
                + ((parentCodeObject == null) ? 0 : parentCodeObject.hashCode());
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
        DefaultNestedCodeObjects other = (DefaultNestedCodeObjects) obj;
        if (parentCodeObject == null) {
            if (other.parentCodeObject != null)
                return false;
        } else if (!parentCodeObject.equals(other.parentCodeObject))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "DefaultNestedCodeObjects [parentCodeObject=" + parentCodeObject
                + ", nestedCodeObjects=" + nestedCodeObjects + "]";
    }

}
