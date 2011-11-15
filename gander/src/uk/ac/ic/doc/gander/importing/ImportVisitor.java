package uk.ac.ic.doc.gander.importing;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Import;
import org.python.pydev.parser.jython.ast.ImportFrom;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.aliasType;

import uk.ac.ic.doc.gander.ast.LocalCodeBlockVisitor;

/**
 * Visitor for Python import statements.
 * 
 * Unpacks the statements (which can import many items in a single statement)
 * and calls the given handler for each import name encountered.
 * 
 * Does not traverse any nodes so must be passed directly to the {@link Import}
 * and {@link ImportFrom} nodes in order to do anything. Can be passed to all
 * other nodes safely (for instance by delegating from {@code unhandled_node})
 * but nothing will happen.
 */
public final class ImportVisitor extends LocalCodeBlockVisitor {

	/**
	 * Callback to react to importations.
	 */
	public interface ImportHandler {

		/**
		 * Seen {@code import x.y.z} style import.
		 * 
		 * Compound imports such as {@code import x.y.z, a.b, foo as bar} are
		 * broken up into multiple calls to this method or {@link onImportAs}.
		 * 
		 * @param moduleName
		 *            name of module relative to code block in which it appeared
		 *            (really relative to that code block's containing module)
		 */
		void onImport(String moduleName);

		/**
		 * 
		 * Seen {@code import x.y.z as bar} style import.
		 * 
		 * Compound imports such as {@code import x.y.z as bar, a.b, foo} are
		 * broken up into multiple calls to this method or {@link onImport}.
		 * 
		 * @param moduleName
		 *            name of module relative to code block in which it appeared
		 *            (really relative to that code block's containing module)
		 * @param asName
		 *            name to which the imported module's code object is bound
		 *            in the namespace of the code block in which this import
		 *            statement appeared
		 */
		void onImportAs(String moduleName, String asName);

		/**
		 * Seen {@code from x.y import i} style import.
		 * 
		 * Compound imports such as {@code from x.y import i, j, k as p} are
		 * broken up into multiple calls to this method or
		 * {@link onImportFromAs}.
		 * 
		 * @param moduleName
		 *            name of module relative to code block in which it appeared
		 *            (really relative to that code block's containing module)
		 * @param itemName
		 *            name of object in moduleName's namespace being imported
		 *            into the namespace of the code block in which this import
		 *            statement appeared
		 */
		void onImportFrom(String moduleName, String itemName);

		/**
		 * Seen {@code from x.y import i as p} style import.
		 * 
		 * Compound imports such as {@code from x.y import i as p, j, k as q}
		 * are broken up into multiple calls to this method or
		 * {@link onImportFrom}.
		 * 
		 * @param moduleName
		 *            name of module relative to code block in which it appeared
		 *            (really relative to that code block's containing module)
		 * @param itemName
		 *            name of object in moduleName's namespace being imported
		 *            into the namespace of the code block in which this import
		 *            statement appeared
		 * @param asName
		 *            name to which the imported object is bound in the
		 *            namespace of the code block in which this import statement
		 *            appeared
		 */
		void onImportFromAs(String moduleName, String itemName, String asName);

	}

	private final ImportHandler callback;

	public ImportVisitor(ImportHandler callback) {
		this.callback = callback;
	}

	@Override
	public Object visitImport(Import node) throws Exception {
		for (aliasType alias : node.names) {

			String importName = ((NameTok) alias.name).id;

			if (alias.asname != null) {
				callback.onImportAs(importName, ((NameTok) alias.asname).id);
			} else {
				callback.onImport(importName);
			}
		}

		return null;
	}

	@Override
	public Object visitImportFrom(ImportFrom node) throws Exception {

		String importName = ((NameTok) node.module).id;

		for (aliasType alias : node.names) {

			String itemName = ((NameTok) alias.name).id;

			if (alias.asname != null) {
				callback.onImportFromAs(importName, itemName,
						((NameTok) alias.asname).id);
			} else {
				callback.onImportFrom(importName, itemName);
			}
		}

		return null;
	}

	@Override
	public void traverse(SimpleNode node) throws Exception {
	}

	@Override
	protected Object unhandled_node(SimpleNode node) throws Exception {
		return null;
	}
}
