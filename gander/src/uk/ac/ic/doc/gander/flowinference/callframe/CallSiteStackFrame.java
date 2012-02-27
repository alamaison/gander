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
				new FunctionStylePassingStrategy());
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

}
