package fr.ujm.tse.lt2c.satin.reasoner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.buffer.TripleManager;
import fr.ujm.tse.lt2c.satin.dictionary.DictionaryPrimitrivesRWLock;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Parser;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.rules.Rule;
import fr.ujm.tse.lt2c.satin.rules.run.RunCAX_SCO;
import fr.ujm.tse.lt2c.satin.rules.run.RunPRP_DOM;
import fr.ujm.tse.lt2c.satin.rules.run.RunPRP_RNG;
import fr.ujm.tse.lt2c.satin.rules.run.RunPRP_SPO1;
import fr.ujm.tse.lt2c.satin.rules.run.RunSCM_DOM1;
import fr.ujm.tse.lt2c.satin.rules.run.RunSCM_DOM2;
import fr.ujm.tse.lt2c.satin.rules.run.RunSCM_EQC2;
import fr.ujm.tse.lt2c.satin.rules.run.RunSCM_EQP2;
import fr.ujm.tse.lt2c.satin.rules.run.RunSCM_RNG1;
import fr.ujm.tse.lt2c.satin.rules.run.RunSCM_RNG2;
import fr.ujm.tse.lt2c.satin.rules.run.RunSCM_SCO;
import fr.ujm.tse.lt2c.satin.rules.run.RunSCM_SPO;
import fr.ujm.tse.lt2c.satin.tools.Comparator;
import fr.ujm.tse.lt2c.satin.tools.ParserImplNaive;
import fr.ujm.tse.lt2c.satin.triplestore.VerticalPartioningTripleStoreRWLock;

public class ReasonnerStreamed {

	public static CountDownLatch cdlWriter;
	private static Logger logger = Logger.getLogger(ReasonnerStreamed.class);
	private static ExecutorService executor;

	public static AtomicInteger DEBUG_nb_duplicates;
	public static AtomicInteger runningThreads;
	public static int availables_cores = Runtime.getRuntime().availableProcessors();
	public static int threads_per_core = 10;
	public static int max_threads = availables_cores * threads_per_core;
	// public static int max_threads = 1;

	public static Phaser phaser;

	// private static boolean PERSIST_RESULTS = true;

	private static int SESSION_ID = UUID.randomUUID().hashCode();

