package uk.ac.ic.doc.gander.flowinference.modelgoals;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.NameTok;

import uk.ac.ic.doc.gander.model.AttributeAssignmentSitesWalker;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Namespace;
import uk.ac.ic.doc.gander.model.AttributeAssignmentSitesWalker.EventHandler;

/**
 * Find all the locations in the loaded model where an attribute is set via the
 * equals {@code =} symbol.
 */
final class AttributeAssignmentSitesFinder {

	private final Set<AssignmentSite<Attribute>> sites = new HashSet<AssignmentSite<Attribute>>();

	AttributeAssignmentSitesFinder(Model model, final String attributeName) {

		new AttributeAssignmentSitesWalker(model, new EventHandler() {

			public void encounteredAttributeAssignment(Assign assignment,
					Attribute attribute, Namespace namespace) {
				if (((NameTok) attribute.attr).id.equals(attributeName)) {
					sites.add(new AssignmentSite<Attribute>(assignment,
							attribute, namespace));
				}
			}
		});
	}

	Set<AssignmentSite<Attribute>> getSites() {
		return Collections.unmodifiableSet(sites);
	}

}
