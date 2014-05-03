package uk.ac.ic.doc.gander.duckinference;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.NamespaceWalker;
import uk.ac.ic.doc.gander.model.codeobject.ClassCO;

public class LoadedTypeDefinitions {

    private final Set<ClassCO> definitions = new HashSet<ClassCO>();

    public LoadedTypeDefinitions(Model model) {
        ClassDefinitionCollector collector = new ClassDefinitionCollector();
        collector.walk(model.getTopLevel());
        for (Class klass : collector.getClasses()) {
            definitions.add(klass.codeObject());
        }
    }

    public Set<ClassCO> getDefinitions() {
        return Collections.unmodifiableSet(definitions);
    }

    private final class ClassDefinitionCollector extends NamespaceWalker {

        private final Set<Class> classes = new HashSet<Class>();

        Set<Class> getClasses() {
            return classes;
        }

        @Override
        protected void visitClass(Class value) {
            classes.add(value);
        }
    }

}