	public static void main(String[] args) {

		List<String> files = new ArrayList<String>();

		// files.add("tiny_subclassof.owl");
		// files.add("subclassof.owl");
		// files.add("sample1.owl");
		// files.add("univ-bench.owl");
		// files.add("sweetAll.owl");
		// files.add("wine.rdf");
		 files.add("geopolitical_200Ko.owl");
		// files.add("geopolitical_300Ko.owl");
		// files.add("geopolitical_500Ko.owl");
		// files.add("geopolitical_1Mo.owl");
		// files.add("geopolitical.owl");
		// files.add("efo.owl");
		// files.add("opencyc.owl");

		try {
			for (int file = 0; file < files.size(); file++) {

				for (int i = 0; i < 10; i++) {

					infere(files.get(file));
				}
			}

			System.out.println("SESSION ID : " + SESSION_ID);
			shutdownAndAwaitTermination(executor);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static void infere(String input) {

		logger.debug("***************************************************************************************************************************************************************");
		logger.debug("****************************************************************************NEW RUN****************************************************************************");
		logger.debug("***************************************************************************************************************************************************************");
		// System.out.println("Infere : " + input);

		/* Initialise Structures */
		TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
		Dictionary dictionary = new DictionaryPrimitrivesRWLock();
		TripleManager tripleManager = new TripleManager();
		Parser parser = new ParserImplNaive(dictionary, tripleStore);

		phaser = new Phaser(1) {
			protected boolean onAdvance(int phase, int registeredParties) {
				return phase >= 1 || registeredParties == 0;
			}
		};

		/* Init counters */
		DEBUG_nb_duplicates = new AtomicInteger();
		runningThreads = new AtomicInteger();

		/* File parsing */
		parser.parse(input);

		long DEBUG_beginNbTriples = tripleStore.size();

		/* Initialize rules used for inference on RhoDF */

		CountDownLatch doneSignal = null;
		executor = Executors.newFixedThreadPool(max_threads);
		// executor = Executors.newFixedThreadPool(availables_cores *
		// threads_per_core);

		tripleManager.addRule(new Rule(new RunCAX_SCO(dictionary, tripleStore, doneSignal), executor));
		tripleManager.addRule(new Rule(new RunPRP_DOM(dictionary, tripleStore, doneSignal), executor));
		tripleManager.addRule(new Rule(new RunPRP_RNG(dictionary, tripleStore, doneSignal), executor));
		tripleManager.addRule(new Rule(new RunPRP_SPO1(dictionary, tripleStore, doneSignal), executor));
		tripleManager.addRule(new Rule(new RunSCM_SCO(dictionary, tripleStore, doneSignal), executor));
		tripleManager.addRule(new Rule(new RunSCM_EQC2(dictionary, tripleStore, doneSignal), executor));
		tripleManager.addRule(new Rule(new RunSCM_SPO(dictionary, tripleStore, doneSignal), executor));
		tripleManager.addRule(new Rule(new RunSCM_EQP2(dictionary, tripleStore, doneSignal), executor));
		tripleManager.addRule(new Rule(new RunSCM_DOM1(dictionary, tripleStore, doneSignal), executor));
		tripleManager.addRule(new Rule(new RunSCM_DOM2(dictionary, tripleStore, doneSignal), executor));
		tripleManager.addRule(new Rule(new RunSCM_RNG1(dictionary, tripleStore, doneSignal), executor));
		tripleManager.addRule(new Rule(new RunSCM_RNG2(dictionary, tripleStore, doneSignal), executor));

		if (logger.isDebugEnabled())
			logger.debug(tripleManager.getRules().size() + " Rules initialized\n");

		if (logger.isTraceEnabled()) {
			for (Rule rule : tripleManager.getRules()) {
				logger.trace(rule.getTripleDistributor().subscribers(rule.name(), dictionary));
			}
		}

		// doneSignal = new CountDownLatch(rules.size());
		// cdlWriter = new CountDownLatch(rules.size());

		// for (Rule r : rules)
		// r.getRun().setDoneSignal(doneSignal);

		logger.debug("***************************************************************************************************************************************************************");
		logger.debug("************************************************************************START INFERENCE************************************************************************");
		logger.debug("***************************************************************************************************************************************************************");

		// System.out.println("Infere : " + input);

		long DEBUG_startTime = System.nanoTime();

		// phaser.register();

		/************************
		 * LAUNCH INFERENCE *
		 ************************/
		tripleManager.addTriples(tripleStore.getAll());

		/*
		 * Notify the triple manager that we don't have more triples and wait
		 * the end
		 */

		long still = tripleManager.finishThem();
		if (logger.isTraceEnabled())
			logger.trace("REASONER There are still " + still + " triples");
		while (still != 0) {
			// try {
			// Thread.sleep(1000);
			// } catch (InterruptedException e) {
			// e.printStackTrace();
			// }
			if (logger.isTraceEnabled())
				logger.trace("REASONNER Finish Them!");
			phaser.arriveAndAwaitAdvance();
			still = tripleManager.finishThem();
			if (logger.isTraceEnabled())
				logger.trace("REASONER There are still " + still + " non empty buffers");
		}

		if (logger.isDebugEnabled())
			logger.debug("REASONNER FAtality!");

		executor.shutdown();
		try {
			executor.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		/*
		 * END
		 */

		logger.debug("Reasoning is finished");

		for (Rule rule : tripleManager.getRules()) {
			logger.debug(rule.getRun().getRuleName() + " launched " + rule.getRun().getThreads() + " times");
		}

		long DEBUG_endTime = System.nanoTime();

		System.out.println(input + ": " + DEBUG_beginNbTriples + " -> " + (tripleStore.size() - DEBUG_beginNbTriples) + " in " + (TimeUnit.MILLISECONDS.convert(DEBUG_endTime - DEBUG_startTime, TimeUnit.NANOSECONDS)) + " ms");

		HashMap<Integer, List<String>> triple_in_much = Comparator.compare("jena_" + input, dictionary, tripleStore);

		List<String> missing_triples = triple_in_much.get(0);
		List<String> too_triples = triple_in_much.get(1);

		if (missing_triples.size() + too_triples.size() == 0) {
			System.out.println("Results match");
		} else {
			System.out.println("-" + missing_triples.size() + " +" + too_triples.size());
		}

		for (String string : missing_triples) {
			System.out.println("- " + string);
		}
		for (String string : too_triples) {
			System.out.println("+ " + string);
		}

		/*************************
		 * MUST SAVE RUN RESULTS *
		 *************************/

	}

	static void shutdownAndAwaitTermination(ExecutorService pool) {
		System.exit(-1);
		pool.shutdown(); // Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!pool.awaitTermination(50, TimeUnit.MILLISECONDS)) {
				pool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!pool.awaitTermination(50, TimeUnit.MILLISECONDS))
					System.err.println("Pool did not terminate");
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			System.out.println("piece of");
			pool.shutdownNow();

			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}
}
