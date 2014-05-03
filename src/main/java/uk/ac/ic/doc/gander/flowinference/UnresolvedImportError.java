/**
 * 
 */
package uk.ac.ic.doc.gander.flowinference;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import uk.ac.ic.doc.gander.model.Module;

// TODO: Change to Exception to force explicit handling
public class UnresolvedImportError extends Error {

    private static final long serialVersionUID = -3845956678785948260L;
    private List<String> importPath;
    private Module relativeToPackage;

    public UnresolvedImportError(List<String> importPath,
            Module relativeToPackage) {
        assert importPath != null;
        // relativeToPackage may be null when import occurs in top level e.g.
        // builtins

        this.importPath = importPath;
        this.relativeToPackage = relativeToPackage;
    }

    @Override
    public String getMessage() {
        if (relativeToPackage != null) {
            return "'" + join(importPath, ".")
                    + "' couldn't be resolved from package '"
                    + relativeToPackage.getFullName() + "'";
        } else {
            return "'" + join(importPath, ".")
                    + "' couldn't be resolved in the top-level package'";
        }
    }

    private static String join(Collection<?> s, String delimiter) {
        StringBuilder builder = new StringBuilder();
        Iterator<?> iter = s.iterator();
        while (iter.hasNext()) {
            builder.append(iter.next());
            if (!iter.hasNext()) {
                break;
            }
            builder.append(delimiter);
        }
        return builder.toString();
    }

}