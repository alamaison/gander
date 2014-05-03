package uk.ac.ic.doc.gander.ducktype;

import uk.ac.ic.doc.gander.Feature;

/**
 * This is not meant to model a method the way Python thinks about them (as
 * callable attributes.  This is a method in the fake, ideal language semantics.
 * The kind of method that is defined by the class.
 */
public final class NamedMethodFeature implements Feature {

    private final String name;

    public NamedMethodFeature(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
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
        NamedMethodFeature other = (NamedMethodFeature) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "NamedMethodFeature [name=" + name + "]";
    }

}
