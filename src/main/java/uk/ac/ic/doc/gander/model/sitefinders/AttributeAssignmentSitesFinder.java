package uk.ac.ic.doc.gander.model.sitefinders;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Assign;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.NameTok;

import uk.ac.ic.doc.gander.model.AssignmentSite;
import uk.ac.ic.doc.gander.model.AttributeAssignmentSitesWalker;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.AttributeAssignmentSitesWalker.EventHandler;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;

/**
 * Find all the locations in the loaded model where an attribute is set via the
 * equals {@code =} symbol.
 */
public final class AttributeAssignmentSitesFinder {

    private final Set<AssignmentSite<Attribute>> sites = new HashSet<AssignmentSite<Attribute>>();

    public AttributeAssignmentSitesFinder(Model model,
            final String attributeName) {

        new AttributeAssignmentSitesWalker(model, new EventHandler() {

            public void encounteredAttributeAssignment(Assign assignment,
                    Attribute attribute, CodeObject codeObject) {
                if (((NameTok) attribute.attr).id.equals(attributeName)) {
                    sites.add(new AssignmentSite<Attribute>(assignment,
                            attribute, codeObject));
                }
            }
        });
    }

    public Set<AssignmentSite<Attribute>> getSites() {
        return Collections.unmodifiableSet(sites);
    }

}
