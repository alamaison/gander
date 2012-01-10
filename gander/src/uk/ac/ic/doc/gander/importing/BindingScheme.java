package uk.ac.ic.doc.gander.importing;

interface BindingScheme<M> {
	void bindSolitaryToken(M module, String name);

	void bindFirstToken(M module, String name);

	void bindIntermediateToken(M module, String name, M previouslyLoadedModule);

	void bindFinalToken(M module, String name, M previouslyLoadedModule);
}