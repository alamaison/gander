package uk.ac.ic.doc.gander.importing;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class ImportSpecificationTest {

	@Test
	public void importSingle() throws Throwable {
		ImportSpecification info = ImportSpecificationFactory.newImport("x");
		assertEquals("x", info.bindingName());
		assertEquals("x", info.bindingObject());
	}

	@Test
	public void importDouble() throws Throwable {
		ImportSpecification info = ImportSpecificationFactory.newImport("p.q");
		assertEquals("p", info.bindingName());
		assertEquals("p", info.bindingObject());
	}

	@Test
	public void importTriple() throws Throwable {
		ImportSpecification info = ImportSpecificationFactory
				.newImport("a.b.c");
		assertEquals("a", info.bindingName());
		assertEquals("a", info.bindingObject());
	}

	@Test
	public void importSingleAs() throws Throwable {
		ImportSpecification info = ImportSpecificationFactory.newImportAs("x",
				"y");
		assertEquals("y", info.bindingName());
		assertEquals("x", info.bindingObject());
	}

	@Test
	public void importDoubleAs() throws Throwable {
		ImportSpecification info = ImportSpecificationFactory.newImportAs(
				"p.q", "r");
		assertEquals("r", info.bindingName());
		assertEquals("p.q", info.bindingObject());
	}

	@Test
	public void importTripleAs() throws Throwable {
		ImportSpecification info = ImportSpecificationFactory.newImportAs(
				"a.b.c", "d");
		assertEquals("d", info.bindingName());
		assertEquals("a.b.c", info.bindingObject());
	}

	@Test
	public void fromImportSingle() throws Throwable {
		ImportSpecification info = ImportSpecificationFactory.newFromImport(
				"x", "i");
		assertEquals("i", info.bindingName());
		assertEquals("x.i", info.bindingObject());
	}

	@Test
	public void fromImportDouble() throws Throwable {
		ImportSpecification info = ImportSpecificationFactory.newFromImport(
				"p.q", "s");
		assertEquals("s", info.bindingName());
		assertEquals("p.q.s", info.bindingObject());
	}

	@Test
	public void fromImportTriple() throws Throwable {
		ImportSpecification info = ImportSpecificationFactory.newFromImport(
				"a.b.c", "m");
		assertEquals("m", info.bindingName());
		assertEquals("a.b.c.m", info.bindingObject());
	}

	@Test
	public void fromImportSingleAs() throws Throwable {
		ImportSpecification info = ImportSpecificationFactory.newFromImportAs(
				"x", "i", "j");
		assertEquals("j", info.bindingName());
		assertEquals("x.i", info.bindingObject());
	}

	@Test
	public void fromIimportDoubleAs() throws Throwable {
		ImportSpecification info = ImportSpecificationFactory.newFromImportAs(
				"p.q", "s", "t");
		assertEquals("t", info.bindingName());
		assertEquals("p.q.s", info.bindingObject());
	}

	@Test
	public void fromImportTripleAs() throws Throwable {
		ImportSpecification info = ImportSpecificationFactory.newFromImportAs(
				"a.b.c", "m", "n");
		assertEquals("n", info.bindingName());
		assertEquals("a.b.c.m", info.bindingObject());
	}
}
