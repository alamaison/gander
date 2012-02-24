package uk.ac.ic.doc.gander.flowinference.callsite;

import uk.ac.ic.doc.gander.flowinference.argument.Argument;

public interface InternalCallsite {

	Argument argumentExplicitlyPassedAtPosition(int position);

	Argument argumentExplicitlyPassedToKeyword(String keyword);

	Argument argumentThatCouldExpandIntoPosition(int position);

	Argument argumentThatCouldExpandIntoKeyword(String keyword);

}