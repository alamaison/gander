package uk.ac.ic.doc.gander.model.name_binding;

import uk.ac.ic.doc.gander.model.codeobject.CodeObject;

/**
 * Representation of a location a named variable might bind in.
 * 
 * Variables always resolve to locations that are with respect to a code object.
 * This is the code object whose execution namespace may be access or modified
 * by uses of the variable.
 */
public final class BindingLocation {

    private final String name;
    private final CodeObject codeObject;

    BindingLocation(String name, CodeObject codeObject) {
        if (name == null)
            throw new NullPointerException("Names must actually exits");
        if (name.isEmpty())
            throw new IllegalArgumentException("Names must have characters");
        if (codeObject == null)
            throw new NullPointerException(
                    "Binding locations are always with respect to a "
                            + "code object");

        this.name = name;
        this.codeObject = codeObject;
    }

    public CodeObject codeObject() {
        return codeObject;
    }

    public String name() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((codeObject == null) ? 0 : codeObject.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        BindingLocation other = (BindingLocation) obj;
        if (codeObject == null) {
            if (other.codeObject != null)
                return false;
        } else if (!codeObject.equals(other.codeObject))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "BindingLocation [name=" + name + ", codeObject=" + codeObject
                + "]";
    }

}