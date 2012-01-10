package uk.ac.ic.doc.gander.importing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import uk.ac.ic.doc.gander.DottedName;
import uk.ac.ic.doc.gander.importing.ImportSimulator.Binder;
import uk.ac.ic.doc.gander.importing.ImportSimulator.Loader;

public final class ImportSimulatorTest {

	private List<TestEntry> bindings = new ArrayList<TestEntry>();

	private Binder<String, String, String> bindingHandler = new Binder<String, String, String>() {

		public void bindModuleToLocalName(String loadedModule, String name,
				String importReceiver) {
			bindings.add(new TestEntry(loadedModule, name, importReceiver));
		}

		public void bindModuleToName(String loadedModule, String name,
				String receivingModule) {
			bindings.add(new TestEntry(loadedModule, name, receivingModule));
		}

		public void bindObjectToLocalName(String importedObject, String name,
				String importReceiver) {
			bindings.add(new TestEntry(importedObject, name, importReceiver));
		}

		public void bindObjectToName(String importedObject, String name,
				String receivingModule) {
			bindings.add(new TestEntry(importedObject, name, receivingModule));
		}

		public void onUnresolvedImport(Import<String, String> importInstance,
				String name) {
			fail();
		}
	};

	private Loader<String, String, String> loader = new Loader<String, String, String>() {

		public String loadModule(List<String> importPath,
				String relativeToModule) {
			assertTrue("Not passed a module to be relative to: "
					+ relativeToModule, relativeToModule.matches("\\[\\S*\\]"));

			String[] moduleName = relativeToModule.split("[\\[\\]]");

			/*
			 * i is a special import name segment for our tests which means it
			 * isn't a module but is an item.
			 */
			if (importPath.get(importPath.size() - 1).equals("i")) {
				return null;
			}

			String importName = DottedName.toDottedName(importPath);
			if (moduleName.length > 0) {
				return "[" + moduleName[0] + "." + importName + "]";
			} else {
				return "[" + importName + "]";
			}
		}

		public String loadModule(List<String> importPath) {
			return loadModule(importPath, "[]");
		}

		public String loadNonModuleMember(String itemName,
				String codeObjectWhoseNamespaceWeAreLoadingFrom) {
			return codeObjectWhoseNamespaceWeAreLoadingFrom + "@" + itemName;
		}
	};

	ImportSimulator<String, String, String> simulator() {
		return new ImportSimulator<String, String, String>(bindingHandler,
				loader);
	}

	@Test
	public void importSingle() throws Throwable {
		simulator().simulateImport(
				new StandardImport<String, String>(StandardImportSpecification
						.newInstance("x"), "[]", "[smurble]"));
		assertBindings(entry("[x]", "x", "[smurble]"));
	}

	@Test
	public void importDouble() throws Throwable {
		simulator().simulateImport(
				new StandardImport<String, String>(StandardImportSpecification
						.newInstance("p.q"), "[]", "[smurble]"));
		assertBindings(entry("[p]", "p", "[smurble]"), entry("[p.q]", "q",
				"[p]"));
	}

	@Test
	public void importTriple() throws Throwable {
		simulator().simulateImport(
				new StandardImport<String, String>(StandardImportSpecification
						.newInstance("a.b.c"), "[]", "[smurble]"));
		assertBindings(entry("[a]", "a", "[smurble]"), entry("[a.b]", "b",
				"[a]"), entry("[a.b.c]", "c", "[a.b]"));
	}

	@Test
	public void importSingleAs() throws Throwable {
		simulator().simulateImport(
				new StandardImportAs<String, String>(
						StandardImportAsSpecification.newInstance("x", "y"),
						"[]", "[smurble]"));
		assertBindings(entry("[x]", "y", "[smurble]"));
	}

	@Test
	public void importDoubleAs() throws Throwable {
		simulator().simulateImport(
				new StandardImportAs<String, String>(
						StandardImportAsSpecification.newInstance("p.q", "r"),
						"[]", "[smurble]"));
		assertBindings(entry("[p.q]", "r", "[smurble]"), entry("[p.q]", "q",
				"[p]"));
	}

