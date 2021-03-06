package uk.ac.ic.doc.gander.hierarchy.build;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import uk.ac.ic.doc.gander.hierarchy.SourceFile;
import uk.ac.ic.doc.gander.hierarchy.Package;

class BuildablePackage extends Package {

    private HashMap<String, SourceFile> modules = new HashMap<String, SourceFile>();
    private HashMap<String, Package> packages = new HashMap<String, Package>();

    public BuildablePackage(String name, File initFile, Package parent,
            boolean isSystem) {
        super(name, initFile, parent, isSystem);
    }

    public Map<String, Package> getPackages() {
        return Collections.unmodifiableMap(packages);
    }

    public Map<String, SourceFile> getSourceFiles() {
        return Collections.unmodifiableMap(modules);
    }

    public void addPackage(Package subpackage) {
        packages.put(subpackage.getName(), subpackage);
    }

    public void addModule(SourceFile submodule) {
        modules.put(submodule.getName(), submodule);
    }
}
