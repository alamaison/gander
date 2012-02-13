package uk.ac.ic.doc.gander.importing;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class ImportSpecificationTest {

	@Test
	public void importSingle() throws Throwable {
		StaticImportStatement info = ImportStatementFactory
				.newImport(ImportPath.fromDottedName("x"));
		assertEquals("x", info.bindingName());
		assertEquals("", info.boundObjectParentPath().dottedName());
		assertEquals("x", info.boundObjectName());
	}

	@Test
	public void importDouble() throws Throwable {
		StaticImportStatement info = ImportStatementFactory
				.newImport(ImportPath.fromDottedName("p.q"));
		assertEquals("", info.boundObjectParentPath().dottedName());
		assertEquals("p", info.boundObjectName());
		assertEquals("p", info.bindingName());
	}

	@Test
	public void importTriple() throws Throwable {
		StaticImportStatement info = ImportStatementFactory
				.newImport(ImportPath.fromDottedName("a.b.c"));
		assertEquals("", info.boundObjectParentPath().dottedName());
		assertEquals("a", info.boundObjectName());
		assertEquals("a", info.bindingName());
	}

	@Test
	public void importSingleAs() throws Throwable {
		StaticImportStatement info = ImportStatementFactory
				.newImportAs(ImportPath.fromDottedName("x"), "y");
		assertEquals("", info.boundObjectParentPath().dottedName());
		assertEquals("x", info.boundObjectName());
		assertEquals("y", info.bindingName());
	}

	@Test
	public void importDoubleAs() throws Throwable {
		StaticImportStatement info = ImportStatementFactory
				.newImportAs(ImportPath.fromDottedName("p.q"), "r");
		assertEquals("p", info.boundObjectParentPath().dottedName());
		assertEquals("q", info.boundObjectName());
		assertEquals("r", info.bindingName());
	}

	@Test
	public void importTripleAs() throws Throwable {
		StaticImportStatement info = ImportStatementFactory
				.newImportAs(ImportPath.fromDottedName("a.b.c"), "d");
		assertEquals("a.b", info.boundObjectParentPath().dottedName());
		assertEquals("c", info.boundObjectName());
		assertEquals("d", info.bindingName());
	}

	@Test
	public void fromImportSingle() throws Throwable {
		StaticImportStatement info = ImportStatementFactory
				.newFromImport(ImportPath.fromDottedName("x"), "i");
		assertEquals("x", info.boundObjectParentPath().dottedName());
		assertEquals("i", info.boundObjectName());
		assertEquals("i", info.bindingName());
	}

	@Test
	public void fromImportDouble() throws Throwable {
		StaticImportStatement info = ImportStatementFactory
				.newFromImport(ImportPath.fromDottedName("p.q"), "s");
		assertEquals("p.q", info.boundObjectParentPath().dottedName());
		assertEquals("s", info.boundObjectName());
		assertEquals("s", info.bindingName());
	}

	@Test
	public void fromImportTriple() throws Throwable {
		StaticImportStatement info = ImportStatementFactory
				.newFromImport(ImportPath.fromDottedName("a.b.c"), "m");
		assertEquals("a.b.c", info.boundObjectParentPath().dottedName());
		assertEquals("m", info.boundObjectName());
		assertEquals("m", info.bindingName());
	}

	@Test
	public void fromImportSingleAs() throws Throwable {
		StaticImportStatement info = ImportStatementFactory
				.newFromImportAs(ImportPath.fromDottedName("x"), "i", "j");
		assertEquals("x", info.boundObjectParentPath().dottedName());
		assertEquals("i", info.boundObjectName());
		assertEquals("j", info.bindingName());
	}

	@Test
	public void fromIimportDoubleAs() throws Throwable {
		StaticImportStatement info = ImportStatementFactory
				.newFromImportAs(ImportPath.fromDottedName("p.q"), "s", "t");
		assertEquals("p.q", info.boundObjectParentPath().dottedName());
		assertEquals("s", info.boundObjectName());
		assertEquals("t", info.bindingName());
	}

	@Test
	public void fromImportTripleAs() throws Throwable {
		StaticImportStatement info = ImportStatementFactory
				.newFromImportAs(ImportPath.fromDottedName("a.b.c"), "m", "n");
		assertEquals("a.b.c", info.boundObjectParentPath().dottedName());
		assertEquals("m", info.boundObjectName());
		assertEquals("n", info.bindingName());
	}
}
