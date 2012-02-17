package uk.ac.ic.doc.gander.flowinference;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Result.Transformer;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.OldNamespace;

public class TypeResolver {

	private final Model model;

	public TypeResolver(Model model) {
		this.model = model;
	}

	public Type typeOf(ModelSite<exprType> expression) {

		Result<Type> types = new ZeroCfaTypeEngine().typeOf(expression);

		return types.transformResult(new Singletoniser());
	}

	private final class Singletoniser implements Transformer<Type, Type> {

		@Override
		public Type transformFiniteResult(java.util.Set<Type> result) {
			if (result.size() == 1) {
				return result.iterator().next();
			} else {
				System.err.println("Oh dear, not a singleton: " + result);
				return null;
			}
		}

		@Override
		public Type transformInfiniteResult() {
			System.err.println("Oh dear, Top");
			return null;
		}
	}
}
