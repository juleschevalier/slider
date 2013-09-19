package fr.ujm.tse.lt2c.satin.reasoner;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVWriter;
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
import fr.ujm.tse.lt2c.satin.triplestore.TemporaryVerticalPartioningTripleStoreRWLock;
import fr.ujm.tse.lt2c.satin.triplestore.VerticalPartioningTripleStoreRWLock;

public class ReasonnerVerticalMTRWLock {

	public static CountDownLatch cdlWriter;
	private static Logger logger = Logger
			.getLogger(ReasonnerVerticalMTRWLock.class);
	private static ExecutorService executor;

	public static void main(String[] args) {

		CSVWriter writer = null;
		try {
			writer = new CSVWriter(new FileWriter("resultsMTLock.csv"), ';');

			String[] headers = { "File", "i", "Initial triples",
					"Infered triples", "Loops", "Time", "Left over" };
			writer.writeNext(headers);

			for (int i = 0; i < 100; i++) {

				// System.out.println("subclassof.owl 5618 bits");
				// infere("subclassof.owl", i, writer);
				// System.out.println();
				//
				 System.out.println("sample1.owl 9714 bits");
				 infere("sample1.owl", i, writer);
				 System.out.println();
				//
				// System.out.println("univ-bench.owl 13840 bits");
				// infere("univ-bench.owl", i, writer);
				// System.out.println();
				//
				// System.out.println("sweetAll.owl 17538 bits");
				// infere("sweetAll.owl", i, writer);
				// System.out.println();
				//
				// System.out.println("wine.rdf 78225 bits");
				// infere("wine.rdf", i, writer);
				// System.out.println();
				//
				// System.out.println("geopolitical_200Ko.owl 199105 bits");
				// infere("geopolitical_200Ko.owl", i, writer);
				// System.out.println();
				//
				// System.out.println("geopolitical_300Ko.owl 306377 bits");
				// infere("geopolitical_300Ko.owl",i,writer);
				// System.out.println();
				//
				// System.out.println("geopolitical_500Ko.owl 497095 bits");
				// infere("geopolitical_500Ko.owl",i,writer);
				// System.out.println();
				//
				// System.out.println("geopolitical_1Mo.owl 1047485 bits");
				// infere("geopolitical_1Mo.owl",i,writer);
				// System.out.println();
				//
				// System.out.println("geopolitical.owl 1780714 bits");
				// infere("geopolitical.owl",i,writer);
				// System.out.println();
				//
				// System.out.println("efo.owl 26095973 bits");
				// infere("efo.owl",i,writer);
				// System.out.println();
				//
				// System.out.println("opencyc.owl 252122090 bits");
				// infere("opencyc.owl",i,writer);
				// System.out.println();
				//
			}

			writer.close();

			// Runtime.getRuntime().exec(
			// "text2speech \"The computation is over.\"");

			shutdownAndAwaitTermination(executor);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private static void infere(String input, int i, CSVWriter writer) {

		logger.debug("***************************************************************************************************************************************************************");
		logger.debug("****************************************************************************NEW RUN****************************************************************************");
		logger.debug("***************************************************************************************************************************************************************");

		TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
		// Dictionary dictionary = new DictionaryImplNaive();
		Dictionary dictionary = new DictionaryRWLock();
		Parser parser = new ParserImplNaive(dictionary, tripleStore);

		// long startTime = System.nanoTime();

		parser.parse(input);

		// logger.debug("Parsing completed");

		long parsingTime = System.nanoTime();

		long beginNbTriples = tripleStore.size();

		ArrayList<AbstractRule> rules = new ArrayList<>();
		TemporaryVerticalPartioningTripleStoreRWLock usableTriples = new TemporaryVerticalPartioningTripleStoreRWLock();
		Set<Triple> newTriples = Collections
				.newSetFromMap(new ConcurrentHashMap<Triple, Boolean>());

		CountDownLatch doneSignal = null;

		/* Initialize rules used for inference on RhoDF */

		rules.add(new Mark1CAX_SCO(dictionary, usableTriples, newTriples,
				tripleStore, doneSignal));
		rules.add(new Mark1PRP_DOM(dictionary, usableTriples, newTriples,
				tripleStore, doneSignal));
		rules.add(new Mark1PRP_RNG(dictionary, usableTriples, newTriples,
				tripleStore, doneSignal));
		rules.add(new Mark1PRP_SPO1(dictionary, usableTriples, newTriples,
				tripleStore, doneSignal));
		rules.add(new Mark1SCM_SCO(dictionary, usableTriples, newTriples,
				tripleStore, doneSignal));
		rules.add(new Mark1SCM_EQC2(dictionary, usableTriples, newTriples,
				tripleStore, doneSignal));
		rules.add(new Mark1SCM_SPO(dictionary, usableTriples, newTriples,
				tripleStore, doneSignal));
		rules.add(new Mark1SCM_EQP2(dictionary, usableTriples, newTriples,
				tripleStore, doneSignal));
		rules.add(new Mark1SCM_DOM1(dictionary, usableTriples, newTriples,
				tripleStore, doneSignal));
		rules.add(new Mark1SCM_DOM2(dictionary, usableTriples, newTriples,
				tripleStore, doneSignal));
		rules.add(new Mark1SCM_RNG1(dictionary, usableTriples, newTriples,
				tripleStore, doneSignal));
		rules.add(new Mark1SCM_RNG2(dictionary, usableTriples, newTriples,
				tripleStore, doneSignal));

		doneSignal = new CountDownLatch(rules.size());
		cdlWriter = new CountDownLatch(rules.size());

		for (AbstractRule r : rules)
			r.setDoneSignal(doneSignal);

		long old_size;
		long new_size;
		int steps = 0;

		executor = Executors.newFixedThreadPool(rules.size());

		do {
			old_size = tripleStore.size();
			long stepTime = System.nanoTime();
			logger.debug("--------------------STEP " + steps
					+ "--------------------" + doneSignal.getCount());

			for (Rule rule : rules) {
				executor.submit(rule);
			}
			// Wait all rules to finish
			// System.out.println("Waiting for latch");
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
			logger.debug("End of iteration - Latch : " + doneSignal.getCount());
			usableTriples.clear();
			usableTriples.addAll(newTriples);
			newTriples.clear();

			logger.debug("Usable triples: " + usableTriples.size());

			new_size = tripleStore.size();
			long step2Time = System.nanoTime();
			logger.debug((step2Time - stepTime) + "ns for "
					+ (new_size - old_size) + " triples");
			steps++;
			for (Triple triple : usableTriples.getAll()) {
				logger.trace(dictionary.printTriple(triple));
			}
		} while (!usableTriples.isEmpty());

		long endTime = System.nanoTime();

		long size = Comparator
				.compare("jena_" + input, dictionary, tripleStore);

		// System.out.println("Dictionary size: "+dictionary.size());
		// System.out.println("Initial triples: "+beginNbTriples);
		// System.out.println("Triples after inference: "+tripleStore.size());
		System.out.println("Generated triples: "
				+ (tripleStore.size() - beginNbTriples));
		// System.out.println("Iterations: "+steps);
		// System.out.println("Parsing: "+(parsingTime-startTime)/1000000.0+"ns");
		// System.out.println("Inference: "+(endTime-parsingTime)/1000000.0+"ns");
		// System.out.println("Total time: "+(endTime-startTime)/1000000.0+"ns");
		// System.out.print("File writing: ");
		tripleStore.writeToFile("Inferred"
				+ (tripleStore.size() - beginNbTriples) + input + ".out",
				dictionary);
		// System.out.println("ok");

		// String[] headers =
		// {"File","Size","i","Initial triples","Infered triples","Loops","Time","Correctness"};
		String[] datas = { input, "" + i, "" + beginNbTriples,
				"" + (tripleStore.size() - beginNbTriples), "" + steps,
				"" + (endTime - parsingTime), "" + size };
		writer.writeNext(datas);

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
