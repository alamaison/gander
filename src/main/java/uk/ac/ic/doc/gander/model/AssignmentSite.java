package uk.ac.ic.doc.gander.model;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Assign;

import uk.ac.ic.doc.gander.model.codeobject.CodeObject;

/**
 * A point in the code where a value is assigned to the result of an expression
 * via the equals {@code =} symbol.
 * 
 * This class is needed as assignments are compound expressions that can include
 * multiple targets and one value source. This class allows reasoning about a
 * <em>single</em> target.
 * 
 * @param <T>
 *            The type of AST node receiving the result of the assignment.
 */
public final class AssignmentSite<T extends SimpleNode> {
    private final T target;
    private final Assign assignment;
    private final CodeObject enclosingCodeObject;

    public AssignmentSite(Assign assignment, T target, CodeObject codeObject) {
        this.assignment = assignment;
        this.target = target;
        this.enclosingCodeObject = codeObject;
    }

    public CodeObject getEnclosingScope() {
        return enclosingCodeObject;
    }

    public Assign getAssignment() {
        return assignment;
    }

    public T getTarget() {
        return target;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((assignment == null) ? 0 : assignment.hashCode());
        result = prime
                * result
                + ((enclosingCodeObject == null) ? 0 : enclosingCodeObject
                        .hashCode());
        result = prime * result + ((target == null) ? 0 : target.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AssignmentSite<?> other = (AssignmentSite<?>) obj;
        if (assignment == null) {
            if (other.assignment != null)
                return false;
        } else if (!assignment.equals(other.assignment))
            return false;
        if (enclosingCodeObject == null) {
            if (other.enclosingCodeObject != null)
                return false;
        } else if (!enclosingCodeObject.equals(other.enclosingCodeObject))
            return false;
        if (target == null) {
            if (other.target != null)
                return false;
        } else if (!target.equals(other.target))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "AssignmentSite [assignment=" + assignment
                + ", enclosingCodeObject=" + enclosingCodeObject + ", target="
                + target + "]";
    }

}
