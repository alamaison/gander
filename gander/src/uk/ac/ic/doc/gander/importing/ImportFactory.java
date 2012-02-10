package uk.ac.ic.doc.gander.importing;

import java.util.ArrayList;
import java.util.List;

public final class ImportFactory {

	public static <O, C, M> Import<O, C, M> newImport(
			StaticImportSpecification specification, M relativeTo, C container) {
		return DefaultImport.newImport(specification, relativeTo, container);
	}

	public static <O, C, M> Iterable<Import<O, C, M>> fromAstNode(
			org.python.pydev.parser.jython.ast.Import node, M relativeTo,
			C container) {

		Iterable<StaticImportSpecification> specs = ImportSpecificationFactory
				.fromAstNode(node);

		return createImportInstancesForSpecifications(relativeTo, container,
				specs);
	}

	public static <O, C, M> Iterable<Import<O, C, M>> fromAstNode(
			org.python.pydev.parser.jython.ast.ImportFrom node, M relativeTo,
			C container) {

		Iterable<StaticImportSpecification> specs = ImportSpecificationFactory
				.fromAstNode(node);

		return createImportInstancesForSpecifications(relativeTo, container,
				specs);
	}

	private static <C, O, M> Iterable<Import<O, C, M>> createImportInstancesForSpecifications(
			M relativeTo, C container, Iterable<StaticImportSpecification> specs) {

		List<Import<O, C, M>> importInstances = new ArrayList<Import<O, C, M>>();

		for (StaticImportSpecification specification : specs) {
			Import<O, C, M> importInstance = newImport(specification,
					relativeTo, container);
			importInstances.add(importInstance);
		}

		return importInstances;
	}

	private ImportFactory() {
		throw new AssertionError();
	}

}
