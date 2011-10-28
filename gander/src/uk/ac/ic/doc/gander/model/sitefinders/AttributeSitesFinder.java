package uk.ac.ic.doc.gander.model.sitefinders;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.NameTok;

import uk.ac.ic.doc.gander.model.AttributeSitesWalker;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.Namespace;
import uk.ac.ic.doc.gander.model.AttributeSitesWalker.EventHandler;


public final class AttributeSitesFinder {

	private final Set<ModelSite<Attribute>> sites = new HashSet<ModelSite<Attribute>>();

	public AttributeSitesFinder(Model model, final String attributeName) {

		new AttributeSitesWalker(model, new EventHandler() {

			public void encounteredAttribute(Attribute attribute,
					Namespace namespace) {
				if (((NameTok) attribute.attr).id.equals(attributeName)) {
					sites.add(new ModelSite<Attribute>(attribute, namespace));
				}
			}
		});
	}

	public Set<ModelSite<Attribute>> getSites() {
		return Collections.unmodifiableSet(sites);
	}

}
