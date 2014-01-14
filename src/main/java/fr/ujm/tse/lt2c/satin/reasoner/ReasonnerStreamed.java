package fr.ujm.tse.lt2c.satin.reasoner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

	private ReasonnerStreamed() {
	}

	public static CountDownLatch cdlWriter;
	private static Logger logger = Logger.getLogger(ReasonnerStreamed.class);
	private static ExecutorService executor;

	public static AtomicInteger debugNbDuplicates;
	public static AtomicInteger runningThreads;
	public static int availablesCores = Runtime.getRuntime().availableProcessors();
	public static int threadsPerCore = 10;
	public static int maxThreads = availablesCores * threadsPerCore;
	// public static int max_threads = 1;

	public static Phaser phaser;

	private static boolean PERSIST_RESULTS = false;

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
			if (logger.isInfoEnabled()) {
				logger.info("SESSION ID : " + SESSION_ID);
			}
			shutdownAndAwaitTermination(executor);

		} catch (Exception e) {
			logger.error("", e);
		}

	}

	private static void infere(String input) {

		logger.debug("***************************************************************************************************************************************************************");
		logger.debug("****************************************************************************NEW RUN****************************************************************************");
		logger.debug("***************************************************************************************************************************************************************");

		/* Initialise Structures */
		TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
		Dictionary dictionary = new DictionaryPrimitrivesRWLock();
		TripleManager tripleManager = new TripleManager();
		Parser parser = new ParserImplNaive(dictionary, tripleStore);

		phaser = new Phaser(1) {
			@Override
			protected boolean onAdvance(int phase, int registeredParties) {
				return phase >= 1 || registeredParties == 0;
			}
		};

		/* Init counters */
		debugNbDuplicates = new AtomicInteger();
		runningThreads = new AtomicInteger();

		/* File parsing */
		parser.parse(input);

		long debugBeginNbTriples = tripleStore.size();

		/* Initialize rules used for inference on RhoDF */

		CountDownLatch doneSignal = null;
		executor = Executors.newFixedThreadPool(maxThreads);

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

		if (logger.isDebugEnabled()) {
			logger.debug(tripleManager.getRules().size() + " Rules initialized\n");
		}

		if (logger.isTraceEnabled()) {
			for (Rule rule : tripleManager.getRules()) {
				logger.trace(rule.getTripleDistributor().subscribers(rule.name(), dictionary));
			}
		}

		logger.debug("***************************************************************************************************************************************************************");
		logger.debug("************************************************************************START INFERENCE************************************************************************");
		logger.debug("***************************************************************************************************************************************************************");

		long DEBUG_startTime = System.nanoTime();

		/************************
		 * LAUNCH INFERENCE *
		 ************************/
		tripleManager.addTriples(tripleStore.getAll());

		/*
		 * Notify the triple manager that we don't have more triples and wait
		 * the end
		 */

		long still = tripleManager.finishThem();
		if (logger.isTraceEnabled()) {
			logger.trace("REASONER There are still " + still + " triples");
		}
		while (still != 0) {
			if (logger.isTraceEnabled()) {
				logger.trace("REASONNER Finish Them!");
			}
			phaser.arriveAndAwaitAdvance();
			still = tripleManager.finishThem();
			if (logger.isTraceEnabled()) {
				logger.trace("REASONER There are still " + still + " non empty buffers");
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("REASONNER FAtality!");
		}

		executor.shutdown();
		try {
			executor.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			logger.error("", e);
		}
		/*
		 * END
		 */

		logger.debug("Reasoning is finished");

		for (Rule rule : tripleManager.getRules()) {
			logger.debug(rule.getRun().getRuleName() + " launched " + rule.getRun().getThreads() + " times");
		}

		long debugEndTime = System.nanoTime();

		if (logger.isInfoEnabled()) {
			logger.info("---------------------------------");
			logger.info(input + ": " + debugBeginNbTriples + " -> " + (tripleStore.size() - debugBeginNbTriples) + " in " + (TimeUnit.MILLISECONDS.convert(debugEndTime - DEBUG_startTime, TimeUnit.NANOSECONDS)) + " ms");
		}

		Map<Integer, List<String>> diffTriples = Comparator.compare("jena_" + input, dictionary, tripleStore);

		List<String> missingTriples = diffTriples.get(0);
		List<String> tooTriples = diffTriples.get(1);

		if (logger.isInfoEnabled()) {
			if (missingTriples.size() + tooTriples.size() == 0) {
				logger.info("Results match");
			} else {
				logger.info("-" + missingTriples.size() + " +" + tooTriples.size());
			}

			for (String string : missingTriples) {
				logger.info("- " + string);
			}
			for (String string : tooTriples) {
				logger.info("+ " + string);
			}
		}

		/*************************
		 * MUST SAVE RUN RESULTS *
		 *************************/

		if (PERSIST_RESULTS) {
			/* PERSIST */
		}

	}

	static void shutdownAndAwaitTermination(ExecutorService pool) {
		pool.shutdown(); // Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!pool.awaitTermination(50, TimeUnit.MILLISECONDS)) {
				pool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!pool.awaitTermination(50, TimeUnit.MILLISECONDS)) {
					logger.error("Pool did not terminate");
				}
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			logger.error("", ie);
			pool.shutdownNow();

			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}
}
