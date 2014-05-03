package uk.ac.ic.doc.gander.flowinference.typegoals.namespacename;

import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.model.AttributeAccessFinder;
import uk.ac.ic.doc.gander.model.ModelSite;

/**
 * Given a set of expressions, find any attribute accesses on those expressions
 * that match the given attribute name.
 */
final class NamedAttributeAccessFinder {

    private final String attributeName;
    private final Set<ModelSite<Attribute>> matchingAttributes = new HashSet<ModelSite<Attribute>>();

    NamedAttributeAccessFinder(
            Set<? extends ModelSite<? extends exprType>> namespacePositions,
            String attributeName) {
        assert namespacePositions != null;
        
        this.attributeName = attributeName;
        new AttributeAccessFinder(namespacePositions,
                new AttributeNameMatcher());
    }

    Set<ModelSite<Attribute>> accesses() {
        return matchingAttributes;
    }

    private class AttributeNameMatcher implements AttributeAccessFinder.Event {

        public boolean attributeAccess(ModelSite<Attribute> access) {
            if (((NameTok) access.astNode().attr).id.equals(attributeName)) {
                matchingAttributes.add(access);
            }
            return false;
        }

    }
}
