package uk.ac.ic.doc.gander.analysis;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.flowinference.types.TClass;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Namespace;

/**
 * Given an expression, attempt to find the class definition it refers to.
 */
public class ClassResolver {

	private Namespace enclosingScope;
	private Model model;
	private Class klass;

	public ClassResolver(exprType expr, Namespace enclosingScope, Model model) {
		this.enclosingScope = enclosingScope;
		this.model = model;
		klass = resolveClass(expr);
	}

	private Class resolveClass(exprType expr) {
		TypeResolver resolver = new TypeResolver(model);
		Type type = resolver.typeOf(expr, enclosingScope);
		if (type != null && type instanceof TClass) {
			return ((TClass) type).getClassInstance();
		}

		return null;
	}

	public Class getResolvedClass() {
		return klass;
	}
}
