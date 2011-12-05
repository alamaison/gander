package uk.ac.ic.doc.gander.flowinference;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uk.ac.ic.doc.gander.DottedName;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.typegoals.TopT;
import uk.ac.ic.doc.gander.flowinference.types.TClass;
import uk.ac.ic.doc.gander.flowinference.types.TFunction;
import uk.ac.ic.doc.gander.flowinference.types.TModule;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Module;

public final class ImportTypeMapper {

	public static Result<Type> typeImport(Model model, String importPath) {

		Module module = model.lookup(importPath);
		/*
		 * TODO: in theory this could resolve to more than one module but the
		 * model doesn't support that at the moment
		 */

		if (module == null) {
			return TopT.INSTANCE;
		} else {
			return new FiniteResult<Type>(Collections.singleton(new TModule(
					module.codeObject())));
		}
	}

	public static Result<Type> typeFromImport(Model model, String importPath) {
		List<String> tokens = DottedName.toImportTokens(importPath);
		Module module = model.lookup(tokens.subList(0, tokens.size() - 1));
		if (module == null) {
			return TopT.INSTANCE;
		} else {
			return aggregateType(module, tokens.get(tokens.size() - 1));
		}
	}

	private static Result<Type> aggregateType(Module module, String itemName) {
		Set<Type> types = new HashSet<Type>();

		Class klass = module.getClasses().get(itemName);
		if (klass != null) {
			types.add(new TClass(klass));
		}

		Function function = module.getFunctions().get(itemName);
		if (function != null) {
			types.add(new TFunction(function));
		}

		// TODO: item can be arbitrary object

		return new FiniteResult<Type>(types);
	}

	private ImportTypeMapper() {
		throw new AssertionError();
	}
}