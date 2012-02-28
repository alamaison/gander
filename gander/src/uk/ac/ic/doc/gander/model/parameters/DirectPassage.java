package uk.ac.ic.doc.gander.model.parameters;

import java.util.Collections;
import java.util.Set;

import uk.ac.ic.doc.gander.flowinference.argument.Argument;
import uk.ac.ic.doc.gander.flowinference.argument.ArgumentDestination;
import uk.ac.ic.doc.gander.flowinference.call.Passage;

final class DirectPassage implements Passage {

	private final Argument argument;
	private final FormalParameter parameter;

	DirectPassage(Argument argument, FormalParameter parameter) {
		this.argument = argument;
		this.parameter = parameter;
	}

	@Override
	public Set<ArgumentDestination> destinationsOf(Argument argument) {

		if (argument.equals(this.argument)) {

			return Collections.singleton(parameter.passage(this.argument));
		} else {
			return Collections.emptySet();
		}
	}

}
