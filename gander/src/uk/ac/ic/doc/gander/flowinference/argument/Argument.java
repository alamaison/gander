package uk.ac.ic.doc.gander.flowinference.argument;

import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;

public interface Argument {

	ArgumentPassage passArgumentAtCall(InvokableCodeObject receiver,
			ArgumentPassingStrategy argumentMapper);

}
