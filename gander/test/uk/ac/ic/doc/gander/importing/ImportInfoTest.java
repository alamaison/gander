package uk.ac.ic.doc.gander.importing;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class ImportInfoTest {

	@Test
	public void importSingle() throws Throwable {
		ImportInfo info = ImportInfoFactory.newImport("x");
		assertEquals("x", info.bindingName());
		assertEquals("x", info.bindingObject());
	}

	@Test
	public void importDouble() throws Throwable {
		ImportInfo info = ImportInfoFactory.newImport("p.q");
		assertEquals("p", info.bindingName());
		assertEquals("p", info.bindingObject());
	}

	@Test
	public void importTriple() throws Throwable {
		ImportInfo info = ImportInfoFactory.newImport("a.b.c");
		assertEquals("a", info.bindingName());
		assertEquals("a", info.bindingObject());
	}

	@Test
	public void importSingleAs() throws Throwable {
		ImportInfo info = ImportInfoFactory.newImportAs("x", "y");
		assertEquals("y", info.bindingName());
		assertEquals("x", info.bindingObject());
	}

	@Test
	public void importDoubleAs() throws Throwable {
		ImportInfo info = ImportInfoFactory.newImportAs("p.q", "r");
		assertEquals("r", info.bindingName());
		assertEquals("p.q", info.bindingObject());
	}

	@Test
	public void importTripleAs() throws Throwable {
		ImportInfo info = ImportInfoFactory.newImportAs("a.b.c", "d");
		assertEquals("d", info.bindingName());
		assertEquals("a.b.c", info.bindingObject());
	}

	@Test
	public void fromImportSingle() throws Throwable {
		ImportInfo info = ImportInfoFactory.newFromImport("x", "i");
		assertEquals("i", info.bindingName());
		assertEquals("x.i", info.bindingObject());
	}

	@Test
	public void fromImportDouble() throws Throwable {
		ImportInfo info = ImportInfoFactory.newFromImport("p.q", "s");
		assertEquals("s", info.bindingName());
		assertEquals("p.q.s", info.bindingObject());
	}

	@Test
	public void fromImportTriple() throws Throwable {
		ImportInfo info = ImportInfoFactory.newFromImport("a.b.c", "m");
		assertEquals("m", info.bindingName());
		assertEquals("a.b.c.m", info.bindingObject());
	}

	@Test
	public void fromImportSingleAs() throws Throwable {
		ImportInfo info = ImportInfoFactory.newFromImportAs("x", "i", "j");
		assertEquals("j", info.bindingName());
		assertEquals("x.i", info.bindingObject());
	}

	@Test
	public void fromIimportDoubleAs() throws Throwable {
		ImportInfo info = ImportInfoFactory.newFromImportAs("p.q", "s", "t");
		assertEquals("t", info.bindingName());
		assertEquals("p.q.s", info.bindingObject());
	}

	@Test
	public void fromImportTripleAs() throws Throwable {
		ImportInfo info = ImportInfoFactory.newFromImportAs("a.b.c", "m", "n");
		assertEquals("n", info.bindingName());
		assertEquals("a.b.c.m", info.bindingObject());
	}
}
