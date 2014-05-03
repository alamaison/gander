package uk.ac.ic.doc.gander.model;

public abstract class NamespaceWalker {

    public final void walk(Module topLevel) {
        visitModule(topLevel);
        walkThroughNamespace(topLevel);
    }

    public final void walk(Class topLevel) {
        visitClass(topLevel);
        walkThroughNamespace(topLevel);
    }

    public final void walk(Function topLevel) {
        visitFunction(topLevel);
        walkThroughNamespace(topLevel);
    }

    private void walkThroughNamespace(OldNamespace namespace) {
        for (Module module : namespace.getModules().values()) {
            walk(module);
        }
        for (Class klass : namespace.getClasses().values()) {
            walk(klass);
        }
        for (Function function : namespace.getFunctions().values()) {
            walk(function);
        }
    }

    protected void visitModule(Module module) {
    }

    protected void visitClass(Class klass) {
    }

    protected void visitFunction(Function function) {
    }
}
