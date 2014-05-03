package uk.ac.ic.doc.gander.analysis.inheritance;

import java.util.HashMap;
import java.util.Map;

import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.model.Class;

public class CachingInheritanceTree implements InheritanceTree {
    private static Map<Class, Node> roots = new HashMap<Class, Node>();

    private Node root;

    public CachingInheritanceTree(Class klass, TypeResolver resolver) {
        if (!roots.containsKey(klass))
            roots.put(klass, new Node(klass, resolver));
        this.root = roots.get(klass);
    }

    public Node getTree() {
        return root;
    }

}
