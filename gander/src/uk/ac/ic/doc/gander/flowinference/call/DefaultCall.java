package uk.ac.ic.doc.gander.flowinference.call;

import uk.ac.ic.doc.gander.flowinference.argument.Argument;
import uk.ac.ic.doc.gander.flowinference.argument.ArgumentDestination;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.types.TCallable;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;
import uk.ac.ic.doc.gander.model.name_binding.Variable;
import uk.ac.ic.doc.gander.model.parameters.FormalParameter;

public final class DefaultCall implements Call {

	private final TCallable calledAs;
	private final InvokableCodeObject procedure;
	private final ModelSite<org.python.pydev.parser.jython.ast.Call> callSite;

	public DefaultCall(InvokableCodeObject procedure, TCallable calledAs,
			ModelSite<org.python.pydev.parser.jython.ast.Call> callSite) {

		this.procedure = procedure;
		this.calledAs = calledAs;
		this.callSite = callSite;
	}

	@Override
	public Result<ArgumentDestination> destinationsReceivingArgument(
			Argument argument, SubgoalManager goalManager) {
		return calledAs.destinationsReceivingArgument(argument, goalManager);
	}

	@Override
	public Result<Argument> argumentsBoundToVariable(Variable variable,
			SubgoalManager goalManager) {

		FormalParameter parameter = procedure.formalParameters()
				.variableBindingParameter(variable);

		return calledAs.argumentsPassedToParameter(parameter, callSite,
				goalManager);
	}

}
