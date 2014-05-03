package uk.ac.ic.doc.gander.importing;

enum StandardImportAsBindingBehaviour implements BindingBehaviour {

    INSTANCE;

    /**
     * {@inheritDoc}
     * 
     * {@code import x as d} binds x locally (as d).
     */
    @Override
    public Behaviour bindSolitaryToken() {
        return Behaviour.BINDS_IN_RECEIVER;
    }

    /**
     * {@inheritDoc}
     * 
     * {@code import x.y.z as d} doesn't bind x to anything.
     */
    @Override
    public Behaviour bindFirstToken() {
        return Behaviour.NOT_BOUND;
    }

    /**
     * {@inheritDoc}
     * 
     * {@code import x.y.z as d} binds y in z.
     */
    @Override
    public Behaviour bindIntermediateToken() {
        return Behaviour.BINDS_IN_PREVIOUS_MODULE;
    }

    /**
     * {@inheritDoc}
     * 
     * {@code import x.y.z as d} binds z in y and locally (as d).
     */
    @Override
    public Behaviour bindFinalToken() {
        return Behaviour.BINDS_IN_BOTH;
    }
}