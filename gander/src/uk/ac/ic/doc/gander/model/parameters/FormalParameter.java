package uk.ac.ic.doc.gander.model.parameters;

import java.util.Set;

import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.flowinference.argument.ArgumentDestination;
import uk.ac.ic.doc.gander.flowinference.argument.KeywordArgument;
import uk.ac.ic.doc.gander.flowinference.argument.PositionalArgument;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

public interface FormalParameter {

	ModelSite<?> site();

	ArgumentDestination passage(PositionalArgument argument);

	ArgumentDestination passage(KeywordArgument argument);

	Result<Type> typeAtCall(ModelSite<Call> callSite, SubgoalManager goalManager);

	Set<Variable> boundVariables();

}