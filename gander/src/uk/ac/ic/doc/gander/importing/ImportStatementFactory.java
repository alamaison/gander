package uk.ac.ic.doc.gander.importing;

import java.util.ArrayList;
import java.util.List;

import org.python.pydev.parser.jython.ast.Import;
import org.python.pydev.parser.jython.ast.ImportFrom;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.aliasType;

public final class ImportStatementFactory {

	public static StandardImportStatement newImport(ImportPath moduleImportName) {
		return StandardImportStatement.newInstance(moduleImportName);
	}

	public static StandardImportAsStatement newImportAs(
			ImportPath moduleImportName, String alias) {
		return StandardImportAsStatement.newInstance(moduleImportName, alias);
	}

	public static FromImportStatement newFromImport(
			ImportPath moduleImportName, String itemName) {
		return FromImportStatement.newInstance(moduleImportName, itemName);
	}

	public static FromImportAsStatement newFromImportAs(
			ImportPath moduleImportName, String itemName, String alias) {
		return FromImportAsStatement.newInstance(moduleImportName, itemName,
				alias);
	}

	public static FromImportEverythingStatement newFromImportEverything(
			ImportPath moduleName) {
		return FromImportEverythingStatement.newInstance(moduleName);
	}

	public static Iterable<ImportStatement> fromAstNode(Import node) {
		List<ImportStatement> specs = new ArrayList<ImportStatement>();

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

	public static Iterable<ImportStatement> fromAstNode(ImportFrom node) {

		List<ImportStatement> specs = new ArrayList<ImportStatement>();

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

	private ImportStatementFactory() {
		throw new AssertionError();
	}

}
