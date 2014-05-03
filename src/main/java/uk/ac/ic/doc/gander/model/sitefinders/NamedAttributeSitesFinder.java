package uk.ac.ic.doc.gander.model.sitefinders;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.NameTok;

import uk.ac.ic.doc.gander.model.AttributeSearch;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.AttributeSearch.EventHandler;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;

public final class NamedAttributeSitesFinder {

    private final Set<ModelSite<Attribute>> sites = new HashSet<ModelSite<Attribute>>();

    public NamedAttributeSitesFinder(final CodeObject codeObject,
            final String attributeName) {

        new AttributeSearch(codeObject, new EventHandler() {

            public void encounteredAttribute(Attribute attribute,
                    CodeObject codeObject) {
                if (((NameTok) attribute.attr).id.equals(attributeName)) {
                    sites.add(new ModelSite<Attribute>(attribute, codeObject));
                }
            }
        });
    }

    public Set<ModelSite<Attribute>> getSites() {
        return Collections.unmodifiableSet(sites);
    }

}
