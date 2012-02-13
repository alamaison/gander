package uk.ac.ic.doc.gander.flowinference.typegoals;

import java.util.Collections;

import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.types.TModule;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.importing.ImportSpecification;
import uk.ac.ic.doc.gander.importing.StaticImportSpecification;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.codeobject.ModuleCO;
import uk.ac.ic.doc.gander.model.name_binding.Variable;

final class ImportTypeMapper {

	private final SubgoalManager goalManager;

	ImportTypeMapper(SubgoalManager goalManager) {
		this.goalManager = goalManager;
	}

	Result<Type> typeImport(Variable variable, ImportSpecification info) {
		if (info instanceof StaticImportSpecification) {

			if (variable.name().equals(
					((StaticImportSpecification) info).bindingName())) {

				ModuleCO module = variable.model().lookup(
						info.boundObjectParentPath());

				/*
				 * TODO: in theory this could resolve to more than one module
				 * but the model doesn't support that at the moment
				 */

				if (module == null) {
					return TopT.INSTANCE;
				} else {
					return typeImportedObject(module,
							(StaticImportSpecification) info);
				}

			} else {
				return FiniteResult.bottom();
			}
		} else {
			return TopT.INSTANCE;
		}
	}

	/**
	 * Type the object that is loaded and bound by the given import.
	 * 
	 * This works a bit differently depending on whether the import is a
	 * bog-standard {@code import} of a {@code from x import}. The former only
	 * looks for module objects while the latter can import both modules and
	 * other objects.
	 * 
	 * This works a bit strangely; the mechanism for determining the type can't
	 * just delegate the typing to the parent module's namespace. If the item
	 * being imported is a submodule then it isn't an attribute of the parent
	 * module's namespace. In other words the dotted import name isn't an
	 * attribute access at all. Only once the submodule lookup fails, can the
	 * dotted name be considered an attribute lookup and delegated to the
	 * namespace typer.
	 * 
	 * @param module
	 *            the module with respect to which the sub item is being
	 *            imported
	 * @param info
	 *            the import specification
	 * @return the type of the item
	 */
	private Result<Type> typeImportedObject(ModuleCO module,
			StaticImportSpecification info) {

		Module submodule = module.oldStyleConflatedNamespace().getModules()
				.get(info.boundObjectName());

		if (submodule != null) {

			return new FiniteResult<Type>(Collections.singleton(new TModule(
					submodule.codeObject())));

		} else if (!info.importsAreLimitedToModules()) {

			return goalManager.registerSubgoal(new NamespaceNameTypeGoal(
					new NamespaceName(info.boundObjectName(), module
							.oldStyleConflatedNamespace())));

		} else {
			return TopT.INSTANCE;
		}
	}
}