package uk.ac.ic.doc.gander.flowinference.argument;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.callsite.ArgumentPassingStrategy;
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

	private final ModelSite<exprType> argument;
	private final int position;

	ExplicitPositionalCallsiteArgument(ModelSite<exprType> argument,
			int position) {
		assert argument != null;
		assert position >= 0;

		this.argument = argument;
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

		return new ExplicitPositionalArgument(argument,
				argumentMapper.realPosition(position));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((argument == null) ? 0 : argument.hashCode());
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
		if (argument == null) {
			if (other.argument != null)
				return false;
		} else if (!argument.equals(other.argument))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ExplicitPositionalCallsiteArgument [argument=" + argument
				+ ", position=" + position + "]";
	}

}
