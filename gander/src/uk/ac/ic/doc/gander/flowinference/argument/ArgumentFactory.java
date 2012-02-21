package uk.ac.ic.doc.gander.flowinference.argument;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.model.ModelSite;

public enum ArgumentFactory {

	INSTANCE;

	public CallsiteArgument fromCallSite(ModelSite<Call> callSite,
			exprType argument) {

		Call node = callSite.astNode();

		if (argument.equals(node.starargs) || argument.equals(node.kwargs)) {

			// TODO: implement these arguments

			return new CallsiteArgument() {

				@Override
				public Argument mapToActualArgument(
						ArgumentPassingStrategy argumentMapper) {

					return new EscapeArgument();
				}
			};

		} else {

			for (int i = 0; i < node.args.length; ++i) {
				if (argument.equals(node.args[i])) {

					return new ExplicitPositionalCallsiteArgument(callSite, i);
				}
			}

			for (int i = 0; i < node.keywords.length; ++i) {
				if (argument.equals(node.keywords[i].value)) {

					return new ExplicitKeywordCallsiteArgument(callSite, i);
				}
			}

			return null; // expression not an argument
		}
	}
}
