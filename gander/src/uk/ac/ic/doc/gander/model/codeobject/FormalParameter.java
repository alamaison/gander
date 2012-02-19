package uk.ac.ic.doc.gander.model.codeobject;

import org.python.pydev.parser.jython.ast.Call;

import uk.ac.ic.doc.gander.flowinference.ArgumentPassage;
import uk.ac.ic.doc.gander.flowinference.KeywordArgument;
import uk.ac.ic.doc.gander.flowinference.PositionalArgument;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.ModelSite;

public interface FormalParameter {

	ModelSite<?> site();

	ArgumentPassage passage(PositionalArgument argument);

	ArgumentPassage passage(KeywordArgument argument);

	Result<Type> typeAtCall(ModelSite<Call> callSite, SubgoalManager goalManager);

}