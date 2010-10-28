package uk.ac.ic.doc.cfg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.ic.doc.cfg.model.Module;
import uk.ac.ic.doc.cfg.model.Package;

public class ModelTest {

	// private static final int COPY_BUFFER_SIZE = 1024;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	// private void copy(InputStream from, OutputStream out) throws IOException
	// {
	//		
	// byte[] buffer = new byte[COPY_BUFFER_SIZE];
	// int len;
	// while ((len = from.read(buffer)) > 0) {
	// out.write(buffer);
	// }
	// }

	@Test
	public void testGetTopLevelPackages() throws Throwable {
		URL topLevel = getClass().getResource(
				"python_test_code/model_structure");

		File topLevelDirectory = new File(topLevel.toURI());

		Model model = new Model(topLevelDirectory);
		Map<String, Package> packages = model.getTopLevelPackage()
				.getPackages();
		assertEquals(1, packages.size());
		assertTrue(packages.containsKey("my_package"));
		assertEquals("my_package", packages.get("my_package").getName());
	}

	@Test
	public void testGetTopLevelModules() throws Throwable {
		URL topLevel = getClass().getResource(
				"python_test_code/model_structure");

		File topLevelDirectory = new File(topLevel.toURI());

		Model model = new Model(topLevelDirectory);
		Map<String, Module> modules = model.getTopLevelPackage().getModules();
		assertEquals(2, modules.size());
		assertTrue(modules.containsKey("my_module1"));
		assertTrue(modules.containsKey("my_module2"));
	}

}
