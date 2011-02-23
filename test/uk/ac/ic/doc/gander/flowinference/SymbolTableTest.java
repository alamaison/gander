package uk.ac.ic.doc.gander.flowinference;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ic.doc.gander.flowinference.types.TClass;
import uk.ac.ic.doc.gander.flowinference.types.TFunction;
import uk.ac.ic.doc.gander.flowinference.types.TModule;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.Module;
import uk.ac.ic.doc.gander.model.Package;

public class SymbolTableTest {

	private static final String TEST_FOLDER = "python_test_code/symbol_table";
	private Model model;
	private Module module;
	private SymbolTable table;

	@Before
	public void setup() throws Throwable {
		URL topLevel = getClass().getResource(TEST_FOLDER);

		File topLevelDirectory = new File(topLevel.toURI());

		model = new Model(topLevelDirectory);
		module = model.getTopLevelPackage().getModules().get("start");
		table = new SymbolTable(model);
	}

	@Test
	public void localFunction() {
		assertTrue("start's symbol table doesn't contain 'alice'", table
				.symbols(module).containsKey("alice"));

		Type type = table.symbols(module).get("alice");
		assertTrue("start's symbol table contains 'alice' but it isn't "
				+ "recognised as referring to a function",
				type instanceof TFunction);

		Function alice = module.getFunctions().get("alice");

		assertEquals("Type resolved to a function but not to 'alice'", alice,
				((TFunction) type).getFunctionInstance());

	}

	@Test
	public void localClass() {
		assertTrue("start's symbol table doesn't contain 'Bob'", table.symbols(
				module).containsKey("Bob"));

		Type type = table.symbols(module).get("Bob");
		assertTrue("start's symbol table contains 'Bob' but it isn't "
				+ "recognised as referring to a class", type instanceof TClass);

		Class bob = module.getClasses().get("Bob");

		assertEquals("Type resolved to a class but not to 'Bob'", bob,
				((TClass) type).getClassInstance());
	}

	@Test
	public void localClassMethod() {
		assertTrue("start.Bob's symbol table doesn't contain 'charles'", table
				.symbols(module.getClasses().get("Bob")).containsKey("charles"));

		Class bob = module.getClasses().get("Bob");

		Type type = table.symbols(bob).get("charles");
		assertTrue("start.Bob's symbol table contains 'charles' but it isn't "
				+ "recognised as referring to a function",
				type instanceof TFunction);

		Function charles = bob.getFunctions().get("charles");

		assertEquals("Type resolved to a function but not to 'charles'",
				charles, ((TFunction) type).getFunctionInstance());
	}

	@Test
	public void siblingImport() {
		assertTrue("start's symbol table doesn't include 'gertrude'", table
				.symbols(module).containsKey("gertrude"));

		Type type = table.symbols(module).get("gertrude");
		assertTrue("start's symbol table contains 'gertrude' but it isn't "
				+ "recognised as referring to a module",
				type instanceof TModule);

		Module gertrude = model.lookupModule("gertrude");
		assertEquals("Type resolved to a module but not to 'gertrude'",
				gertrude, ((TModule) type).getModuleInstance());
	}

	@Test
	public void childImport() {
		assertTrue("start's symbol table doesn't contain 'children'", table
				.symbols(module).containsKey("children"));

		Package children = model.getTopLevelPackage().getPackages().get(
				"children");
		assertTrue("Package 'children' not in model", children != null);

		Map<String, Type> childrenTable = table.symbols(children);
		assertTrue("No symbol table for top-level package 'children'",
				childrenTable != null);

		assertTrue("children's symbol table doesn't contain 'bobby'",
				childrenTable.containsKey("bobby"));

		Type type = table.symbols(children).get("bobby");
		assertTrue("children's symbol table contains 'bobby' but it isn't "
				+ "recognised as referring to a module",
				type instanceof TModule);

		Module bobby = children.getModules().get("bobby");
		assertEquals("Type resolved to a module but not to 'bobby'", bobby,
				((TModule) type).getModuleInstance());
	}
}
