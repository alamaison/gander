package uk.ac.ic.doc.gander.importing;

import java.util.ArrayList;
import java.util.List;

import org.python.pydev.parser.jython.ast.Import;
import org.python.pydev.parser.jython.ast.ImportFrom;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.aliasType;

public final class ImportSpecificationFactory {

	public static StandardImportSpecification newImport(String moduleImportName) {
		return StandardImportSpecification.newInstance(moduleImportName);
	}

	public static StaticImportSpecification newImportAs(String moduleImportName,
			String alias) {
		return StandardImportAsSpecification.newInstance(moduleImportName,
				alias);
	}

	public static StaticImportSpecification newFromImport(String moduleImportName,
			String itemName) {
		return FromImportSpecification.newInstance(moduleImportName, itemName);
	}

	public static StaticImportSpecification newFromImportAs(String moduleImportName,
			String itemName, String alias) {
		return FromImportAsSpecification.newInstance(moduleImportName,
				itemName, alias);
	}

	public static Iterable<StaticImportSpecification> fromAstNode(Import node) {
		List<StaticImportSpecification> specs = new ArrayList<StaticImportSpecification>();

		for (aliasType alias : node.names) {
			if (alias.asname != null) {
				specs.add(newImportAs(((NameTok) alias.name).id,
						((NameTok) alias.asname).id));
			} else {
				specs.add(newImport(((NameTok) alias.name).id));
			}
		}

		return specs;
	}

	public static Iterable<StaticImportSpecification> fromAstNode(ImportFrom node) {
		List<StaticImportSpecification> specs = new ArrayList<StaticImportSpecification>();

		for (aliasType alias : node.names) {
			if (alias.asname != null) {
				specs
						.add(newFromImportAs(((NameTok) node.module).id,
								((NameTok) alias.name).id,
								((NameTok) alias.asname).id));
			} else {
				specs.add(newFromImport(((NameTok) node.module).id,
						((NameTok) alias.name).id));
			}
		}

		return specs;
	}

	private ImportSpecificationFactory() {
		throw new AssertionError();
	}

}
