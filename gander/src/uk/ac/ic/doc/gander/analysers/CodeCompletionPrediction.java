package uk.ac.ic.doc.gander.analysers;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.python.pydev.parser.jython.ParseException;
import org.python.pydev.parser.jython.ast.Attribute;
import org.python.pydev.parser.jython.ast.Call;
import org.python.pydev.parser.jython.ast.exprType;

import uk.ac.ic.doc.gander.analysis.CallFinder;
import uk.ac.ic.doc.gander.analysis.CallFinder.EventHandler;
import uk.ac.ic.doc.gander.cfg.BasicBlock;
import uk.ac.ic.doc.gander.duckinference.DuckTyper;
import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.flowinference.ZeroCfaTypeEngine;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.types.Type;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.hierarchy.HierarchyWalker;
import uk.ac.ic.doc.gander.hierarchy.Package;
import uk.ac.ic.doc.gander.hierarchy.SourceFile;
import uk.ac.ic.doc.gander.model.DefaultModel;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.ModelWalkerWithParent;
import uk.ac.ic.doc.gander.model.MutableModel;

/**
 * Study how often code completion would have included the methods called in the
 * program.
 */
public class CodeCompletionPrediction {

	private final DuckTyper duckTyper;
	private final MutableModel model;
	
	private long callSites = 0;
	private long callSitesPredictedCorrectly = 0;

	public CodeCompletionPrediction(Hierarchy hierarchy, File projectRoot) throws Exception {
		System.out.println("Creating model from hierarchy");
		this.model = new DefaultModel(hierarchy);
		System.out.println("Loading all non-system modules in hierarchy");
		new HierarchyLoader().walk(hierarchy);

		System.out.println("Performing flow-based type inference");

		final TypeResolver typer = new TypeResolver(new ZeroCfaTypeEngine());
		this.duckTyper = new DuckTyper(model, typer);

		System.out.println("Running signature analysis");
		new ModelDucker().walk(model);
	}

	public double result() {
		return callSitesPredictedCorrectly / callSites;
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

						exprType lhs = ((Attribute) call.func).value;

						Result<Type> duckType = duckTyper.typeOf(lhs, block, function, true);
					}

				}
			}
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
