package uk.ac.ic.doc.gander.duckinference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.ModelWalker;

public class TypeDefinitions {

	private Model model;

	public TypeDefinitions(Model model) {
		this.model = model;
	}

	public Collection<Class> getDefinitions() {
		return new ClassDefinitionCollector(model).getClasses();
	}

	private class ClassDefinitionCollector extends ModelWalker {

		final List<Class> classes = new ArrayList<Class>();
		
		ClassDefinitionCollector(Model model) {
			walk(model);
		}
		
		List<Class> getClasses() {
			return classes;
		}

		@Override
		protected void visitClass(Class value) {
			classes.add(value);
		}
	}

}
