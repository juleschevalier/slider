package fr.ujm.tse.lt2c.satin.reasoner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.mongodb.MongoClient;

import fr.ujm.tse.lt2c.satin.dictionary.DictionaryRWLock;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionary;
import fr.ujm.tse.lt2c.satin.interfaces.Parser;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.rules.AbstractRule;
import fr.ujm.tse.lt2c.satin.rules.mark1.Mark1CAX_SCO;
import fr.ujm.tse.lt2c.satin.rules.mark1.Mark1PRP_DOM;
import fr.ujm.tse.lt2c.satin.rules.mark1.Mark1PRP_RNG;
import fr.ujm.tse.lt2c.satin.rules.mark1.Mark1PRP_SPO1;
import fr.ujm.tse.lt2c.satin.rules.mark1.Mark1SCM_DOM1;
import fr.ujm.tse.lt2c.satin.rules.mark1.Mark1SCM_DOM2;
import fr.ujm.tse.lt2c.satin.rules.mark1.Mark1SCM_EQC2;
import fr.ujm.tse.lt2c.satin.rules.mark1.Mark1SCM_EQP2;
import fr.ujm.tse.lt2c.satin.rules.mark1.Mark1SCM_RNG1;
import fr.ujm.tse.lt2c.satin.rules.mark1.Mark1SCM_RNG2;
import fr.ujm.tse.lt2c.satin.rules.mark1.Mark1SCM_SCO;
import fr.ujm.tse.lt2c.satin.rules.mark1.Mark1SCM_SPO;
import fr.ujm.tse.lt2c.satin.tools.Comparator;
import fr.ujm.tse.lt2c.satin.tools.ParserImplNaive;
import fr.ujm.tse.lt2c.satin.tools.RunEntity;
import fr.ujm.tse.lt2c.satin.triplestore.TemporaryVerticalPartioningTripleStoreRWLock;
import fr.ujm.tse.lt2c.satin.triplestore.VerticalPartioningTripleStoreRWLock;

public class ReasonnerVerticalMTRWLock {

	public static CountDownLatch cdlWriter;
	private static Logger logger = Logger.getLogger(ReasonnerVerticalMTRWLock.class);
	private static ExecutorService executor;

	private static boolean PERSIST_RESULTS = true;
	
	private static int SESSION_ID = UUID.randomUUID().hashCode();

