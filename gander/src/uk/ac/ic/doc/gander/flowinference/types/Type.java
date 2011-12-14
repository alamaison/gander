package uk.ac.ic.doc.gander.flowinference.types;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.Result;

public interface Type {
	String getName();

	Result<Type> memberType(String memberName, SubgoalManager goalManager);
}
