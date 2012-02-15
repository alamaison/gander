package uk.ac.ic.doc.gander.flowinference.typegoals;

import java.util.Collections;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.RedundancyEliminator;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.types.TModule;
import uk.ac.ic.doc.gander.flowinference.types.TUnresolvedImport;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.importing.Import;
import uk.ac.ic.doc.gander.importing.ImportSimulator;
import uk.ac.ic.doc.gander.importing.ImportSimulator.Binder;
import uk.ac.ic.doc.gander.model.Namespace;
import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.NamespaceNameLoader;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;
import uk.ac.ic.doc.gander.model.codeobject.ModuleCO;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

final class ImportTypeMapper {

	private final SubgoalManager goalManager;

	ImportTypeMapper(SubgoalManager goalManager) {
		this.goalManager = goalManager;
	}

	Result<Type> typeImport(Variable variable,
			Import<CodeObject, ModuleCO> importInstance) {
		LocalTypeBinder typingBinder = new LocalTypeBinder(variable);

		ImportSimulator<NamespaceName, Namespace, CodeObject, ModuleCO> simulator = ImportSimulator
				.newInstance(typingBinder,
						new NamespaceNameLoader(variable.model()));
		simulator.simulateImport(importInstance);

		return typingBinder.partialVariableType.result();
	}

	private final class LocalTypeBinder implements
			Binder<NamespaceName, Namespace, CodeObject, ModuleCO> {

		private final Variable variable;
		private final RedundancyEliminator<Type> partialVariableType = new RedundancyEliminator<Type>();

		public LocalTypeBinder(Variable variable) {
			this.variable = variable;
		}

		@Override
		public void bindModuleToLocalName(ModuleCO loadedModule, String name,
				CodeObject container) {
			assert container.equals(variable.codeObject());

			if (name.equals(variable.name())) {

				partialVariableType.add(new FiniteResult<Type>(Collections
						.singleton(new TModule(loadedModule))));
			}
		}

		@Override
		public void bindObjectToLocalName(NamespaceName importedObject,
				String name, CodeObject container) {
			assert container.equals(variable.codeObject());

			if (name.equals(variable.name())) {

				partialVariableType.add(goalManager
						.registerSubgoal(new NamespaceNameTypeGoal(
								importedObject)));
			}
		}

		@Override
		public void bindModuleToName(ModuleCO loadedModule, String name,
				ModuleCO receivingModule) {
			assert !receivingModule.equals(variable.codeObject());
		}

		@Override
		public void bindObjectToName(NamespaceName importedObject, String name,
				ModuleCO receivingModule) {
			assert !receivingModule.equals(variable.codeObject());
		}

		@Override
		public void bindAllNamespaceMembers(Namespace allMembers,
				CodeObject container) {
			assert container.equals(variable.codeObject());

			/*
			 * event though all names are imported, we are interested in a
			 * specific name so can issue a query for it
			 */
			partialVariableType.add(goalManager
					.registerSubgoal(new NamespaceNameTypeGoal(
							new NamespaceName(variable.name(), allMembers))));
		}

		@Override
		public void onUnresolvedImport(
				Import<CodeObject, ModuleCO> importInstance, String name,
				ModuleCO receivingModule) {
			assert !receivingModule.equals(variable.codeObject());
		}

		@Override
		public void onUnresolvedLocalImport(
				Import<CodeObject, ModuleCO> importInstance, String name) {

			if (name.equals(variable.name())) {
				partialVariableType.add(new FiniteResult<Type>(Collections
						.singleton(new TUnresolvedImport(importInstance))));
			}
		}
	}
}