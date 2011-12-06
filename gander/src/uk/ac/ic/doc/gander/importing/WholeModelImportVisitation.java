package uk.ac.ic.doc.gander.importing;

import org.python.pydev.parser.jython.SimpleNode;

import uk.ac.ic.doc.gander.ast.LocalCodeBlockVisitor;
import uk.ac.ic.doc.gander.model.CodeObjectWalker;
import uk.ac.ic.doc.gander.model.Model;
import uk.ac.ic.doc.gander.model.codeobject.CodeObject;

/**
 * Visitation of every import statement in the given runtime model.
 */
public final class WholeModelImportVisitation {

	private final ImportHandler<CodeObject> callback;
	private final Model model;

	public WholeModelImportVisitation(Model model, ImportHandler<CodeObject> callback) {
		this.model = model;
		this.callback = callback;
		walkModel();
	}

	void walkModel() {

		/*
		 * The code below basically does just two things: walks the model
		 * looking for imports and then uses the ImportVisitor to unpack them,
		 * calling the callback as appropriate.
		 */
		new CodeObjectWalker() {

			@Override
			protected void visitCodeObject(final CodeObject codeObject) {
				try {
					codeObject.codeBlock().accept(new LocalCodeBlockVisitor() {

						@Override
						protected Object unhandled_node(SimpleNode node)
								throws Exception {
							return node.accept(newImportVisitor(codeObject));
						}

						@Override
						public void traverse(SimpleNode node) throws Exception {
							node.traverse(this);
						}
					});
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

		}.walk(model.getTopLevel().codeObject());
	}

	/**
	 * Construct an {@link ImportVisitor} with a callback that passes the call
	 * on up to our callback with the code object as an extra parameter.
	 * 
	 * @param codeObject
	 *            code object visitor is going to be invoked on.
	 * @return new import visitor
	 */
	private ImportVisitor newImportVisitor(final CodeObject codeObject) {

		return new ImportVisitor(new ImportVisitor.ImportHandler() {

			public void onImportFrom(String moduleName, String itemName) {
				callback.onImportFrom(codeObject, moduleName, itemName);
			}

			public void onImportFromAs(String moduleName, String itemName,
					String asName) {
				callback.onImportFromAs(codeObject, moduleName, itemName,
						asName);
			}

			public void onImportAs(String moduleName, String asName) {
				callback.onImportAs(codeObject, moduleName, asName);
			}

			public void onImport(String moduleName) {
				callback.onImport(codeObject, moduleName);
			}
		});
	}
}
