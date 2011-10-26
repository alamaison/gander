package uk.ac.ic.doc.gander.flowinference.modelgoals;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.NameTok;

import uk.ac.ic.doc.gander.model.AttributeSitesWalker;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Namespace;
import uk.ac.ic.doc.gander.model.AttributeSitesWalker.EventHandler;

final class AttributeSitesFinder {

	private final Set<ModelSite<Attribute>> sites = new HashSet<ModelSite<Attribute>>();

	AttributeSitesFinder(Model model, final String attributeName) {

		new AttributeSitesWalker(model, new EventHandler() {

			public void encounteredAttribute(Attribute attribute,
					Namespace namespace) {
				if (((NameTok) attribute.attr).id.equals(attributeName)) {
					sites.add(new ModelSite<Attribute>(attribute, namespace));
				}
			}
		});
	}

	Set<ModelSite<Attribute>> getSites() {
		return Collections.unmodifiableSet(sites);
	}

}
