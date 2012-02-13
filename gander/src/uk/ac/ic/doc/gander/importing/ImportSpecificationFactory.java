package uk.ac.ic.doc.gander.importing;

import java.util.ArrayList;
import java.util.List;

import org.python.pydev.parser.jython.ast.Import;
import org.python.pydev.parser.jython.ast.ImportFrom;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.aliasType;

public final class ImportSpecificationFactory {

	public static StandardImportSpecification newImport(
			ImportPath moduleImportName) {
		return StandardImportSpecification.newInstance(moduleImportName);
	}

	public static StandardImportAsSpecification newImportAs(
			ImportPath moduleImportName, String alias) {
		return StandardImportAsSpecification.newInstance(moduleImportName,
				alias);
	}

	public static FromImportSpecification newFromImport(
			ImportPath moduleImportName, String itemName) {
		return FromImportSpecification.newInstance(moduleImportName, itemName);
	}

	public static FromImportAsSpecification newFromImportAs(
			ImportPath moduleImportName, String itemName, String alias) {
		return FromImportAsSpecification.newInstance(moduleImportName,
				itemName, alias);
	}

	public static FromImportEverythingSpecification newFromImportEverything(
			ImportPath moduleName) {
		return FromImportEverythingSpecification.newInstance(moduleName);
	}

	public static Iterable<ImportSpecification> fromAstNode(Import node) {
		List<ImportSpecification> specs = new ArrayList<ImportSpecification>();

		for (aliasType alias : node.names) {
			if (alias.asname != null) {
				specs.add(newImportAs(
						ImportPath.fromDottedName(((NameTok) alias.name).id),
						((NameTok) alias.asname).id));
			} else {
				specs.add(newImport(ImportPath
						.fromDottedName(((NameTok) alias.name).id)));
			}
		}

		return specs;
	}

	public static Iterable<ImportSpecification> fromAstNode(ImportFrom node) {

		List<ImportSpecification> specs = new ArrayList<ImportSpecification>();

		ImportPath modulePath = ImportPath
				.fromDottedName(((NameTok) node.module).id);

		if (node.names.length == 0) { // this indicates a starred import

			specs.add(newFromImportEverything(modulePath));

		} else {

			for (aliasType alias : node.names) {
				if (alias.asname != null) {
					specs.add(newFromImportAs(modulePath,
							((NameTok) alias.name).id,
							((NameTok) alias.asname).id));
				} else {
					specs.add(newFromImport(modulePath,
							((NameTok) alias.name).id));
				}
			}
		}

		return specs;
	}

	private ImportSpecificationFactory() {
		throw new AssertionError();
	}

}
