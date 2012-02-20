package uk.ac.ic.doc.gander.flowinference;

import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;

public enum ArgumentFactory {

	INSTANCE;

	public Argument fromCallSite(ModelSite<Call> callSite, exprType argument) {

		Call node = callSite.astNode();

		if (argument.equals(node.starargs) || argument.equals(node.kwargs)) {
			// TODO: implement these arguments
			return new Argument() {

				@Override
				public ArgumentPassage passArgumentAtCall(
						InvokableCodeObject receiver,
						ArgumentPassingStrategy argumentMapper) {
					return new ArgumentPassage() {

						@Override
						public Result<FlowPosition> nextFlowPositions() {
							return FiniteResult.bottom();
						}
					};
				}
			};
		} else {

			for (int i = 0; i < node.args.length; ++i) {
				if (argument.equals(node.args[i])) {

					return new ExplicitPositionalArgument(callSite, i);
				}
			}

			for (int i = 0; i < node.keywords.length; ++i) {
				if (argument.equals(node.keywords[i].value)) {

					return new ExplicitKeywordArgument(callSite, i);
				}
			}

			return null; // expression not an argument
		}
	}
}