	public static void main(String[] args) {

		try {

			for (int i = 0; i < 1; i++) {

				// infere("subclassof.owl");
				infere("sample1.owl");
				// infere("univ-bench.owl");
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

			
			System.out.println("SESSION ID : "+SESSION_ID);
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
		Dictionary dictionary = new DictionaryRWLock();
		Parser parser = new ParserImplNaive(dictionary, tripleStore);

		ArrayList<AbstractRule> rules = new ArrayList<>();
		TemporaryVerticalPartioningTripleStoreRWLock usableTriples = new TemporaryVerticalPartioningTripleStoreRWLock();
		Set<Triple> newTriples = Collections.newSetFromMap(new ConcurrentHashMap<Triple, Boolean>());

		/* File parsing */
		parser.parse(input);

		long beginNbTriples = tripleStore.size();

		/* Initialize rules used for inference on RhoDF */

		CountDownLatch doneSignal = null;

		rules.add(new Mark1CAX_SCO(dictionary, usableTriples, newTriples, tripleStore, doneSignal));
		rules.add(new Mark1PRP_DOM(dictionary, usableTriples, newTriples, tripleStore, doneSignal));
		rules.add(new Mark1PRP_RNG(dictionary, usableTriples, newTriples, tripleStore, doneSignal));
		rules.add(new Mark1PRP_SPO1(dictionary, usableTriples, newTriples, tripleStore, doneSignal));
		rules.add(new Mark1SCM_SCO(dictionary, usableTriples, newTriples, tripleStore, doneSignal));
		rules.add(new Mark1SCM_EQC2(dictionary, usableTriples, newTriples, tripleStore, doneSignal));
		rules.add(new Mark1SCM_SPO(dictionary, usableTriples, newTriples, tripleStore, doneSignal));
		rules.add(new Mark1SCM_EQP2(dictionary, usableTriples, newTriples, tripleStore, doneSignal));
		rules.add(new Mark1SCM_DOM1(dictionary, usableTriples, newTriples, tripleStore, doneSignal));
		rules.add(new Mark1SCM_DOM2(dictionary, usableTriples, newTriples, tripleStore, doneSignal));
		rules.add(new Mark1SCM_RNG1(dictionary, usableTriples, newTriples, tripleStore, doneSignal));
		rules.add(new Mark1SCM_RNG2(dictionary, usableTriples, newTriples, tripleStore, doneSignal));

		doneSignal = new CountDownLatch(rules.size());
		cdlWriter = new CountDownLatch(rules.size());

		for (AbstractRule r : rules)
			r.setDoneSignal(doneSignal);

		long old_size;
		long new_size;
		long steps = 0;

		executor = Executors.newFixedThreadPool(rules.size());

		long parsingTime = System.nanoTime();

		/* Run each rule until there is no more new triple inferred */
		do {

			old_size = tripleStore.size();
			long stepTime = System.nanoTime();
			logger.debug("--------------------STEP " + steps + "--------------------");

			for (Rule rule : rules) {
				executor.submit(rule);
			}

			// Wait all rules to finish
			try {

				// for (AbstractRule r : rules) {
				// System.out.println(r.getRuleName() + " " + r.isFinished());
				// }
				// System.out.println(doneSignal.getCount());
				doneSignal.await();
				// System.out.println("######################################");
				// for (AbstractRule r : rules) {
				// System.out.println(r.getRuleName() + " " + r.isFinished());
				// }
				// System.out.println("Fire in the hole");
				// System.out.println("***************************************");
				doneSignal = new CountDownLatch(rules.size());
				cdlWriter = new CountDownLatch(rules.size());
				for (AbstractRule r : rules) {
					r.setDoneSignal(doneSignal);
					r.setFinished(false);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			/* Step end - Replace usableTriples by newTriples */
			logger.debug("End of iteration - Latch : " + doneSignal.getCount());
			usableTriples.clear();
			usableTriples.addAll(newTriples);
			newTriples.clear();

			logger.debug("Usable triples: " + usableTriples.size());

			new_size = tripleStore.size();
			long step2Time = System.nanoTime();
			logger.debug((step2Time - stepTime) + "ns for " + (new_size - old_size) + " triples");
			steps++;
			for (Triple triple : usableTriples.getAll()) {
				logger.trace(dictionary.printTriple(triple));
			}
		} while (!usableTriples.isEmpty());

		long endTime = System.nanoTime();

		System.out.print("Inference Done");

		/* Print inferred triples to file */
		tripleStore.writeToFile("Inferred" + (tripleStore.size() - beginNbTriples) + input + ".out", dictionary);

		/*
		 * RESULTS PERSISTANCE
		 */
		if (PERSIST_RESULTS) {
			MongoClient client;
			try {
				client = new MongoClient();
				Morphia morphia = new Morphia();
				morphia.map(RunEntity.class);
				Datastore ds = morphia.createDatastore(client, "RunResults");

				HashMap<Integer, List<String>> triple_in_much = Comparator.compare("jena_" + input, dictionary, tripleStore);

				List<String> missing_triples = triple_in_much.get(0);
				List<String> too_triples = triple_in_much.get(1);

				RunEntity runEntity = new RunEntity(input, SESSION_ID, steps, (endTime - parsingTime), beginNbTriples, (tripleStore.size() - beginNbTriples), missing_triples, too_triples);
				ds.save(runEntity);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		System.out.println("Saving OK");

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
