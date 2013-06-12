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

import uk.ac.ic.doc.gander.Feature;
import uk.ac.ic.doc.gander.analysis.CallFinder;
import uk.ac.ic.doc.gander.analysis.CallFinder.EventHandler;
import uk.ac.ic.doc.gander.cfg.BasicBlock;
import uk.ac.ic.doc.gander.concretetype.ConcreteType;
import uk.ac.ic.doc.gander.concretetype.ConcreteTypeSystem;
import uk.ac.ic.doc.gander.contraindication.Contraindication;
import uk.ac.ic.doc.gander.ducktype.InterfaceRecovery;
import uk.ac.ic.doc.gander.ducktype.NamedMethodFeature;
import uk.ac.ic.doc.gander.flowinference.TypeResolver;
import uk.ac.ic.doc.gander.flowinference.ZeroCfaTypeEngine;
import uk.ac.ic.doc.gander.flowinference.abstractmachine.PyObject;
import uk.ac.ic.doc.gander.flowinference.result.Result;
import uk.ac.ic.doc.gander.flowinference.result.Result.Transformer;
import uk.ac.ic.doc.gander.hierarchy.Hierarchy;
import uk.ac.ic.doc.gander.hierarchy.HierarchyWalker;
import uk.ac.ic.doc.gander.hierarchy.Package;
import uk.ac.ic.doc.gander.hierarchy.SourceFile;
import uk.ac.ic.doc.gander.implementation.Implementation;
import uk.ac.ic.doc.gander.interfacetype.InterfaceType;
import uk.ac.ic.doc.gander.model.DefaultModel;
import uk.ac.ic.doc.gander.model.Function;
import uk.ac.ic.doc.gander.model.ModelSite;
import uk.ac.ic.doc.gander.model.ModelWalkerWithParent;
import uk.ac.ic.doc.gander.model.MutableModel;

/**
 * Study how often code completion would have included the methods called in the
 * program.
 */
public class CodeCompletionPrediction {

	private InterfaceRecovery duckTyper;
	private ConcreteTypeSystem flowTyper;
	private ConcreteTypeSystem contraindicationTyper;

	private final MutableModel model;

	private long callSites = 0;
	private long predictedCorrectlyByInterface = 0;
	private long predictedCorrectlyByFlow = 0;
	private long predictedCorrectlyByContraindication = 0;

	public CodeCompletionPrediction(Hierarchy hierarchy, File projectRoot)
			throws Exception {
		System.out.println("Creating model from hierarchy");
		this.model = new DefaultModel(hierarchy);
		System.out.println("Loading all non-system modules in hierarchy");
		new HierarchyLoader().walk(hierarchy);

		System.out.println("Performing flow-based type inference");

		ZeroCfaTypeEngine cfaTyper = new ZeroCfaTypeEngine();
		TypeResolver resolver = new TypeResolver(cfaTyper);
		this.flowTyper = new FlowTyperToConcreteType(cfaTyper, resolver);
		this.duckTyper = new InterfaceRecovery(resolver, true);
		this.contraindicationTyper = new Contraindication(model, flowTyper,
				duckTyper, resolver);

		System.out.println("Running signature analysis");
		new ModelDucker().walk(model);
	}

	public float interfaceResult() {
		if (callSites == 0)
			return 0F;
		else
			return predictedCorrectlyByInterface * 100F / callSites;
	}

	public float flowResult() {
		if (callSites == 0)
			return 0F;
		else
			return predictedCorrectlyByFlow * 100F / callSites;
	}

	public float contraindicationResult() {
		if (callSites == 0)
			return 0F;
		else
			return predictedCorrectlyByContraindication * 100F / callSites;
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

						String name = ((NameTok) ((Attribute) call.func).attr).id;
						ModelSite<exprType> lhs = new ModelSite<exprType>(
								((Attribute) call.func).value,
								function.codeObject());

						ConcreteType flowType = flowTyper.typeOf(lhs, block);
						if (nameGuaranteedByConcreteType(name, flowType)) {
							++predictedCorrectlyByFlow;
						}

						ConcreteType contraType = contraindicationTyper.typeOf(
								lhs, block);
						if (nameGuaranteedByConcreteType(name, contraType)) {
							++predictedCorrectlyByContraindication;
						}

						InterfaceType duckType = duckTyper.typeOf(lhs, block);
						if (nameInRecoveredInterface(name, duckType)) {
							++predictedCorrectlyByInterface;
						}

					}

				}
			}
		}

		private boolean nameGuaranteedByConcreteType(final String name,
				ConcreteType flowType) {

			return flowType
					.transformResult(new Transformer<Implementation, Boolean>() {

						@Override
						public Boolean transformFiniteResult(
								Set<Implementation> result) {
							for (Implementation impl : result) {
								if (!impl.definesSupportFor(new NamedMethodFeature(
										name))) {
									return false;
								}
							}

							return true;
						}

						@Override
						public Boolean transformInfiniteResult() {
							return false;
						}
					});
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
