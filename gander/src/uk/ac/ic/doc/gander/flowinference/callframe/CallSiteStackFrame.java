package uk.ac.ic.doc.gander.flowinference.callframe;

import java.util.List;
import java.util.Map;

import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.flowinference.argument.Argument;
import uk.ac.ic.doc.gander.flowinference.argument.Arguments;
import uk.ac.ic.doc.gander.flowinference.argument.CallsiteArguments;
import uk.ac.ic.doc.gander.flowinference.types.FunctionStylePassingStrategy;
import uk.ac.ic.doc.gander.model.ModelSite;

public final class CallSiteStackFrame implements StackFrame<Argument> {

	private final Arguments arguments;

	public CallSiteStackFrame(ModelSite<Call> senderCallSite) {
		CallsiteArguments callSiteArguments = new CallsiteArguments(
				senderCallSite);
		arguments = new Arguments(callSiteArguments,
				FunctionStylePassingStrategy.INSTANCE);
	}

	@Override
	public List<Argument> knownPositions() {
		return arguments.positionals();
	}

	@Override
	public Map<String, Argument> knownKeywords() {

		return arguments.keywords();
	}

	@Override
	public boolean includesUnknownPositions() {
		return arguments.expandedIterable() != null;
	}

	@Override
	public boolean includesUnknownKeywords() {
		return arguments.expandedMapping() != null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((arguments == null) ? 0 : arguments.hashCode());
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
		CallSiteStackFrame other = (CallSiteStackFrame) obj;
		if (arguments == null) {
			if (other.arguments != null)
				return false;
		} else if (!arguments.equals(other.arguments))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CallSiteStackFrame [arguments=" + arguments + "]";
	}

}
