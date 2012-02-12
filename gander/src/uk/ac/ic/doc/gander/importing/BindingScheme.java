package uk.ac.ic.doc.gander.importing;

interface BindingScheme<M> {
	
	void bindItems(M sourceModule);
	
	BindingBehaviour modulePathBindingBehaviour();
}