	@Test
	public void importTripleAs() throws Throwable {
		simulator()
				.simulateImport(
						new StandardImportAs<String, String>(
								StandardImportAsSpecification.newInstance(
										"a.b.c", "d"), "[]", "[smurble]"));
		assertBindings(entry("[a.b.c]", "d", "[smurble]"), entry("[a.b]", "b",
				"[a]"), entry("[a.b.c]", "c", "[a.b]"));
	}

	@Test
	public void fromImportSingle() throws Throwable {
		simulator().simulateImport(
				new FromImport<String, String>(FromImportSpecification
						.newInstance("x", "i"), "[]", "[smurble]"));
		assertBindings(entry("[x]@i", "i", "[smurble]"));
	}

	@Test
	public void fromImportDouble() throws Throwable {
		simulator().simulateImport(
				new FromImport<String, String>(FromImportSpecification
						.newInstance("p.q", "i"), "[]", "[smurble]"));
		assertBindings(entry("[p.q]@i", "i", "[smurble]"), entry("[p.q]", "q",
				"[p]"));
	}

	@Test
	public void fromImportTriple() throws Throwable {
		simulator().simulateImport(
				new FromImport<String, String>(FromImportSpecification
						.newInstance("a.b.c", "i"), "[]", "[smurble]"));
		assertBindings(entry("[a.b.c]@i", "i", "[smurble]"), entry("[a.b.c]",
				"c", "[a.b]"), entry("[a.b]", "b", "[a]"));
	}

	@Test
	public void fromImportSingleAs() throws Throwable {
		simulator().simulateImport(
				new FromImportAs<String, String>(FromImportAsSpecification
						.newInstance("x", "i", "j"), "[]", "[smurble]"));
		assertBindings(entry("[x]@i", "j", "[smurble]"));
	}

	@Test
	public void fromImportDoubleAs() throws Throwable {
		simulator().simulateImport(
				new FromImportAs<String, String>(FromImportAsSpecification
						.newInstance("p.q", "i", "t"), "[]", "[smurble]"));
		assertBindings(entry("[p.q]@i", "t", "[smurble]"), entry("[p.q]", "q",
				"[p]"));
	}

	@Test
	public void fromImportTripleAs() throws Throwable {
		simulator().simulateImport(
				new FromImportAs<String, String>(FromImportAsSpecification
						.newInstance("a.b.c", "i", "n"), "[]", "[smurble]"));
		assertBindings(entry("[a.b.c]@i", "n", "[smurble]"), entry("[a.b.c]",
				"c", "[a.b]"), entry("[a.b]", "b", "[a]"));
	}

	private TestEntry entry(String loadedObject, String as, String codeObject) {
		return new TestEntry(loadedObject, as, codeObject);
	}

	private void assertBindings(TestEntry... entries) {
		assertEquals(new HashSet<TestEntry>(Arrays.asList(entries)),
				new HashSet<TestEntry>(bindings));
	}

	private static final class TestEntry {
		private final String loadedObject;
		private final String as;
		private final String codeBlock;

		TestEntry(String loadedObject, String as, String codeBlock) {
			this.loadedObject = loadedObject;
			this.as = as;
			this.codeBlock = codeBlock;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((as == null) ? 0 : as.hashCode());
			result = prime * result
					+ ((codeBlock == null) ? 0 : codeBlock.hashCode());
			result = prime * result
					+ ((loadedObject == null) ? 0 : loadedObject.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TestEntry other = (TestEntry) obj;
			if (as == null) {
				if (other.as != null)
					return false;
			} else if (!as.equals(other.as))
				return false;
			if (codeBlock == null) {
				if (other.codeBlock != null)
					return false;
			} else if (!codeBlock.equals(other.codeBlock))
				return false;
			if (loadedObject == null) {
				if (other.loadedObject != null)
					return false;
			} else if (!loadedObject.equals(other.loadedObject))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "TestEntry [loadedObject=" + loadedObject + ", as=" + as
					+ ", codeBlock=" + codeBlock + "]";
		}

	}
}
