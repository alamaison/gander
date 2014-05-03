package uk.ac.ic.doc.gander.model;

public abstract class ModelWalker {

	public final void walk(Model model) {
		new NamespaceWalker() {

			@Override
			protected void visitClass(Class klass) {
				ModelWalker.this.visitClass(klass);
			}

			@Override
			protected void visitFunction(Function function) {
				ModelWalker.this.visitFunction(function);
			}

			@Override
			protected void visitModule(Module module) {
				ModelWalker.this.visitModule(module);
			}
		}.walk(model.getTopLevel());
	}

	protected void visitModule(Module module) {
	}

	protected void visitClass(Class klass) {
	}

	protected void visitFunction(Function function) {
	}
}
