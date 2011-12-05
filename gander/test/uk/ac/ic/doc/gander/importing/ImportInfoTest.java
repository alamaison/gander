package uk.ac.ic.doc.gander.importing;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class ImportInfoTest {

	@Test
	public void importSingle() throws Throwable {
		ImportInfo info = ImportInfoFactory.newImport("x");
		assertEquals(info.bindingName(), "x");
		assertEquals(info.bindingObject(), "x");
	}

	@Test
	public void importDouble() throws Throwable {
		ImportInfo info = ImportInfoFactory.newImport("p.q");
		assertEquals(info.bindingName(), "p");
		assertEquals(info.bindingObject(), "p");
	}

	@Test
	public void importTriple() throws Throwable {
		ImportInfo info = ImportInfoFactory.newImport("a.b.c");
		assertEquals(info.bindingName(), "a");
		assertEquals(info.bindingObject(), "a");
	}

	@Test
	public void importSingleAs() throws Throwable {
		ImportInfo info = ImportInfoFactory.newImportAs("x", "y");
		assertEquals(info.bindingName(), "y");
		assertEquals(info.bindingObject(), "x");
	}

	@Test
	public void importDoubleAs() throws Throwable {
		ImportInfo info = ImportInfoFactory.newImportAs("p.q", "r");
		assertEquals(info.bindingName(), "r");
		assertEquals(info.bindingObject(), "p.q");
	}

	@Test
	public void importTripleAs() throws Throwable {
		ImportInfo info = ImportInfoFactory.newImportAs("a.b.c", "d");
		assertEquals(info.bindingName(), "d");
		assertEquals(info.bindingObject(), "a.b.c");
	}

	@Test
	public void fromImportSingle() throws Throwable {
		ImportInfo info = ImportInfoFactory.newFromImport("x", "i");
		assertEquals(info.bindingName(), "i");
		assertEquals(info.bindingObject(), "x.i");
	}

	@Test
	public void fromImportDouble() throws Throwable {
		ImportInfo info = ImportInfoFactory.newFromImport("p.q", "s");
		assertEquals(info.bindingName(), "s");
		assertEquals(info.bindingObject(), "p.q.s");
	}

	@Test
	public void fromImportTriple() throws Throwable {
		ImportInfo info = ImportInfoFactory.newFromImport("a.b.c", "m");
		assertEquals(info.bindingName(), "m");
		assertEquals(info.bindingObject(), "a.b.c.m");
	}

	@Test
	public void fromImportSingleAs() throws Throwable {
		ImportInfo info = ImportInfoFactory.newFromImportAs("x", "i", "j");
		assertEquals(info.bindingName(), "j");
		assertEquals(info.bindingObject(), "x.i");
	}

	@Test
	public void fromIimportDoubleAs() throws Throwable {
		ImportInfo info = ImportInfoFactory.newFromImportAs("p.q", "s", "t");
		assertEquals(info.bindingName(), "t");
		assertEquals(info.bindingObject(), "p.q.s");
	}

	@Test
	public void fromImportTripleAs() throws Throwable {
		ImportInfo info = ImportInfoFactory.newFromImportAs("a.b.c", "m", "n");
		assertEquals(info.bindingName(), "n");
		assertEquals(info.bindingObject(), "a.b.c.m");
	}
}
