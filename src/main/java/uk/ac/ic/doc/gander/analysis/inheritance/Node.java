/**
 * 
 */
package uk.ac.ic.doc.gander.analysis.inheritance;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.analysis.ClassResolver;
import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.model.Class;

public class Node {
    private Class klass;
    private Node[] bases;
    private final TypeResolver types;

    public Node(Class klass, TypeResolver types) {
        assert klass != null;

        this.klass = klass;
        this.types = types;
        
        exprType[] baseExpressions = klass.inheritsFrom();
        bases = new Node[baseExpressions.length];

        for (int i = 0; i < baseExpressions.length; ++i) {
            exprType expr = baseExpressions[i];
            Class base = resolveClass(expr, klass);
            if (base != null) {
                if (base.equals(klass))
                    bases[i] = this;
                else
                    bases[i] = new Node(base, types);
            }
        }
    }

    public Node[] getBases() {
        return bases;
    }

    public Class getKlass() {
        return klass;
    }

    private Class resolveClass(exprType expr, Class subclass) {
        return new ClassResolver(expr, subclass.getParentScope(), types)
                .getResolvedClass();
    }
}