package uk.ac.ic.doc.gander.analysers;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ParseException;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.NameTok;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.analysis.CallFinder;
import uk.ac.ic.doc.gander.analysis.CallFinder.EventHandler;
import uk.ac.ic.doc.gander.cfg.BasicBlock;
import uk.ac.ic.doc.gander.ducktype.InterfaceRecovery;
import uk.ac.ic.doc.gander.ducktype.NamedMethodFeature;
import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.flowinference.ZeroCfaTypeEngine;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.hierarchy.HierarchyWalker;
import uk.ac.ic.doc.gander.hierarchy.Package;
import uk.ac.ic.doc.gander.hierarchy.SourceFile;
import uk.ac.ic.doc.gander.interfacetype.Feature;
import uk.ac.ic.doc.gander.interfacetype.InterfaceType;
import uk.ac.ic.doc.gander.model.DefaultModel;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.ModelWalkerWithParent;
import uk.ac.ic.doc.gander.model.MutableModel;

/**
 * Study how often code completion would have included the methods called in the
 * program.
 */
public class CodeCompletionPrediction {

	private InterfaceRecovery duckTyper;
	private final MutableModel model;

	private long callSites = 0;
	private long callSitesPredictedCorrectly = 0;

	public CodeCompletionPrediction(Hierarchy hierarchy, File projectRoot)
			throws Exception {
		System.out.println("Creating model from hierarchy");
		this.model = new DefaultModel(hierarchy);
		System.out.println("Loading all non-system modules in hierarchy");
		new HierarchyLoader().walk(hierarchy);

		System.out.println("Performing flow-based type inference");

		this.duckTyper = new InterfaceRecovery(new TypeResolver(
				new ZeroCfaTypeEngine()));

		System.out.println("Running signature analysis");
		new ModelDucker().walk(model);
	}

	public float result() {
		if (callSites == 0)
			return 0F;
		else
			return callSitesPredictedCorrectly * 100F / callSites;
	}

	private final class ModelDucker extends ModelWalkerWithParent {

		@Override
		protected void visitFunction(Function function) {
			// only analyse methods within our target project's namespace
			if (function.isSystem())
				return;

			for (BasicBlock block : function.getCfg().getBlocks()) {

				for (Call call : calls(block)) {

					if (call.func instanceof Attribute) {

						++callSites;

						exprType lhs = ((Attribute) call.func).value;

						InterfaceType duckType = duckTyper.inferDuckType(lhs,
								block, function, true);

						String name = ((NameTok) ((Attribute) call.func).attr).id;

						if (nameInRecoveredInterface(name, duckType)) {
							++callSitesPredictedCorrectly;
						}

					}

				}
			}
		}

		private boolean nameInRecoveredInterface(String name,
				InterfaceType duckType) {

			for (Feature feature : duckType) {
				if (feature instanceof NamedMethodFeature) {
					if (((NamedMethodFeature) feature).name().equals(name))
						return true;
				}
			}

			return false;
		}

		private Set<Call> calls(BasicBlock block) {

			final Set<Call> calls = new HashSet<Call>();

			new CallFinder(block, new EventHandler() {

				@Override
				public void foundCall(Call call) {
					calls.add(call);
				}
			});

			return calls;
		}
	}

	/**
	 * Load every module and package module in the hierarchy.
	 */
	private final class HierarchyLoader extends HierarchyWalker {

		@Override
		protected void visitSourceFile(SourceFile module) {
			try {
				if (!module.isSystem())
					model.loadModule(module.getFullyQualifiedName());
			} catch (ParseException e) {
				System.err.println("MISSED DATA WARNING: error while "
						+ "parsing module" + module.getFullyQualifiedName());
				System.err.println(e);
				return;
			} catch (IOException e) {
				System.err.println("MISSED DATA WARNING: error while "
						+ "finding module" + module.getFullyQualifiedName());
				System.err.println(e);
			}
		}

		@Override
		protected void visitPackage(Package pkg) {
			try {
				if (!pkg.isSystem())
					model.loadPackage(pkg.getFullyQualifiedName());
			} catch (ParseException e) {
				System.err.println("MISSED DATA WARNING: error while "
						+ "parsing package" + pkg.getFullyQualifiedName());
				System.err.println(e);
				return;
			} catch (IOException e) {
				System.err.println("MISSED DATA WARNING: error while "
						+ "finding package" + pkg.getFullyQualifiedName());
				System.err.println(e);
				return;
			}
		}
	}
}
