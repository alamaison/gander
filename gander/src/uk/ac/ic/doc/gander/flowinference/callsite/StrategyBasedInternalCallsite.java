package uk.ac.ic.doc.gander.flowinference.callsite;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.flowinference.argument.Argument;
import uk.ac.ic.doc.gander.flowinference.argument.ArgumentFactory;
import uk.ac.ic.doc.gander.flowinference.argument.CallsiteArgument;
import uk.ac.ic.doc.gander.model.ModelSite;

public final class StrategyBasedInternalCallsite implements InternalCallsite {

	private final Set<Argument> arguments = new HashSet<Argument>();

	public StrategyBasedInternalCallsite(ModelSite<Call> callSite,
			ArgumentPassingStrategy passingStrategy) {

		for (CallsiteArgument argument : ArgumentFactory.INSTANCE
				.fromCallSite(callSite)) {
			arguments.add(argument.mapToActualArgument(passingStrategy));
		}

		if (passingStrategy.passesHiddenSelf()) {
			arguments.add(passingStrategy.selfArgument());
		}
	}

	@Override
	public Argument argumentExplicitlyPassedAtPosition(int position) {

		for (Argument argument : arguments) {
			if (argument.isPassedAtPosition(position)) {
				return argument;
			}
		}

		return null;
	}

	@Override
	public Argument argumentExplicitlyPassedToKeyword(String keyword) {

		for (Argument argument : arguments) {
			if (argument.isPassedByKeyword(keyword)) {
				return argument;
			}
		}

		return null;
	}

	@Override
	public Argument argumentThatCouldExpandIntoPosition(int position) {

		for (Argument argument : arguments) {
			if (argument.mayExpandIntoPosition(position)) {
				return argument;
			}
		}

		return null;
	}

	@Override
	public Argument argumentThatCouldExpandIntoKeyword(String keyword) {

		for (Argument argument : arguments) {
			if (argument.mayExpandIntoKeyword(keyword)) {
				return argument;
			}
		}

		return null;
	}

}