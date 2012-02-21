package uk.ac.ic.doc.gander.flowinference.argument;

import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.model.ModelSite;

/**
 * Models an argument at a callsite that is passed by position as an individual
 * argument.
 * 
 * The simplest example would look like {@code f(this_argument)}.
 * 
 * In other words, not a keyword argument and not part of an expanded iterable
 * such as {@code f(*iterable)}.
 */
final class ExplicitPositionalCallsiteArgument implements
		PositionalCallsiteArgument {

	private final ModelSite<Call> callSite;
	private final int position;

	ExplicitPositionalCallsiteArgument(ModelSite<Call> callSite, int position) {
		this.callSite = callSite;
		this.position = position;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * The returned actual explicit positional argument may have a different
	 * position to reflect the real position they occupy in the call frame.
	 */
	@Override
	public Argument mapToActualArgument(ArgumentPassingStrategy argumentMapper) {

		return new ExplicitPositionalArgument(callSite,
				argumentMapper.realPosition(position));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + position;
		result = prime * result
				+ ((callSite == null) ? 0 : callSite.hashCode());
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
		ExplicitPositionalCallsiteArgument other = (ExplicitPositionalCallsiteArgument) obj;
		if (position != other.position)
			return false;
		if (callSite == null) {
			if (other.callSite != null)
				return false;
		} else if (!callSite.equals(other.callSite))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ExplicitPositionalCallsiteArgument [callSite=" + callSite
				+ ", position=" + position + "]";
	}

}
