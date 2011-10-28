package uk.ac.ic.doc.gander.flowinference.modelgoals;

import java.util.Collections;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Attribute;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.sitefinders.AttributeSitesFinder;

/**
 * Find all sites where an attribute is referenced with a name that matches the
 * given name.
 */
public final class AttributeSitesGoal implements ModelGoal<Attribute> {

	private final Model model;
	private final String attributeName;

	public AttributeSitesGoal(Model model, String attributeName) {
		this.model = model;
		this.attributeName = attributeName;
	}

	public Set<ModelSite<Attribute>> initialSolution() {
		return Collections.emptySet();
	}

	public Set<ModelSite<Attribute>> recalculateSolution(
			SubgoalManager goalManager) {
		return new AttributeSitesFinder(model, attributeName).getSites();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((attributeName == null) ? 0 : attributeName.hashCode());
		result = prime * result + ((model == null) ? 0 : model.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AttributeSitesGoal other = (AttributeSitesGoal) obj;
		if (attributeName == null) {
			if (other.attributeName != null)
				return false;
		} else if (!attributeName.equals(other.attributeName))
			return false;
		if (model == null) {
			if (other.model != null)
				return false;
		} else if (!model.equals(other.model))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AttributeSitesGoal [attributeName=" + attributeName + "]";
	}

}
