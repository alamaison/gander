package uk.ac.ic.doc.gander.importing;

interface BindingScheme<O, M> {
	void bindSolitaryToken(O object, String name);

	void bindFirstToken(O object, String name);

	void bindIntermediateToken(O object, String name,
			M previouslyLoadedModule);

	void bindFinalToken(O object, String name, M previouslyLoadedModule);
}