package uk.ac.ic.doc.gander.importing;

interface BindingBehaviour {

    enum Behaviour {
        NOT_BOUND, BINDS_IN_RECEIVER, BINDS_IN_PREVIOUS_MODULE, BINDS_IN_BOTH
    }

    Behaviour bindSolitaryToken();

    Behaviour bindFirstToken();

    Behaviour bindIntermediateToken();

    Behaviour bindFinalToken();
}