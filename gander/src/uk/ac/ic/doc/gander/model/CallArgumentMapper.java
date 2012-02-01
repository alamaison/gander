package uk.ac.ic.doc.gander.model;

import uk.ac.ic.doc.gander.model.codeobject.FormalParameter;

public interface CallArgumentMapper {

	FormalParameter parameterAtIndex(int argumentIndex);

	FormalParameter namedParameter(String parameterName);

}
