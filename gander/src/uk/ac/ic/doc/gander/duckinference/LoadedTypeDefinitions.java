package uk.ac.ic.doc.gander.duckinference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.ModelWalker;

public class LoadedTypeDefinitions {

	private Collection<Class> definitions;

	public LoadedTypeDefinitions(Model model) {
		this.definitions = new ClassDefinitionCollector(model).getClasses();
	}

	public Collection<Class> getDefinitions() {
		return definitions;
	}

	private final class ClassDefinitionCollector extends ModelWalker {

		private final List<Class> classes = new ArrayList<Class>();
		
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
