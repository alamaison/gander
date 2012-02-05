package uk.ac.ic.doc.gander.flowinference;

import uk.ac.ic.doc.gander.model.codeobject.InvokableCodeObject;

public interface Argument {

	ArgumentPassage passArgumentAtCall(InvokableCodeObject receiver,
			ArgumentPassingStrategy argumentMapper);

}
