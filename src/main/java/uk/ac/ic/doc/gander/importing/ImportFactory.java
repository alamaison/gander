package uk.ac.ic.doc.gander.importing;

import java.util.ArrayList;
import java.util.List;

public final class ImportFactory {

    public static <C, M> Import<C, M> newImport(ImportStatement specification,
            M relativeTo, C container) {
        return DefaultImport.newImport(specification, relativeTo, container);
    }

    public static <C, M> Iterable<Import<C, M>> fromAstNode(
            org.python.pydev.parser.jython.ast.Import node, M relativeTo,
            C container) {

        Iterable<ImportStatement> specs = ImportStatementFactory
                .fromAstNode(node);

        return createImportInstancesForSpecifications(relativeTo, container,
                specs);
    }

    public static <C, M> Iterable<Import<C, M>> fromAstNode(
            org.python.pydev.parser.jython.ast.ImportFrom node, M relativeTo,
            C container) {

        Iterable<ImportStatement> specs = ImportStatementFactory
                .fromAstNode(node);

        return createImportInstancesForSpecifications(relativeTo, container,
                specs);
    }

    private static <C, M> Iterable<Import<C, M>> createImportInstancesForSpecifications(
            M relativeTo, C container, Iterable<ImportStatement> specs) {

        List<Import<C, M>> importInstances = new ArrayList<Import<C, M>>();

        for (ImportStatement specification : specs) {
            Import<C, M> importInstance = newImport(specification, relativeTo,
                    container);
            importInstances.add(importInstance);
        }

        return importInstances;
    }

    private ImportFactory() {
        throw new AssertionError();
    }

}
