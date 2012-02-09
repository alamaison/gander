package uk.ac.ic.doc.gander.importing;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class ImportSpecificationTest {

	@Test
	public void importSingle() throws Throwable {
		StaticImportSpecification info = ImportSpecificationFactory
				.newImport("x");
		assertEquals("x", info.bindingName());
		assertEquals("", info.boundObjectParentPath().dottedName());
		assertEquals("x", info.boundObjectName());
	}

	@Test
	public void importDouble() throws Throwable {
		StaticImportSpecification info = ImportSpecificationFactory
				.newImport("p.q");
		assertEquals("", info.boundObjectParentPath().dottedName());
		assertEquals("p", info.boundObjectName());
		assertEquals("p", info.bindingName());
	}

	@Test
	public void importTriple() throws Throwable {
		StaticImportSpecification info = ImportSpecificationFactory
				.newImport("a.b.c");
		assertEquals("", info.boundObjectParentPath().dottedName());
		assertEquals("a", info.boundObjectName());
		assertEquals("a", info.bindingName());
	}

	@Test
	public void importSingleAs() throws Throwable {
		StaticImportSpecification info = ImportSpecificationFactory
				.newImportAs("x", "y");
		assertEquals("", info.boundObjectParentPath().dottedName());
		assertEquals("x", info.boundObjectName());
		assertEquals("y", info.bindingName());
	}

	@Test
	public void importDoubleAs() throws Throwable {
		StaticImportSpecification info = ImportSpecificationFactory
				.newImportAs("p.q", "r");
		assertEquals("p", info.boundObjectParentPath().dottedName());
		assertEquals("q", info.boundObjectName());
		assertEquals("r", info.bindingName());
	}

	@Test
	public void importTripleAs() throws Throwable {
		StaticImportSpecification info = ImportSpecificationFactory
				.newImportAs("a.b.c", "d");
		assertEquals("a.b", info.boundObjectParentPath().dottedName());
		assertEquals("c", info.boundObjectName());
		assertEquals("d", info.bindingName());
	}

	@Test
	public void fromImportSingle() throws Throwable {
		StaticImportSpecification info = ImportSpecificationFactory
				.newFromImport("x", "i");
		assertEquals("x", info.boundObjectParentPath().dottedName());
		assertEquals("i", info.boundObjectName());
		assertEquals("i", info.bindingName());
	}

	@Test
	public void fromImportDouble() throws Throwable {
		StaticImportSpecification info = ImportSpecificationFactory
				.newFromImport("p.q", "s");
		assertEquals("p.q", info.boundObjectParentPath().dottedName());
		assertEquals("s", info.boundObjectName());
		assertEquals("s", info.bindingName());
	}

	@Test
	public void fromImportTriple() throws Throwable {
		StaticImportSpecification info = ImportSpecificationFactory
				.newFromImport("a.b.c", "m");
		assertEquals("a.b.c", info.boundObjectParentPath().dottedName());
		assertEquals("m", info.boundObjectName());
		assertEquals("m", info.bindingName());
	}

	@Test
	public void fromImportSingleAs() throws Throwable {
		StaticImportSpecification info = ImportSpecificationFactory
				.newFromImportAs("x", "i", "j");
		assertEquals("x", info.boundObjectParentPath().dottedName());
		assertEquals("i", info.boundObjectName());
		assertEquals("j", info.bindingName());
	}

	@Test
	public void fromIimportDoubleAs() throws Throwable {
		StaticImportSpecification info = ImportSpecificationFactory
				.newFromImportAs("p.q", "s", "t");
		assertEquals("p.q", info.boundObjectParentPath().dottedName());
		assertEquals("s", info.boundObjectName());
		assertEquals("t", info.bindingName());
	}

	@Test
	public void fromImportTripleAs() throws Throwable {
		StaticImportSpecification info = ImportSpecificationFactory
				.newFromImportAs("a.b.c", "m", "n");
		assertEquals("a.b.c", info.boundObjectParentPath().dottedName());
		assertEquals("m", info.boundObjectName());
		assertEquals("n", info.bindingName());
	}
}
