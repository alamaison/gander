package uk.ac.ic.doc.gander;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public final class ResourceResolver {

    public static File resolveRelativeToClass(File path, Class<?> root) {
        URL url = root.getResource(".");
        if (url == null)
            throw new RuntimeException("Could not resolve class");

        File rootPath;
        try {
            rootPath = new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Unable to resolve class location to "
                    + "filesystem path: " + url, e);
        }
        
        File nonCanonicalPath = new File(rootPath, path.toString());
        try {
            return nonCanonicalPath.getCanonicalFile();
        } catch (IOException e) {
            throw new RuntimeException("Unable to make path canonical: "
                    + nonCanonicalPath, e);
        }
    }
}
