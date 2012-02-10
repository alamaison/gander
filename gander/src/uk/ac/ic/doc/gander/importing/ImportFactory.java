package uk.ac.ic.doc.gander.importing;

import java.util.ArrayList;
import java.util.List;

import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.aliasType;

public final class ImportFactory {

	public static <O, C, M> Import<O, C, M> newImport(
			StandardImportSpecification specification, M relativeTo, C container) {
		return new StandardImport<O, C, M>(specification, relativeTo, container);
	}

	public static <O, C, M> Import<O, C, M> newImportAs(
			StandardImportAsSpecification specification, M relativeTo,
			C container) {
		return new StandardImportAs<O, C, M>(specification, relativeTo,
				container);
	}

	public static <O, C, M> Import<O, C, M> newFromImport(
			FromImportSpecification specification, M relativeTo, C container) {
		return new FromImport<O, C, M>(specification, relativeTo, container);
	}

	public static <O, C, M> Import<O, C, M> newFromImportAs(
			FromImportAsSpecification specification, M relativeTo, C container) {
		return new FromImportAs<O, C, M>(specification, relativeTo, container);
	}

	public static <O, C, M> Iterable<Import<O, C, M>> fromAstNode(
			org.python.pydev.parser.jython.ast.Import node, M relativeTo,
			C container) {

		List<Import<O, C, M>> importInstances = new ArrayList<Import<O, C, M>>();

		for (aliasType alias : node.names) {
			Import<O, C, M> importInstance;
			if (alias.asname != null) {
				importInstance = newImportAs(
						StandardImportAsSpecification.newInstance(
								((NameTok) alias.name).id,
								((NameTok) alias.asname).id), relativeTo,
						container);
			} else {
				importInstance = newImport(
						StandardImportSpecification
								.newInstance(((NameTok) alias.name).id),
						relativeTo, container);
			}
			importInstances.add(importInstance);
		}

		return importInstances;
	}

	public static <O, C, M> Iterable<Import<O, C, M>> fromAstNode(
			org.python.pydev.parser.jython.ast.ImportFrom node, M relativeTo,
			C container) {

		List<Import<O, C, M>> importInstances = new ArrayList<Import<O, C, M>>();

		String moduleName = ((NameTok) node.module).id;

		for (aliasType alias : node.names) {
			Import<O, C, M> importInstance;
			if (alias.asname != null) {
				importInstance = newFromImportAs(
						FromImportAsSpecification.newInstance(moduleName,
								((NameTok) alias.name).id,
								((NameTok) alias.asname).id), relativeTo,
						container);
			} else {
				importInstance = newFromImport(
						FromImportSpecification.newInstance(moduleName,
								((NameTok) alias.name).id), relativeTo,
						container);
			}
			importInstances.add(importInstance);
		}

		return importInstances;
	}

	private ImportFactory() {
		throw new AssertionError();
	}

}
