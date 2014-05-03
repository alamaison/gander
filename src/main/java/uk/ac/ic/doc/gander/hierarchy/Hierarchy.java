package uk.ac.ic.doc.gander.hierarchy;

import java.io.File;
import java.util.List;

import uk.ac.ic.doc.gander.hierarchy.build.PackageBuilder;

public class Hierarchy {

    private Package topLevel;

    Hierarchy(Iterable<File> topLevelDirectories,
            Iterable<File> topLevelSystemDirectories)
            throws InvalidElementException {
        PackageBuilder builder = new PackageBuilder(topLevelDirectories,
                topLevelSystemDirectories);
        this.topLevel = builder.getPackage();
    }

    public Package getTopLevelPackage() {
        return topLevel;
    }

    public SourceFile findSourceFile(String fullyQualifiedName) {
        return getTopLevelPackage().findSourceFile(fullyQualifiedName);
    }

    public Package findPackage(String fullyQualifiedName) {
        return getTopLevelPackage().findPackage(fullyQualifiedName);
    }

    public SourceFile findSourceFile(List<String> fullyQualifiedName) {
        return getTopLevelPackage().findSourceFile(fullyQualifiedName);
    }

    public Package findPackage(List<String> fullyQualifiedName) {
        return getTopLevelPackage().findPackage(fullyQualifiedName);
    }
}
