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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((argument == null) ? 0 : argument.hashCode());
		result = prime * result
				+ ((parameter == null) ? 0 : parameter.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DirectPassage other = (DirectPassage) obj;
		if (argument == null) {
			if (other.argument != null)
				return false;
		} else if (!argument.equals(other.argument))
			return false;
		if (parameter == null) {
			if (other.parameter != null)
				return false;
		} else if (!parameter.equals(other.parameter))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DirectPassage [argument=" + argument + ", parameter="
				+ parameter + "]";
	}

}
