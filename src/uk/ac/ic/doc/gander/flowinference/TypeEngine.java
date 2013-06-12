package uk.ac.ic.doc.gander.flowinference;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyObject;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;

public interface TypeEngine {

	/**
	 * Infer the type of the expression.
	 */
	public Result<PyObject> typeOf(ModelSite<? extends exprType> expression);

	@Deprecated
	public Result<PyObject> typeOf(exprType expression, CodeObject scope);

}