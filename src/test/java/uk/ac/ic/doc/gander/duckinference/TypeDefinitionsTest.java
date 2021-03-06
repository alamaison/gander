package uk.ac.ic.doc.gander.duckinference;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.hierarchy.HierarchyFactory;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.DefaultModel;
import uk.ac.ic.doc.gander.model.ModelWalker;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.MutableModel;
import uk.ac.ic.doc.gander.model.codeobject.ClassCO;

public class TypeDefinitionsTest {

    private static final String TEST_FOLDER = "python_test_code";
    private MutableModel model;
    private Hierarchy hierarchy;

    public void setup(String caseName) throws Throwable {
        URL testFolder = getClass().getResource(TEST_FOLDER);
        File topLevel = new File(new File(testFolder.toURI()), caseName);

        hierarchy = HierarchyFactory.createHierarchy(topLevel);
        model = new DefaultModel(hierarchy);
    }

    @Test
    public void infileSingle() throws Throwable {
        setup("infile_single");

        Module start = model.loadModule("start");

        Class expected[] = { start.getClasses().get("A"),
                start.getClasses().get("B"), start.getClasses().get("C") };

        assertCollectedClasses(expected);
    }

    @Test
    public void inherited() throws Throwable {
        setup("inherited");

        Module start = model.loadModule("start");

        Class expected[] = { start.getClasses().get("A"),
                start.getClasses().get("B"), start.getClasses().get("C"),
                start.getClasses().get("Base") };

        assertCollectedClasses(expected);
    }

    private void assertCollectedClasses(Class[] specifiedExpected) {
        Set<ClassCO> expected = new HashSet<ClassCO>();
        for (Class klass : specifiedExpected) {
            expected.add(klass.codeObject());
        }

        Collection<ClassCO> builtins = collectBuiltinClasses();
        expected.addAll(builtins);

        assertEquals("Types collected don't match expected classes", expected,
                new LoadedTypeDefinitions(model).getDefinitions());
    }

    private Collection<ClassCO> collectBuiltinClasses() {
        final Set<ClassCO> classes = new HashSet<ClassCO>();

        new ModelWalker() {

            @Override
            protected void visitClass(Class klass) {
                classes.add(klass.codeObject());
            }
        }.walk(model);

        return classes;
    }
}
