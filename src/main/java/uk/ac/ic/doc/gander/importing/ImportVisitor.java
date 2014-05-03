package uk.ac.ic.doc.gander.importing;

import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.ast.Import;
import org.python.pydev.parser.jython.ast.ImportFrom;

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
		 * Seen an import statement.
		 * 
		 * Compound imports such as {@code import x.y.z, a.b, foo as bar} are
		 * broken up into multiple calls to this method with a separate
		 * {@link StaticImportStatement} for each.
		 * 
		 * @param importStatement
		 *            object representing the import
		 */
		void onImport(ImportStatement importStatement);

	}

	private final ImportHandler callback;

	public ImportVisitor(ImportHandler callback) {
		this.callback = callback;
	}

	@Override
	public Object visitImport(Import node) throws Exception {

		Iterable<ImportStatement> statements = ImportStatementFactory
				.fromAstNode(node);

		for (ImportStatement statement : statements) {
			callback.onImport(statement);
		}

		return null;
	}

	@Override
	public Object visitImportFrom(ImportFrom node) throws Exception {

		Iterable<ImportStatement> statements = ImportStatementFactory
				.fromAstNode(node);

		for (ImportStatement statement : statements) {
			callback.onImport(statement);
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
