package uk.ac.ic.doc.gander.importing;

import java.util.ArrayList;
import java.util.List;

import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.aliasType;

public final class ImportFactory {

	public static <O, C, M> Import<O, C, M> newImport(String moduleImportName,
			M relativeTo, C container) {
		return new StandardImport<O, C, M>(StandardImportSpecification
				.newInstance(moduleImportName), relativeTo, container);
	}

	public static <O, C, M> Import<O, C, M> newImportAs(
			String moduleImportName, String alias, M relativeTo, C container) {
		return new StandardImportAs<O, C, M>(StandardImportAsSpecification
				.newInstance(moduleImportName, alias), relativeTo, container);
	}

	public static <O, C, M> Import<O, C, M> newFromImport(
			String moduleImportName, String itemName, M relativeTo, C container) {
		return new FromImport<O, C, M>(FromImportSpecification.newInstance(
				moduleImportName, itemName), relativeTo, container);
	}

	public static <O, C, M> Import<O, C, M> newFromImportAs(
			String moduleImportName, String itemName, String alias,
			M relativeTo, C container) {
		return new FromImportAs<O, C, M>(FromImportAsSpecification.newInstance(
				moduleImportName, itemName, alias), relativeTo, container);
	}

	public static <O, C, M> Iterable<Import<O, C, M>> fromAstNode(
			org.python.pydev.parser.jython.ast.Import node, M relativeTo,
			C container) {

		List<Import<O, C, M>> specs = new ArrayList<Import<O, C, M>>();

		for (aliasType alias : node.names) {
			Import<O, C, M> importInstance;
			if (alias.asname != null) {
				importInstance = newImportAs(((NameTok) alias.name).id,
						((NameTok) alias.asname).id, relativeTo, container);
			} else {
				importInstance = newImport(((NameTok) alias.name).id,
						relativeTo, container);
			}
			specs.add(importInstance);
		}

		return specs;
	}

	public static <O, C, M> Iterable<Import<O, C, M>> fromAstNode(
			org.python.pydev.parser.jython.ast.ImportFrom node, M relativeTo,
			C container) {

		List<Import<O, C, M>> specs = new ArrayList<Import<O, C, M>>();

		for (aliasType alias : node.names) {
			Import<O, C, M> importInstance;
			if (alias.asname != null) {
				importInstance = newFromImportAs(((NameTok) node.module).id,
						((NameTok) alias.name).id, ((NameTok) alias.asname).id,
						relativeTo, container);
			} else {
				importInstance = newFromImport(((NameTok) node.module).id,
						((NameTok) alias.name).id, relativeTo, container);
			}
			specs.add(importInstance);
		}

		return specs;
	}

	private ImportFactory() {
		throw new AssertionError();
	}

}
