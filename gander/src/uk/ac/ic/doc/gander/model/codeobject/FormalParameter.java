package uk.ac.ic.doc.gander.model.codeobject;

import uk.ac.ic.doc.gander.flowinference.ArgumentPassage;
import uk.ac.ic.doc.gander.flowinference.KeywordArgument;
import uk.ac.ic.doc.gander.flowinference.PositionalArgument;
import uk.ac.ic.doc.gander.model.ModelSite;

public interface FormalParameter {

	ModelSite<?> site();

	ArgumentPassage passage(PositionalArgument argument);
	ArgumentPassage passage(KeywordArgument argument);

}