package uk.ac.ic.doc.gander.model.build;

import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Package;
import uk.ac.ic.doc.gander.model.Namespace;

public interface BuildableNamespace extends Namespace {

	public void addPackage(Package pkg);

	public void addModule(Module module);

	public void addFunction(Function function);

	public void addClass(Class klass);
}
