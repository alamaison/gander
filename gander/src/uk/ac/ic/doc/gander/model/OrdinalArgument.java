package uk.ac.ic.doc.gander.model;

import org.python.pydev.parser.jython.ast.Call;


public final class OrdinalArgument implements Argument {

	private final ModelSite<Call> callSite;
	private final int argumentIndex;

	public OrdinalArgument(ModelSite<Call> callSite, int argumentIndex) {
		this.callSite = callSite;
		this.argumentIndex = argumentIndex;
	}

	public int ordinal() {
		return argumentIndex;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + argumentIndex;
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
		OrdinalArgument other = (OrdinalArgument) obj;
		if (argumentIndex != other.argumentIndex)
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
		return "OrdinalArgument [callSite=" + callSite + ", argumentIndex="
				+ argumentIndex + "]";
	}

}
