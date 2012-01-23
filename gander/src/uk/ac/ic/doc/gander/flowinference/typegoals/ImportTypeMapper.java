package uk.ac.ic.doc.gander.flowinference.typegoals;

import java.util.Collections;
import java.util.List;

import uk.ac.ic.doc.gander.DottedName;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.types.TModule;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.importing.ImportPath;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.codeobject.ModuleCO;

public final class ImportTypeMapper {

	private final SubgoalManager goalManager;

	ImportTypeMapper(SubgoalManager goalManager) {
		this.goalManager = goalManager;
	}

	public Result<Type> typeImport(Model model, String importPath) {

		ModuleCO module = model.lookup(ImportPath.fromDottedName(importPath));

		/*
		 * TODO: in theory this could resolve to more than one module but the
		 * model doesn't support that at the moment
		 */

		if (module == null) {
			return TopT.INSTANCE;
		} else {
			return new FiniteResult<Type>(Collections.singleton(new TModule(
					module)));
		}
	}

	public Result<Type> typeFromImport(Model model, String importPath) {

		List<String> tokens = DottedName.toImportTokens(importPath);

		ModuleCO module = model.lookup(ImportPath.fromTokens(tokens.subList(0,
				tokens.size() - 1)));

		if (module == null) {
			return TopT.INSTANCE;
		} else {
			return fromStyleType(module, tokens.get(tokens.size() - 1));
		}
	}

	/**
	 * Type a from-style imported item.
	 * 
	 * This works a bit unusually; the mechanism for determining the type can't
	 * just delegate the typing to the module's namespace. If the item being
	 * imported is a submodule then it isn't an attribute of the module's
	 * namespace. In other words the dotted import name isn't an attribute
	 * access at all. Only one the submodule lookup fails, can the dotted name
	 * be considered an attribute lookup and delegated to the namespace typer.
	 * 
	 * @param module
	 *            the module with respect to which the sub item is being
	 *            imported
	 * @param itemName
	 *            the name of the item being imported
	 * @return the type of the item
	 */
	private Result<Type> fromStyleType(ModuleCO module, String itemName) {
		Module submodule = module.oldStyleConflatedNamespace().getModules()
				.get(itemName);
		if (submodule != null) {
			return new FiniteResult<Type>(Collections.singleton(new TModule(
					submodule.codeObject())));
		} else {
			return goalManager.registerSubgoal(new NamespaceNameTypeGoal(
					new NamespaceName(itemName, module
							.oldStyleConflatedNamespace())));
		}
	}
}