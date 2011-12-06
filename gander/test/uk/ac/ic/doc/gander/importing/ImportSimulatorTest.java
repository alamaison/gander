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
import uk.ac.ic.doc.gander.importing.DefaultImportSimulator.ImportEvents;

public final class ImportSimulatorTest {

	private List<TestEntry> bindings = new ArrayList<TestEntry>();

	ImportSimulator simulator() {
		return new DefaultImportSimulator<String, String, String>("[smurble]",
				new ImportEvents<String, String, String>() {

					public void bindName(String loadedObject, String as,
							String codeBlock) {
						bindings
								.add(new TestEntry(loadedObject, as, codeBlock));
					}

					public void onUnresolvedImport(List<String> importPath,
							String relativeTo, String as, String codeBlock) {
						fail();
					}

					public void onUnresolvedImportFromItem(
							List<String> fromPath, String relativeTo,
							String itemName, String as, String codeBlock) {
						fail();
					}

					public String loadModule(List<String> importPath,
							String relativeToModule) {
						assertTrue("Not passed a module to be relative to: "
								+ relativeToModule, relativeToModule
								.matches("\\[\\S*\\]"));

						String[] moduleName = relativeToModule
								.split("[\\[\\]]");

						/*
						 * i is a special import name segment for our tests
						 * which means it isn't a module but is an item.
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

					public String lookupNonModuleMember(String itemName,
							String codeObjectWhoseNamespaceWeAreLoadingFrom) {
						return codeObjectWhoseNamespaceWeAreLoadingFrom + "@"
								+ itemName;
					}

					public String parentModule(String importReceiver) {
						return "[]"; // parent of code object [smurble] is []
					}
				});
	}

	@Test
	public void importSingle() throws Throwable {
		simulator().simulateImport("x");
		assertBindings(entry("[x]", "x", "[smurble]"));
	}

	@Test
	public void importDouble() throws Throwable {
		simulator().simulateImport("p.q");
		assertBindings(entry("[p]", "p", "[smurble]"), entry("[p.q]", "q",
				"[p]"));
	}

	@Test
	public void importTriple() throws Throwable {
		simulator().simulateImport("a.b.c");
		assertBindings(entry("[a]", "a", "[smurble]"), entry("[a.b]", "b",
				"[a]"), entry("[a.b.c]", "c", "[a.b]"));
	}

	@Test
	public void importSingleAs() throws Throwable {
		simulator().simulateImportAs("x", "y");
		assertBindings(entry("[x]", "y", "[smurble]"));
	}

	@Test
	public void importDoubleAs() throws Throwable {
		simulator().simulateImportAs("p.q", "r");
		assertBindings(entry("[p.q]", "r", "[smurble]"), entry("[p.q]", "q",
				"[p]"));
	}

	@Test
	public void importTripleAs() throws Throwable {
		simulator().simulateImportAs("a.b.c", "d");
		assertBindings(entry("[a.b.c]", "d", "[smurble]"), entry("[a.b]", "b",
				"[a]"), entry("[a.b.c]", "c", "[a.b]"));
	}

	@Test
	public void fromImportSingle() throws Throwable {
		simulator().simulateImportFrom("x", "i");
		assertBindings(entry("[x]@i", "i", "[smurble]"));
	}

	@Test
	public void fromImportDouble() throws Throwable {
		simulator().simulateImportFrom("p.q", "i");
		assertBindings(entry("[p.q]@i", "i", "[smurble]"), entry("[p.q]", "q",
				"[p]"));
	}

	@Test
	public void fromImportTriple() throws Throwable {
		simulator().simulateImportFrom("a.b.c", "i");
		assertBindings(entry("[a.b.c]@i", "i", "[smurble]"), entry("[a.b.c]",
				"c", "[a.b]"), entry("[a.b]", "b", "[a]"));
	}

	@Test
	public void fromImportSingleAs() throws Throwable {
		simulator().simulateImportFromAs("x", "i", "j");
		assertBindings(entry("[x]@i", "j", "[smurble]"));
	}

	@Test
	public void fromIimportDoubleAs() throws Throwable {
		simulator().simulateImportFromAs("p.q", "i", "t");
		assertBindings(entry("[p.q]@i", "t", "[smurble]"), entry("[p.q]", "q",
				"[p]"));
	}

	@Test
	public void fromImportTripleAs() throws Throwable {
		simulator().simulateImportFromAs("a.b.c", "i", "n");
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
