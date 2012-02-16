package uk.ac.ic.doc.gander.analysis;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.flowinference.types.TClass;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.OldNamespace;

/**
 * Given an expression, attempt to find the class definition it refers to.
 */
public class ClassResolver {

	private OldNamespace enclosingScope;
	private Class klass;
	private final TypeResolver types;

	public ClassResolver(exprType expr, OldNamespace enclosingScope, TypeResolver types) {
		this.enclosingScope = enclosingScope;
		this.types = types;
		klass = resolveClass(expr);
	}

	private Class resolveClass(exprType expr) {
		Type type = types.typeOf(expr, enclosingScope);
		if (type != null && type instanceof TClass) {
			return ((TClass) type).getClassInstance();
		}

		return null;
	}

	public Class getResolvedClass() {
		return klass;
	}
}
