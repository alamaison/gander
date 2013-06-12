package uk.ac.ic.doc.gander.flowinference;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;

public interface TypeEngine {

	/**
	 * Infer the type of the expression.
	 */
	public Result<Type> typeOf(ModelSite<? extends exprType> expression);

	@Deprecated
	public Result<Type> typeOf(exprType expression, CodeObject scope);

}