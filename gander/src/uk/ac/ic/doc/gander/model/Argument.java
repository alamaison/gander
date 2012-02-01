package uk.ac.ic.doc.gander.model;

import uk.ac.ic.doc.gander.model.codeobject.FormalParameter;

public interface Argument {

	FormalParameter passArgumentAtCall(
			CallArgumentMapper argumentMapper);

}
