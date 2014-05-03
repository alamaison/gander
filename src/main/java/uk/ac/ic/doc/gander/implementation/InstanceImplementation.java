package uk.ac.ic.doc.gander.implementation;

import java.util.HashSet;
import java.util.Set;

import uk.ac.ic.doc.gander.Feature;
import uk.ac.ic.doc.gander.analysis.inheritance.CachingInheritanceTree;
import uk.ac.ic.doc.gander.analysis.inheritance.InheritedMethods;
import uk.ac.ic.doc.gander.ducktype.NamedMethodFeature;
import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.model.codeobject.ClassCO;

public final class InstanceImplementation implements Implementation {

    private final ClassCO klass;
    private final InheritedMethods inheritance;

    public InstanceImplementation(ClassCO klass, TypeResolver resolver) {

        this.klass = klass;
        inheritance = new InheritedMethods(new CachingInheritanceTree(
                klass.oldStyleConflatedNamespace(), resolver));
    }

    @Override
    public boolean definesSupportFor(Feature feature) {

        // TODO: We only compare by name. Matching
        // parameter numbers etc
        // will require more complex logic.
        return features().contains(feature);
    }

    private Set<Feature> features() {
        Set<Feature> features = new HashSet<Feature>();

        Set<String> methods = inheritance.methodsInTree();
        for (String methodName : methods) {
            features.add(new NamedMethodFeature(methodName));
        }

        return features;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((klass == null) ? 0 : klass.hashCode());
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
        InstanceImplementation other = (InstanceImplementation) obj;
        if (klass == null) {
            if (other.klass != null)
                return false;
        } else if (!klass.equals(other.klass))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "InstanceImplementation [klass=" + klass + "]";
    }

}
