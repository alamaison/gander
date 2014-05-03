package uk.ac.ic.doc.gander.importing;

/**
 * Model of the module token binding behaviour common to all from-style imports.
 * 
 * {@code from x.y.z import p}, {@code from x.y.z import p as d} and
 * {@code from x.y.z import *} all bind the tokens of the source module path in
 * the same way.
 */
enum FromImportBindingBehaviour implements BindingBehaviour {

    INSTANCE;

    /**
     * {@inheritDoc}
     * 
     * {@code from x import p as d} doesn't bind x to anything.
     */
    @Override
    public Behaviour bindSolitaryToken() {
        return Behaviour.NOT_BOUND;
    }

    /**
     * {@inheritDoc}
     * 
     * {@code from x.y.z import p as d} doesn't bind x to anything.
     */
    @Override
    public Behaviour bindFirstToken() {
        return Behaviour.NOT_BOUND;
    }

    /**
     * {@inheritDoc}
     * 
     * {@code from x.y.z import p as d} binds y in x.
     */
    @Override
    public Behaviour bindIntermediateToken() {
        return Behaviour.BINDS_IN_PREVIOUS_MODULE;
    }

    /**
     * {@inheritDoc}
     * 
     * {@code from x.y.z import p as d} binds z in y.
     */
    @Override
    public Behaviour bindFinalToken() {
        return Behaviour.BINDS_IN_PREVIOUS_MODULE;
    }
}