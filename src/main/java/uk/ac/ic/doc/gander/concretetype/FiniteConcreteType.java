package uk.ac.ic.doc.gander.concretetype;

import java.util.Collection;

import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.implementation.Implementation;

public final class FiniteConcreteType implements ConcreteType {

	private final FiniteResult<Implementation> result;
	

	public FiniteConcreteType(Collection<? extends Implementation> constituentResults) {
		this.result = new FiniteResult<Implementation>(constituentResults);
	}
	
	@Override
	public void actOnResult(
			Result.Processor<Implementation> action) {
		result.actOnResult(action);
	}

	@Override
	public <R> R transformResult(
			Result.Transformer<Implementation, R> action) {
		return result.transformResult(action);
	}

}
