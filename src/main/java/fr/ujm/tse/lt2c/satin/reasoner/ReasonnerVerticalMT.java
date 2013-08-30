package fr.ujm.tse.lt2c.satin.reasoner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.dictionnary.DictionnaryImplNaive;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Parser;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.rules.mark1mt.Mark1mtCAX_SCO;
import fr.ujm.tse.lt2c.satin.rules.mark1mt.Mark1mtPRP_DOM;
import fr.ujm.tse.lt2c.satin.rules.mark1mt.Mark1mtPRP_RNG;
import fr.ujm.tse.lt2c.satin.rules.mark1mt.Mark1mtPRP_SPO1;
import fr.ujm.tse.lt2c.satin.rules.mark1mt.Mark1mtSCM_DOM1;
import fr.ujm.tse.lt2c.satin.rules.mark1mt.Mark1mtSCM_DOM2;
import fr.ujm.tse.lt2c.satin.rules.mark1mt.Mark1mtSCM_EQC2;
import fr.ujm.tse.lt2c.satin.rules.mark1mt.Mark1mtSCM_EQP2;
import fr.ujm.tse.lt2c.satin.rules.mark1mt.Mark1mtSCM_RNG1;
import fr.ujm.tse.lt2c.satin.rules.mark1mt.Mark1mtSCM_RNG2;
import fr.ujm.tse.lt2c.satin.rules.mark1mt.Mark1mtSCM_SCO;
import fr.ujm.tse.lt2c.satin.rules.mark1mt.Mark1mtSCM_SPO;
import fr.ujm.tse.lt2c.satin.tools.ParserImplNaive;
import fr.ujm.tse.lt2c.satin.triplestore.TemporaryVerticalPartioningTripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.VerticalPartioningTripleStore;

public class ReasonnerVerticalMT {

	private static Logger logger = Logger.getLogger(ReasonnerVerticalMT.class);

	public static void main(String[] args) {

		for(int i=0; i<100; i++){
//
//			System.out.println("subclassof.owl 5618 bits");
//			infere("subclassof.owl");
//			System.out.println();
//
//			System.out.println("sample1.owl 9714 bits");
//			infere("sample1.owl");
//			System.out.println();
//
//			System.out.println("univ-bench.owl 13840 bits");
//			infere("univ-bench.owl");
//			System.out.println();
//
//			System.out.println("sweetAll.owl 17538 bits");
//			infere("sweetAll.owl");
//			System.out.println();
//
			System.out.println("wine.rdf 78225 bits");
			infere("wine.rdf");
			System.out.println();
//
//			System.out.println("geopolitical_200Ko.owl 199105 bits");
//			infere("geopolitical_200Ko.owl");
//			System.out.println();
//
//			System.out.println("geopolitical_300Ko.owl 306377 bits");
//			infere("geopolitical_300Ko.owl");
//			System.out.println();
//
//			System.out.println("geopolitical_500Ko.owl 497095 bits");
//			infere("geopolitical_500Ko.owl");
//			System.out.println();
//
//			System.out.println("geopolitical_1Mo.owl 1047485 bits");
//			infere("geopolitical_1Mo.owl");
//			System.out.println();
//
//			System.out.println("geopolitical.owl 1780714 bits");
//			infere("geopolitical.owl");
//			System.out.println();
//
//			System.out.println("efo.owl 26095973 bits");
//			infere("efo.owl");
//			System.out.println();
//
//			System.out.println("opencyc.owl 252122090 bits");
//			infere("opencyc.owl");
//			System.out.println();
//
		}

	}

