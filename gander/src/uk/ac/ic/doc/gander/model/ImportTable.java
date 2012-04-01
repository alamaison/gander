package uk.ac.ic.doc.gander.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import uk.ac.ic.doc.gander.flowinference.Namespace;
import uk.ac.ic.doc.gander.flowinference.types.TModule;
import uk.ac.ic.doc.gander.flowinference.types.TUnresolvedImport;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.importing.Import;
import uk.ac.ic.doc.gander.importing.ImportSimulator.Binder;
import uk.ac.ic.doc.gander.importing.WholeModelImportSimulation;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;
import uk.ac.ic.doc.gander.model.codeobject.ModuleCO;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

public final class ImportTable {

	private final Map<NamespaceName, Set<Type>> bindings = new HashMap<NamespaceName, Set<Type>>();

	public ImportTable(Model model) {

		new WholeModelImportSimulation(model,
				new Binder<NamespaceName, Namespace, CodeObject, ModuleCO>() {

					@Override
					public void bindModuleToLocalName(ModuleCO loadedModule,
							String name, CodeObject container) {
						// Handled by UnqualifiedNameDefinitionsPartialSolution
					}

					@Override
					public void bindObjectToLocalName(
							NamespaceName importedObject, String name,
							CodeObject container) {
						// Handled by UnqualifiedNameDefinitionsPartialSolution
					}

					@Override
					public void bindModuleToName(ModuleCO loadedModule,
							String importName, ModuleCO receivingModule) {

						NamespaceName bindingName = new NamespaceName(
								new Variable(importName, receivingModule)
										.bindingLocation());

						addBinding(bindingName, new TModule(loadedModule));
					}

					@Override
					public void bindObjectToName(NamespaceName importedObject,
							String name, ModuleCO receivingModule) {
						/*
						 * It's not possible for an object to be the
						 * intermediate segment of an import statement
						 */
					}

					@Override
					public void bindAllNamespaceMembers(Namespace allMembers,
							CodeObject container) {
						/*
						 * It's not possible for an object to be the
						 * intermediate segment of an import statement
						 */
					}

					@Override
					public void onUnresolvedImport(
							Import<CodeObject, ModuleCO> importInstance,
							String importName, ModuleCO receivingModule) {

						NamespaceName bindingName = new NamespaceName(
								new Variable(importName, receivingModule)
										.bindingLocation());

						addBinding(bindingName, new TUnresolvedImport(
								importInstance));
					}

					@Override
					public void onUnresolvedLocalImport(
							Import<CodeObject, ModuleCO> importInstance,
							String name) {

						// Handled by UnqualifiedNameDefinitionsPartialSolution
					}
				});

	}

	public Set<Type> explicitBindings(NamespaceName bindingName) {

		Set<Type> objects = bindings.get(bindingName);
		if (objects != null) {
			return Collections.unmodifiableSet(objects);
		} else {
			return Collections.emptySet();
		}
	}

	private void addBinding(NamespaceName bindingName, Type object) {
		Set<Type> objects = bindings.get(bindingName);
		if (objects == null) {
			objects = new HashSet<Type>();
			bindings.put(bindingName, objects);
		}

		objects.add(object);
	}
}
