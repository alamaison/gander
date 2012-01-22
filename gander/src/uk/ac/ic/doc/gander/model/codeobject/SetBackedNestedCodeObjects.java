package uk.ac.ic.doc.gander.model.codeobject;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.python.pydev.parser.jython.ast.stmtType;

final class SetBackedNestedCodeObjects extends AbstractSet<NestedCodeObject>
		implements NestedCodeObjects {

	private Set<NestedCodeObject> nestedCodeObjects;

	public SetBackedNestedCodeObjects(
			Collection<? extends NestedCodeObject> nestedObjects) {
		this.nestedCodeObjects = Collections
				.unmodifiableSet(new HashSet<NestedCodeObject>(nestedObjects));
	}

	@Override
	public Iterator<NestedCodeObject> iterator() {
		return nestedCodeObjects.iterator(); // unmodifiable on creation
	}

	@Override
	public int size() {
		return nestedCodeObjects.size();
	}

	@Override
	public NestedCodeObject findCodeObjectMatchingAstNode(stmtType ast) {

		for (NestedCodeObject object : this) {
			if (object.ast().equals(ast)) {
				return object;
			}
		}

		return null;
	}

	@Override
	public NestedCodeObjects namedCodeObjectsDeclaredAs(String declaredName) {
		Set<NestedCodeObject> matchingObjects = new HashSet<NestedCodeObject>();

		for (NestedCodeObject object : this) {

			if (object instanceof NamedCodeObject) {
				if (((NamedCodeObject) object).declaredName().equals(
						declaredName)) {
					matchingObjects.add(object);
				}
			}
		}

		return new SetBackedNestedCodeObjects(matchingObjects);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime
				* result
				+ ((nestedCodeObjects == null) ? 0 : nestedCodeObjects
						.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SetBackedNestedCodeObjects other = (SetBackedNestedCodeObjects) obj;
		if (nestedCodeObjects == null) {
			if (other.nestedCodeObjects != null)
				return false;
		} else if (!nestedCodeObjects.equals(other.nestedCodeObjects))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SetBackedNestedCodeObjects [nestedCodeObjects="
				+ nestedCodeObjects + "]";
	}

}
