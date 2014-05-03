package uk.ac.ic.doc.gander.flowinference.call;

import java.util.Set;

import uk.ac.ic.doc.gander.flowinference.argument.Argument;
import uk.ac.ic.doc.gander.flowinference.argument.ArgumentDestination;

public interface Passage {

    Set<ArgumentDestination> destinationsOf(Argument argument);

}