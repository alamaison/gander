package uk.ac.ic.doc.gander.flowinference.abstractmachine;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import uk.ac.ic.doc.gander.flowinference.Namespace;
import uk.ac.ic.doc.gander.flowinference.dda.SubgoalManager;
import uk.ac.ic.doc.gander.flowinference.result.FiniteResult;
import uk.ac.ic.doc.gander.flowinference.result.RedundancyEliminator;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Result.Transformer;
import uk.ac.ic.doc.gander.flowinference.typegoals.TopT;
import uk.ac.ic.doc.gander.flowinference.typegoals.namespacename.NamespaceNameTypeGoal;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.NamespaceName;
import uk.ac.ic.doc.gander.model.ObjectInstanceNamespace;
import uk.ac.ic.doc.gander.model.codeobject.ClassCO;

/**
 * Abstract model of Python class-instance objects.
 */
public class PyInstance implements PyObject {

    private final ClassCO classObject;

    @Deprecated
    public PyInstance(Class classInstance) {
        if (classInstance == null)
            throw new NullPointerException("Class object required");
        this.classObject = classInstance.codeObject();
    }

    public PyInstance(ClassCO classObject) {
        if (classObject == null)
            throw new NullPointerException("Class object required");
        this.classObject = classObject;
    }

    @Deprecated
    public Class getClassInstance() {
        return classObject.oldStyleConflatedNamespace();
    }

    public ClassCO classObject() {
        return classObject;
    }

    @Override
    public String getName() {
        return "Instance<" + getClassInstance().getFullName() + ">";
    }

    /**
     * {@inheritDoc}
     * 
     * Members on an object are converted to bound method objects before they
     * are returned from the namespace.
     */
    @Override
    public Result<PyObject> memberType(String memberName, SubgoalManager goalManager) {

        RedundancyEliminator<PyObject> result = new RedundancyEliminator<PyObject>();

        result.add(memberTypeFromObject(memberName, goalManager));

        if (!result.isFinished()) {
            result.add(memberTypeFromClass(memberName, goalManager));
        }

        return result.result();
    }

    private Result<PyObject> memberTypeFromObject(String memberName,
            SubgoalManager goalManager) {
        return memberTypeFromNamespace(memberName, new ObjectInstanceNamespace(
                classObject), goalManager);
    }

    private Result<PyObject> memberTypeFromClass(String memberName,
            SubgoalManager goalManager) {
        Result<PyObject> unboundTypes = memberTypeFromNamespace(memberName,
                classObject.fullyQualifiedNamespace(), goalManager);

        return unboundTypes.transformResult(new TypeBinder());
    }

    private Result<PyObject> memberTypeFromNamespace(String memberName,
            Namespace namespace, SubgoalManager goalManager) {
        NamespaceName member = new NamespaceName(memberName, namespace);
        return goalManager.registerSubgoal(new NamespaceNameTypeGoal(member));
    }

    private final class TypeBinder implements Transformer<PyObject, Result<PyObject>> {

        @Override
        public Result<PyObject> transformFiniteResult(Set<PyObject> result) {
            Set<PyObject> boundTypes = new HashSet<PyObject>();

            for (PyObject unboundType : result) {
                boundTypes.add(bindType(unboundType));
            }

            return new FiniteResult<PyObject>(boundTypes);
        }

        @Override
        public Result<PyObject> transformInfiniteResult() {
            return TopT.INSTANCE;
        }
    }

    private PyObject bindType(PyObject unboundType) {
        if (unboundType instanceof PyFunction) {
            return new PyBoundMethod(((PyFunction) unboundType), this);
        } else {
            return unboundType;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * Object instances are summarised using their class's namespace so a member
     * in once instance will affect all instances.
     */
    @Override
    public Set<Namespace> memberReadableNamespaces() {
        return Collections.<Namespace> singleton(classObject
                .fullyQualifiedNamespace());
    }

    /**
     * {@inheritDoc}
     * 
     * Object instances are summarised using their class's namespace so a member
     * in once instance will affect all instances.
     */
    @Override
    public Namespace memberWriteableNamespace() {
        return classObject.fullyQualifiedNamespace();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((classObject == null) ? 0 : classObject.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PyInstance other = (PyInstance) obj;
        if (classObject == null) {
            if (other.classObject != null)
                return false;
        } else if (!classObject.equals(other.classObject))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return getName();
    }
}