	private static void infere(String input) {

		TripleStore tripleStore = new VerticalPartioningTripleStore();
		Dictionnary dictionnary = new DictionnaryImplNaive();
		Parser parser = new ParserImplNaive(dictionnary, tripleStore);

		long startTime = System.nanoTime();

		parser.parse(input);

		logger.debug("Parsing completed");

		long parsingTime = System.nanoTime();

		long beginNbTriples = tripleStore.getAll().size();

		System.out.println(beginNbTriples);

		ArrayList<Rule> rules = new ArrayList<>();
		TemporaryVerticalPartioningTripleStore usableTriples = new TemporaryVerticalPartioningTripleStore();
		Collection<Triple> newTriples = new HashSet<>();		

		/*Initialize rules used for inference on RhoDF*/

		//		rules.add(new Mark1EQ_REF(dictionnary, usableTriples, newTriples, tripleStore));
		rules.add(new Mark1mtPRP_DOM(dictionnary, usableTriples, newTriples, tripleStore));
		rules.add(new Mark1mtPRP_RNG(dictionnary, usableTriples, newTriples, tripleStore));
		rules.add(new Mark1mtPRP_SPO1(dictionnary, usableTriples, newTriples, tripleStore));
		rules.add(new Mark1mtCAX_SCO(dictionnary, usableTriples, newTriples, tripleStore));
		rules.add(new Mark1mtSCM_SCO(dictionnary, usableTriples, newTriples, tripleStore));
		rules.add(new Mark1mtSCM_EQC2(dictionnary, usableTriples, newTriples, tripleStore));
		rules.add(new Mark1mtSCM_SPO(dictionnary, usableTriples, newTriples, tripleStore));
		rules.add(new Mark1mtSCM_EQP2(dictionnary, usableTriples, newTriples, tripleStore));
		rules.add(new Mark1mtSCM_DOM1(dictionnary, usableTriples, newTriples, tripleStore));
		rules.add(new Mark1mtSCM_DOM2(dictionnary, usableTriples, newTriples, tripleStore));
		rules.add(new Mark1mtSCM_RNG1(dictionnary, usableTriples, newTriples, tripleStore));
		rules.add(new Mark1mtSCM_RNG2(dictionnary, usableTriples, newTriples, tripleStore));

		int old_size, new_size, steps=0;

		do{
			old_size = tripleStore.getAll().size();
			long stepTime = System.nanoTime();
			logger.debug("--------------------STEP "+steps+"--------------------");
			//			logger.debug("RS BF "+usableTriples);
			
//			ListeningExecutorService pool = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
			ExecutorService executor = Executors.newFixedThreadPool(rules.size());

			ArrayList<Thread> threads = new ArrayList<>();
			for (Rule rule : rules) {
				executor.submit(rule);
			}

//			for (Thread thread : threads) {
//				try {
//					thread.join();
//				} catch (InterruptedException e) {
//					System.err.println("THREAD JOIN ERROR");
//					e.printStackTrace();
//				}
//			}
			
			try {
				executor.awaitTermination(10, TimeUnit.DAYS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//			logger.debug("RS AT "+usableTriples);
			//			usableTriples = new HashSet<>();
			usableTriples.clear();
			usableTriples.addAll(newTriples);
			newTriples.clear();

			logger.debug("Usable triples: "+usableTriples.size());

			new_size = tripleStore.getAll().size();
			long step2Time = System.nanoTime();
			logger.debug((step2Time-stepTime)+"ns for "+(new_size-old_size)+" triples");
			steps++;
			
		}while(!usableTriples.isEmpty());

		long endTime = System.nanoTime();

		System.out.println("Dictionnary size: "+dictionnary.size());
		System.out.println("Initial triples: "+beginNbTriples);
		System.out.println("Triples after inference: "+tripleStore.getAll().size());
		System.out.println("Generated triples: "+(tripleStore.getAll().size()-beginNbTriples));
		System.out.println("Iterations: "+steps);
		System.out.println("Parsing: "+(parsingTime-startTime)/1000000.0+"ns");
		System.out.println("Inference: "+(endTime-parsingTime)/1000000.0+"ns");
		System.out.println("Total time: "+(endTime-startTime)/1000000.0+"ns");
		System.out.print("File writing: ");
		tripleStore.writeToFile("infered"+input+".out", dictionnary);
		System.out.println("ok");
	}

}
