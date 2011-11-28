package uk.ac.ic.doc.gander.flowinference.flowgoals.flowsituations;

import java.util.Collections;
import java.util.Set;

import org.python.pydev.parser.jython.ast.Name;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.flowgoals.FlowPosition;
import uk.ac.ic.doc.gander.flowinference.flowgoals.NamespaceKeyPosition;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

final class NameSituation implements FlowSituation {

	private final Name name;
	private final ModelSite<?> expressionSite;

	/**
	 * This situation keeps a reference to the node as its text value indicates
	 * the destination of the flow.
	 */
	NameSituation(Name name, ModelSite<?> expressionSite) {
		this.name = name;
		this.expressionSite = expressionSite;
	}

	/**
	 * In a single step of execution Name instances can flow into any use of the
	 * name bound in the same binding namespace.
	 */
	public Set<FlowPosition> nextFlowPositions(SubgoalManager goalManager) {
		/*
		 * The name doesn't necessarily bind in the enclosing code object's
		 * namespace. We have to resolve the lexical binding first.
		 */
		Variable bindingNamespace = new Variable(name.id, expressionSite
				.codeObject());

		return Collections.<FlowPosition> singleton(new NamespaceKeyPosition(
				bindingNamespace));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((expressionSite == null) ? 0 : expressionSite.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		NameSituation other = (NameSituation) obj;
		if (expressionSite == null) {
			if (other.expressionSite != null)
				return false;
		} else if (!expressionSite.equals(other.expressionSite))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "NameSituation [expressionSite=" + expressionSite + ", name="
				+ name + "]";
	}

}
