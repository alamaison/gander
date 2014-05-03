package uk.ac.ic.doc.gander.importing;

enum StandardImportBindingBehaviour implements BindingBehaviour {

	INSTANCE;

	/**
	 * {@inheritDoc}
	 * 
	 * {@code import x} binds x locally.
	 */
	@Override
	public Behaviour bindSolitaryToken() {
		return Behaviour.BINDS_IN_RECEIVER;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * {@code import x.y.z} binds x locally.
	 */
	@Override
	public Behaviour bindFirstToken() {
		return Behaviour.BINDS_IN_RECEIVER;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * {@code import x.y.z} binds y in z.
	 */
	@Override
	public Behaviour bindIntermediateToken() {
		return Behaviour.BINDS_IN_PREVIOUS_MODULE;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * {@code import x.y.z} binds z in y.
	 */
	@Override
	public Behaviour bindFinalToken() {
		return Behaviour.BINDS_IN_PREVIOUS_MODULE;
	}
}