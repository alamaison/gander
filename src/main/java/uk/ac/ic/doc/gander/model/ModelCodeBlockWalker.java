package uk.ac.ic.doc.gander.model;

/**
 * Like {@link ModelWalker} but treats all model elements uniformly.
 */
public abstract class ModelCodeBlockWalker {

    public final void walk(Model model) {
        new ModelWalker() {

            @Override
            protected void visitClass(Class klass) {
                visitCodeBlock(klass);
            }

            @Override
            protected void visitFunction(Function function) {
                visitCodeBlock(function);
            }

            @Override
            protected void visitModule(Module module) {
                visitCodeBlock(module);
            }
        }.walk(model);
    }

    /**
     * Triggered on encountering a model code-block element.
     * 
     * TODO: OldNamespace should eventually become CodeBlock.
     */
    protected abstract void visitCodeBlock(OldNamespace codeBlock);
}
