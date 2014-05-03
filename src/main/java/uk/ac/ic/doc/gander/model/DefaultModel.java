package uk.ac.ic.doc.gander.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.python.pydev.parser.jython.ParseException;

import uk.ac.ic.doc.gander.DottedName;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.importing.ImportPath;
import uk.ac.ic.doc.gander.model.build.CodeObjectImportLoader;
import uk.ac.ic.doc.gander.model.build.FileLoader;
import uk.ac.ic.doc.gander.model.build.PackageLoader;
import uk.ac.ic.doc.gander.model.build.TopLevelModuleLoader;
import uk.ac.ic.doc.gander.model.codeobject.ClassCO;
import uk.ac.ic.doc.gander.model.codeobject.ModuleCO;

public class DefaultModel implements MutableModel {

    protected Module topLevelPackage;
    protected Hierarchy hierarchy;

    public DefaultModel(Hierarchy hierarchy) throws ParseException, IOException {
        this.hierarchy = hierarchy;

        topLevelPackage = TopLevelModuleLoader.load(this);
        new CodeObjectImportLoader(topLevelPackage.codeObject(), this);
    }

    @Override
    public ModuleCO lookup(ImportPath path) {
        Module module = getTopLevel().lookup(path);
        if (module != null)
            return module.codeObject();
        else
            return null;
    }

    @Override
    public Module getTopLevel() {
        return topLevelPackage;
    }

    @Override
    @Deprecated
    public Module lookup(String importName) {
        return lookup(DottedName.toImportTokens(importName));
    }

    @Override
    @Deprecated
    public Module lookup(List<String> importNameTokens) {
        return getTopLevel().lookup(importNameTokens);
    }

    @Override
    public Module load(String importName) throws ParseException, IOException {
        List<String> tokens = DottedName.toImportTokens(importName);
        Module imported = loadPackage(tokens);
        if (imported == null)
            imported = loadModule(tokens);
        return imported;
    }

    @Override
    public Module loadModule(String fullyQualifiedName) throws ParseException,
            IOException {
        return loadModule(DottedName.toImportTokens(fullyQualifiedName));
    }

    @Override
    public Module loadPackage(String fullyQualifiedName) throws ParseException,
            IOException {
        return loadPackage(DottedName.toImportTokens(fullyQualifiedName));
    }

    /**
     * Load module if it hasn't been loaded before.
     * 
     * Will also load any parent packages if they haven't been loaded yet.
     */
    @Override
    public Module loadModule(List<String> fullyQualifiedPath)
            throws ParseException, IOException {
        // Loading must be idempotent so if the module is already loaded we
        // must return the same instance
        Module loaded = lookup(fullyQualifiedPath);
        if (loaded == null)
            loaded = reallyLoadModule(fullyQualifiedPath);

        return loaded;
    }

    /**
     * Load module as well as any parent packages that haven't been loaded
     * already.
     */
    private Module reallyLoadModule(List<String> fullyQualifiedPath)
            throws ParseException, IOException {
        List<String> path = new ArrayList<String>();

        // Loading a module means also loading any packages above it in the
        // hierarchy so we load the package whose path is made from all but
        // the final dotted segment. Loading this package takes care of
        // loading any of its parents.
        for (int i = 0; i < fullyQualifiedPath.size() - 1; ++i)
            path.add(fullyQualifiedPath.get(i));

        Module parent = loadPackage(path);
        if (parent == null)
            return null;

        // Add module name to the path we used earlier to load the package
        if (fullyQualifiedPath.size() > 0)
            path.add(fullyQualifiedPath.get(fullyQualifiedPath.size() - 1));

        return loadModuleIntoParent(path, parent);
    }

    /**
     * Load package if it hasn't been loaded before.
     * 
     * Will also load any parent packages if they haven't been loaded yet.
     */
    @Override
    public Module loadPackage(List<String> fullyQualifiedPath)
            throws ParseException, IOException {
        Queue<String> tokens = new LinkedList<String>(fullyQualifiedPath);
        List<String> loadedPath = new ArrayList<String>();

        Module loaded = topLevelPackage;
        while (loaded != null && !tokens.isEmpty()) {
            loadedPath.add(tokens.remove());

            // Loading must be idempotent so if the package is already loaded we
            // must return the same instance
            Module pkg = lookup(loadedPath);
            if (pkg == null)
                pkg = loadPackageIntoParent(loadedPath, loaded);
            loaded = pkg;
        }

        return loaded;
    }

    private Module loadModuleIntoParent(List<String> fullyQualifiedPath,
            Module parent) throws ParseException, IOException {

        uk.ac.ic.doc.gander.hierarchy.SourceFile module = hierarchy
                .findSourceFile(fullyQualifiedPath);
        if (module == null)
            return null;
        return new FileLoader(module, parent, this).getModule();
    }

    private Module loadPackageIntoParent(List<String> fullyQualifiedPath,
            Module parent) throws ParseException, IOException {

        uk.ac.ic.doc.gander.hierarchy.Package pkg = hierarchy
                .findPackage(fullyQualifiedPath);
        if (pkg == null)
            return null;
        return new PackageLoader(pkg, parent, this).getPackage();
    }

    @Override
    public ClassCO builtinTuple() {
        return getTopLevel().getClasses().get("tuple").codeObject();
    }

    @Override
    public ClassCO builtinDictionary() {
        return getTopLevel().getClasses().get("dict").codeObject();
    }
}