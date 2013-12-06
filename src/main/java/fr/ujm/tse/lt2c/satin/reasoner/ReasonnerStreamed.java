package fr.ujm.tse.lt2c.satin.reasoner;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import fr.ujm.tse.lt2c.satin.tools.ParserImplNaive;
import fr.ujm.tse.lt2c.satin.triplestore.VerticalPartioningTripleStoreRWLock;

public class ReasonnerStreamed {

	public static CountDownLatch cdlWriter;
	private static Logger logger = Logger.getLogger(ReasonnerStreamed.class);
	private static ExecutorService executor;

	public static AtomicInteger nb_duplicates;
	public static AtomicInteger runningThreads;

	// private static boolean PERSIST_RESULTS = true;

	private static int SESSION_ID = UUID.randomUUID().hashCode();

	public static void main(String[] args) {

		try {

			for (int i = 0; i < 1; i++) {

				// infere("subclassof.owl");
				// infere("sample1.owl");
				infere("univ-bench.owl");
				// infere("sweetAll.owl");
				// infere("wine.rdf");
				// infere("geopolitical_200Ko.owl");
				// infere("geopolitical_300Ko.owl");
				// infere("geopolitical_500Ko.owl");
				// infere("geopolitical_1Mo.owl");
				// infere("geopolitical.owl");
				// infere("efo.owl");
				// infere("opencyc.owl");
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
		System.out.println("Infere : " + input);

		/* Initialise Structures */
		TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
		Dictionary dictionary = new DictionaryPrimitrivesRWLock();
		TripleManager tripleManager = new TripleManager();
		Parser parser = new ParserImplNaive(dictionary, tripleStore);

		/* Init counters */
		nb_duplicates = new AtomicInteger();
		runningThreads = new AtomicInteger();

		/* File parsing */
		parser.parse(input);

		long beginNbTriples = tripleStore.size();

		/* Initialize rules used for inference on RhoDF */

		CountDownLatch doneSignal = null;
		executor = Executors.newFixedThreadPool(12);

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

		// doneSignal = new CountDownLatch(rules.size());
		// cdlWriter = new CountDownLatch(rules.size());

		// for (Rule r : rules)
		// r.getRun().setDoneSignal(doneSignal);

		System.out.println(tripleStore.size());

		tripleManager.addTriples(tripleStore.getAll());

		long occupation = 0;
		for (Rule rule : tripleManager.getRules()) {
			occupation += rule.getTripleBuffer().mainBufferOccupation() + rule.getTripleBuffer().secondaryBufferOccupation();
			System.out.println(rule.name() + " occupation : " + rule.getTripleBuffer().mainBufferOccupation() + " " + rule.getTripleBuffer().secondaryBufferOccupation());
			System.out.println(rule.name() + " subscribers : " + rule.getTripleDistributor().subcribers());
		}
		System.out.println("Occupation :" + occupation);

		tripleManager.finishThem();

		while (ReasonnerStreamed.runningThreads.get() > 0) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("Running :" + ReasonnerStreamed.runningThreads.get());
		}

		occupation = 0;
		for (Rule rule : tripleManager.getRules()) {
			occupation += rule.getTripleBuffer().mainBufferOccupation() + rule.getTripleBuffer().secondaryBufferOccupation();
			System.out.println(rule.name() + " " + rule.getTripleBuffer().mainBufferOccupation() + " " + rule.getTripleBuffer().secondaryBufferOccupation());
		}
		System.out.println("Occupation :" + occupation);

		while (occupation != 0) {
			System.out.println(occupation);

			tripleManager.finishThem();

			while (ReasonnerStreamed.runningThreads.get() > 0) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("Running :" + ReasonnerStreamed.runningThreads.get());
			}

			occupation = 0;
			for (Rule rule : tripleManager.getRules()) {
				occupation += rule.getTripleBuffer().mainBufferOccupation() + rule.getTripleBuffer().secondaryBufferOccupation();
				System.out.println(rule.name() + " +" + rule.getTripleBuffer().mainBufferOccupation() + rule.getTripleBuffer().secondaryBufferOccupation());
			}
		}

		executor.shutdown();
		try {
			executor.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Inferred : " + (tripleStore.size() - beginNbTriples));

		/**
		 * 
		 * *******************OLD VERSION**********************
		 * 
		 */
		//
		//
		//
		// long old_size;
		// long new_size;
		// long steps = 0;
		//
		//
		// long parsingTime = System.nanoTime();
		//
		// /* Run each rule until there is no more new triple inferred */
		// do {
		//
		// old_size = tripleStore.size();
		// long stepTime = System.nanoTime();
		// logger.debug("--------------------STEP " + steps +
		// "--------------------");
		//
		// for (RuleRun ruleRun : rules) {
		// executor.submit(ruleRun);
		// }
		//
		// // Wait all rules to finish
		// try {
		//
		// // for (AbstractRule r : rules) {
		// // System.out.println(r.getRuleName() + " " + r.isFinished());
		// // }
		// // System.out.println(doneSignal.getCount());
		// doneSignal.await();
		// // System.out.println("######################################");
		// // for (AbstractRule r : rules) {
		// // System.out.println(r.getRuleName() + " " + r.isFinished());
		// // }
		// // System.out.println("Fire in the hole");
		// // System.out.println("***************************************");
		// doneSignal = new CountDownLatch(rules.size());
		// cdlWriter = new CountDownLatch(rules.size());
		// for (AbstractRun r : rules) {
		// r.setDoneSignal(doneSignal);
		// r.setFinished(false);
		// }
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		//
		// /* Step end - Replace usableTriples by newTriples */
		// logger.debug("End of iteration - Latch : " + doneSignal.getCount());
		// usableTriples.clear();
		// usableTriples.addAll(newTriples);
		// newTriples.clear();
		//
		// logger.debug("Usable triples: " + usableTriples.size());
		//
		// new_size = tripleStore.size();
		// long step2Time = System.nanoTime();
		// logger.debug((step2Time - stepTime) + "ns for " + (new_size -
		// old_size) + " triples");
		// steps++;
		// for (Triple triple : usableTriples.getAll()) {
		// logger.trace(dictionary.printTriple(triple));
		// }
		// } while (!usableTriples.isEmpty());
		//
		// long endTime = System.nanoTime();
		//
		// System.out.println("Inference Done");
		//
		// /* Print inferred triples to file */
		// // tripleStore.writeToFile("Inferred" + (tripleStore.size() -
		// // beginNbTriples) + input + ".out", dictionary);
		//
		// /*
		// * RESULTS PERSISTANCE
		// */
		// if (PERSIST_RESULTS) {
		// MongoClient client;
		// try {
		// client = new MongoClient();
		// Morphia morphia = new Morphia();
		// morphia.map(RunEntity.class);
		// Datastore ds = morphia.createDatastore(client, "RunResults");
		//
		// HashMap<Integer, List<String>> triple_in_much =
		// Comparator.compare("jena_" + input, dictionary, tripleStore);
		//
		// List<String> missing_triples = triple_in_much.get(0);
		// List<String> too_triples = triple_in_much.get(1);
		//
		// RunEntity runEntity = new RunEntity(input, SESSION_ID, steps,
		// nb_duplicates.get(), (endTime - parsingTime), beginNbTriples,
		// (tripleStore.size() - beginNbTriples), missing_triples, too_triples);
		// ds.save(runEntity);
		//
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }
		//
	}

	static void shutdownAndAwaitTermination(ExecutorService pool) {
		// System.out.println("finishing");
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
