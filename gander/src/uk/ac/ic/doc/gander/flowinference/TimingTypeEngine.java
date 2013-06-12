package uk.ac.ic.doc.gander.flowinference;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;

public class TimingTypeEngine implements TypeEngine {

	private final TypeEngine inner;
	private long timeSheet = 0;

	public TimingTypeEngine(TypeEngine engineToWrap) {
		inner = engineToWrap;
	}

	@Override
	public Result<Type> typeOf(exprType expression, CodeObject scope) {
		long start = System.currentTimeMillis();
		Result<Type> result = inner.typeOf(expression, scope);
		timeSheet += System.currentTimeMillis() - start;
		return result;
	}

	@Override
	public Result<Type> typeOf(ModelSite<? extends exprType> expression) {
		long start = System.currentTimeMillis();
		Result<Type> result = inner.typeOf(expression);
		timeSheet += System.currentTimeMillis() - start;
		return result;
	}

	public long milliseconds() {
		return timeSheet;
	}
};