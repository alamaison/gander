package uk.ac.ic.doc.gander.analysis;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyClass;
import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyObject;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.OldNamespace;

/**
 * Given an expression, attempt to find the class definition it refers to.
 */
public class ClassResolver {

    private OldNamespace enclosingScope;
    private Class klass;
    private final TypeResolver types;

    public ClassResolver(exprType expr, OldNamespace enclosingScope,
            TypeResolver types) {
        this.enclosingScope = enclosingScope;
        this.types = types;
        klass = resolveClass(expr);
    }

    private Class resolveClass(exprType expr) {
        PyObject type = types.typeOf(new ModelSite<exprType>(expr, enclosingScope
                .codeObject()));
        if (type != null && type instanceof PyClass) {
            return ((PyClass) type).getClassInstance();
        }

        return null;
    }

    public Class getResolvedClass() {
        return klass;
    }
}
