package uk.ac.ic.doc.gander.importing;

import java.util.ArrayList;
import java.util.List;

import org.python.pydev.parser.jython.ast.Import;
import org.python.pydev.parser.jython.ast.ImportFrom;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.aliasType;

import uk.ac.ic.doc.gander.DottedName;

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

    private static ImportPath modulePathFromAstNode(ImportFrom node) {

        String dottedName = ((NameTok) node.module).id;

        List<String> tokens = new ArrayList<String>();
        if (node.level > 0) {

            /*
             * Relative paths are represented by empty tokens in the path.
             * 
             * The path gets one empty segment for each level it should move up
             * plus one to indicate the current level.
             * 
             * FIXME: .module is subtly different from module: the former is
             * _only_ relative whereas the latter is done relatively first and
             * then absolutely if that fails. This information should be
             * packaged up in ImportPath.
             */
            for (int i = 0; i < node.level; ++i) {
                tokens.add("");
            }
        } else if (dottedName.isEmpty()) {
            throw new AssertionError("Cannot have an empty module "
                    + "name and also not be relative");
        }

        tokens.addAll(DottedName.toImportTokens(dottedName));

        return ImportPath.fromTokens(tokens);
    }

    public static Iterable<ImportStatement> fromAstNode(ImportFrom node) {

        List<ImportStatement> specs = new ArrayList<ImportStatement>();

        ImportPath modulePath = modulePathFromAstNode(node);

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
