package uk.ac.ic.doc.gander.analysis.inheritance;

import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.analysis.ClassResolver;
import uk.ac.ic.doc.gander.model.Class;
import uk.ac.ic.doc.gander.model.Model;

public class InheritanceTree {
	private Model model;
	private Node root;

	public class Node {
		private Class klass;
		private Node[] bases;

		public Node(Class klass) throws Exception {
			assert klass != null;

			this.klass = klass;
			exprType[] baseExpressions = klass.inheritsFrom();
			bases = new Node[baseExpressions.length];

			for (int i = 0; i < baseExpressions.length; ++i) {
				exprType expr = baseExpressions[i];
				Class base = resolveClass(expr, klass);
				if (base != null) {
					if (base.equals(klass))
						bases[i] = this;
					else
						bases[i] = new Node(base);
				}
			}
		}

		public Node[] getBases() {
			return bases;
		}

		public Class getKlass() {
			return klass;
		}
	}

	public InheritanceTree(Class klass, Model model) throws Exception {
		this.model = model;
		root = new Node(klass);
	}

	public Node getTree() {
		return root;
	}

	private Class resolveClass(exprType expr, Class subclass) throws Exception {
		return new ClassResolver(expr, subclass.getParentScope(), model)
				.getResolvedClass();
	}

}
