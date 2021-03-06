package uk.ac.ic.doc.gander.model.build;

import java.io.IOException;

import org.python.pydev.parser.jython.ParseException;

import uk.ac.ic.doc.gander.hierarchy.SourceFile;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.ModuleNamespace;
import uk.ac.ic.doc.gander.model.MutableModel;
import uk.ac.ic.doc.gander.model.codeobject.ModuleCO;

/**
 * Create new SourceFile object in the model by parsing its file and following
 * any import statements.
 * 
 * Much of the point of this class is to translate between a hierarchy module
 * and a runtime-model module.
 */
public final class FileLoader {

    private ModuleNamespace module;

    /**
     * Load a runtime-model module given a hierarchy package.
     */
    public FileLoader(SourceFile sourceFile, Module parent, MutableModel model)
            throws ParseException, IOException {
        assert parent != null;

        // Parse module first so that parse errors abort loading immediately
        // and don't leave empty module in the model
        FileParser parser = new FileParser(sourceFile.getFile());

        // Must add the module to the model before we load it in case modules
        // imported from within it, try to import this module again. In other
        // words, we have to do this to avoid infinite recursion when there
        // are import cycles.
        ModuleCO codeObject = new ModuleCO(sourceFile.getName(), parser
                .getAst());
        module = new ModuleNamespace(codeObject, parent, model, sourceFile.isSystem());
        codeObject.setNamespace(module);
        parent.addModule(module);
        module.addNestedCodeObjects();

        // XXX: If loading module fails (due to problems with imported modules,
        // most likely), we're left with this empty module in the model. Do we
        // need to clean this up?

        new CodeObjectImportLoader(codeObject, model);
    }

    public Module getModule() {
        return module;
    }

}
