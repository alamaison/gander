package uk.ac.ic.doc.gander.importing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import uk.ac.ic.doc.gander.DottedName;
import uk.ac.ic.doc.gander.importing.ImportSimulator.Binder;
import uk.ac.ic.doc.gander.importing.ImportSimulator.Loader;

public final class ImportSimulatorTest {

	private List<TestEntry> bindings = new ArrayList<TestEntry>();

	private Binder<String, Set<String>, String, String> bindingHandler = new Binder<String, Set<String>, String, String>() {

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

		@Override
		public void bindAllNamespaceMembers(Set<String> allMembers,
				String container) {
			for (String member : allMembers) {
				bindings.add(new TestEntry(member, member.substring(member
						.lastIndexOf("@") + 1), container));
			}
		}

		public void onUnresolvedImport(
				Import<String, String> importInstance, String name,
				String receivingModule) {
			fail();
		}

		public void onUnresolvedLocalImport(
				Import<String, String> importInstance, String name) {
			fail();
		}
	};

	private Loader<String, Set<String>, String> loader = new Loader<String, Set<String>, String>() {

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

		public String loadModuleNamespaceMember(String itemName,
				String codeObjectWhoseNamespaceWeAreLoadingFrom) {
			return codeObjectWhoseNamespaceWeAreLoadingFrom + "@" + itemName;
		}

		@Override
		public Set<String> loadAllMembersInModuleNamespace(
				String sourceNamespace) {

			Set<String> members = new HashSet<String>();

			/*
			 * g in e.f.g is a special name that we use in our tests as a module
			 * containing two items, i and j. Used to test for-* import.
			 */
			if (sourceNamespace.equals("[e.f.g]")) {
				members.add("[e.f.g]@i");
				members.add("[e.f.g]@j");
			} else {
				members.add(sourceNamespace + "@none_of_the_tests_expect_me");
			}

			return members;
		}
	};

	ImportSimulator<String, Set<String>, String, String> simulator() {
		return ImportSimulator.newInstance(bindingHandler, loader);
	}

	@Test
	public void importSingle() throws Throwable {
		simulator().simulateImport(
				newImport(StandardImportStatement.newInstance(ImportPath
						.fromDottedName("x")), "[]", "[smurble]"));
		assertBindings(entry("[x]", "x", "[smurble]"));
	}

	@Test
	public void importDouble() throws Throwable {
		simulator().simulateImport(
				newImport(StandardImportStatement.newInstance(ImportPath
						.fromDottedName("p.q")), "[]", "[smurble]"));
		assertBindings(entry("[p]", "p", "[smurble]"),
				entry("[p.q]", "q", "[p]"));
	}

	@Test
	public void importTriple() throws Throwable {
		simulator().simulateImport(
				newImport(StandardImportStatement.newInstance(ImportPath
						.fromDottedName("a.b.c")), "[]", "[smurble]"));
		assertBindings(entry("[a]", "a", "[smurble]"),
				entry("[a.b]", "b", "[a]"), entry("[a.b.c]", "c", "[a.b]"));
	}

	@Test
	public void importSingleAs() throws Throwable {
		simulator().simulateImport(
				newImport(
						StandardImportAsStatement.newInstance(
								ImportPath.fromDottedName("x"), "y"), "[]",
						"[smurble]"));
		assertBindings(entry("[x]", "y", "[smurble]"));
	}

	@Test
	public void importDoubleAs() throws Throwable {
		simulator().simulateImport(
				newImport(
						StandardImportAsStatement.newInstance(
								ImportPath.fromDottedName("p.q"), "r"), "[]",
						"[smurble]"));
		assertBindings(entry("[p.q]", "r", "[smurble]"),
				entry("[p.q]", "q", "[p]"));
	}

	@Test
	public void importTripleAs() throws Throwable {
		simulator().simulateImport(
				newImport(
						StandardImportAsStatement.newInstance(
								ImportPath.fromDottedName("a.b.c"), "d"), "[]",
						"[smurble]"));
		assertBindings(entry("[a.b.c]", "d", "[smurble]"),
				entry("[a.b]", "b", "[a]"), entry("[a.b.c]", "c", "[a.b]"));
	}

	@Test
	public void fromImportSingle() throws Throwable {
		simulator().simulateImport(
				newImport(
						FromImportStatement.newInstance(
								ImportPath.fromDottedName("x"), "i"), "[]",
						"[smurble]"));
		assertBindings(entry("[x]@i", "i", "[smurble]"));
	}

	@Test
	public void fromImportDouble() throws Throwable {
		simulator().simulateImport(
				newImport(
						FromImportStatement.newInstance(
								ImportPath.fromDottedName("p.q"), "i"), "[]",
						"[smurble]"));
		assertBindings(entry("[p.q]@i", "i", "[smurble]"),
				entry("[p.q]", "q", "[p]"));
	}

	@Test
	public void fromImportTriple() throws Throwable {
		simulator().simulateImport(
				newImport(
						FromImportStatement.newInstance(
								ImportPath.fromDottedName("a.b.c"), "i"), "[]",
						"[smurble]"));
		assertBindings(entry("[a.b.c]@i", "i", "[smurble]"),
				entry("[a.b.c]", "c", "[a.b]"), entry("[a.b]", "b", "[a]"));
	}

	@Test
	public void fromImportSingleAs() throws Throwable {
		simulator().simulateImport(
				newImport(
						FromImportAsStatement.newInstance(
								ImportPath.fromDottedName("x"), "i", "j"),
						"[]", "[smurble]"));
		assertBindings(entry("[x]@i", "j", "[smurble]"));
	}

	@Test
	public void fromImportDoubleAs() throws Throwable {
		simulator().simulateImport(
				newImport(
						FromImportAsStatement.newInstance(
								ImportPath.fromDottedName("p.q"), "i", "t"),
						"[]", "[smurble]"));
		assertBindings(entry("[p.q]@i", "t", "[smurble]"),
				entry("[p.q]", "q", "[p]"));
	}

	@Test
	public void fromImportTripleAs() throws Throwable {
		simulator().simulateImport(
				newImport(
						FromImportAsStatement.newInstance(
								ImportPath.fromDottedName("a.b.c"), "i", "n"),
						"[]", "[smurble]"));
		assertBindings(entry("[a.b.c]@i", "n", "[smurble]"),
				entry("[a.b.c]", "c", "[a.b]"), entry("[a.b]", "b", "[a]"));
	}

	@Test
	public void fromImportTripleAll() throws Throwable {
		simulator().simulateImport(
				newImport(FromImportEverythingStatement
						.newInstance(ImportPath.fromDottedName("e.f.g")), "[]",
						"[smurble]"));
		assertBindings(entry("[e.f.g]@i", "i", "[smurble]"),
				entry("[e.f.g]@j", "j", "[smurble]"),
				entry("[e.f.g]", "g", "[e.f]"), entry("[e.f]", "f", "[e]"));
	}

	private static Import<String, String> newImport(
			ImportStatement specification, String relativeTo,
			String container) {
		return DefaultImport.newImport(specification, relativeTo, container);
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
