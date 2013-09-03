package fr.ujm.tse.lt2c.satin.reasoner;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVWriter;
import fr.ujm.tse.lt2c.satin.dictionnary.DictionnaryImplNaive;
import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Parser;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
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

	private static Logger logger = Logger.getLogger(ReasonnerVerticalMTRWLock.class);

	public static void main(String[] args) {

		CSVWriter writer=null;
		try {
			writer = new CSVWriter(new FileWriter("resultsMTLock.csv"), ';');

			String[] headers = {"File","i","Initial triples","Infered triples","Loops","Time","Left over"};
			writer.writeNext(headers);

			for(int i=0; i<10; i++){

				System.out.println("subclassof.owl 5618 bits");
				infere("subclassof.owl",i,writer);
				System.out.println();

				System.out.println("sample1.owl 9714 bits");
				infere("sample1.owl",i,writer);
				System.out.println();

				System.out.println("univ-bench.owl 13840 bits");
				infere("univ-bench.owl",i,writer);
				System.out.println();
	
				System.out.println("sweetAll.owl 17538 bits");
				infere("sweetAll.owl",i,writer);
				System.out.println();
	
				System.out.println("wine.rdf 78225 bits");
				infere("wine.rdf",i,writer);
				System.out.println();
	
				System.out.println("geopolitical_200Ko.owl 199105 bits");
				infere("geopolitical_200Ko.owl",i,writer);
				System.out.println();
	
				System.out.println("geopolitical_300Ko.owl 306377 bits");
				infere("geopolitical_300Ko.owl",i,writer);
				System.out.println();
	
				System.out.println("geopolitical_500Ko.owl 497095 bits");
				infere("geopolitical_500Ko.owl",i,writer);
				System.out.println();
	
				System.out.println("geopolitical_1Mo.owl 1047485 bits");
				infere("geopolitical_1Mo.owl",i,writer);
				System.out.println();
	//
	//			System.out.println("geopolitical.owl 1780714 bits");
	//			infere("geopolitical.owl",i,writer);
	//			System.out.println();
	//
	//			System.out.println("efo.owl 26095973 bits");
	//			infere("efo.owl",i,writer);
	//			System.out.println();
	//
	//			System.out.println("opencyc.owl 252122090 bits");
	//			infere("opencyc.owl",i,writer);
	//			System.out.println();
	//
			}

			writer.close();
			System.out.println("Work finished");
		} catch (IOException e) {
			System.out.print("I/O error : ");
			e.printStackTrace();
		}

	}

	private static void infere(String input, int i, CSVWriter writer) {

		TripleStore tripleStore = new VerticalPartioningTripleStoreRWLock();
		Dictionnary dictionnary = new DictionnaryImplNaive();
		Parser parser = new ParserImplNaive(dictionnary, tripleStore);

//		long startTime = System.nanoTime();

		parser.parse(input);

		logger.debug("Parsing completed");

		long parsingTime = System.nanoTime();

		long beginNbTriples = tripleStore.size();

		ArrayList<Rule> rules = new ArrayList<>();
		TemporaryVerticalPartioningTripleStoreRWLock usableTriples = new TemporaryVerticalPartioningTripleStoreRWLock();
		Collection<Triple> newTriples = new HashSet<>();		

		/*Initialize rules used for inference on RhoDF*/

		//	rules.add(new Mark1EQ_REF(dictionnary, usableTriples, newTriples, tripleStore));
		rules.add(new Mark1PRP_DOM(dictionnary, usableTriples, newTriples, tripleStore));
		rules.add(new Mark1PRP_RNG(dictionnary, usableTriples, newTriples, tripleStore));
		rules.add(new Mark1PRP_SPO1(dictionnary, usableTriples, newTriples, tripleStore));
		rules.add(new Mark1CAX_SCO(dictionnary, usableTriples, newTriples, tripleStore));
		rules.add(new Mark1SCM_SCO(dictionnary, usableTriples, newTriples, tripleStore));
		rules.add(new Mark1SCM_EQC2(dictionnary, usableTriples, newTriples, tripleStore));
		rules.add(new Mark1SCM_SPO(dictionnary, usableTriples, newTriples, tripleStore));
		rules.add(new Mark1SCM_EQP2(dictionnary, usableTriples, newTriples, tripleStore));
		rules.add(new Mark1SCM_DOM1(dictionnary, usableTriples, newTriples, tripleStore));
		rules.add(new Mark1SCM_DOM2(dictionnary, usableTriples, newTriples, tripleStore));
		rules.add(new Mark1SCM_RNG1(dictionnary, usableTriples, newTriples, tripleStore));
		rules.add(new Mark1SCM_RNG2(dictionnary, usableTriples, newTriples, tripleStore));

		int old_size, new_size, steps=0;

		do{
			old_size = tripleStore.getAll().size();
			long stepTime = System.nanoTime();
			logger.debug("--------------------STEP "+steps+"--------------------");

			ExecutorService executor = Executors.newFixedThreadPool(rules.size());

			for (Rule rule : rules) {
				executor.submit(rule);
			}

			executor.shutdown();
			try {
				executor.awaitTermination(10, TimeUnit.DAYS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			usableTriples.clear();
			usableTriples.addAll(newTriples);
			newTriples.clear();

			logger.debug("Usable triples: "+usableTriples.size());

			new_size = tripleStore.getAll().size();
			long step2Time = System.nanoTime();
			logger.debug((step2Time-stepTime)+"ns for "+(new_size-old_size)+" triples");
			steps++;

		}while(!usableTriples.isEmpty());
		
		Comparator comparator = new Comparator("jena_"+input);
		
		long size = comparator.compare(tripleStore, dictionnary);

		long endTime = System.nanoTime();
		//		System.out.println("Dictionary size: "+dictionnary.size());
		//		System.out.println("Initial triples: "+beginNbTriples);
		//		System.out.println("Triples after inference: "+tripleStore.size());
		//		System.out.println("Generated triples: "+(tripleStore.size()-beginNbTriples));
		//		System.out.println("Iterations: "+steps);
		//		System.out.println("Parsing: "+(parsingTime-startTime)/1000000.0+"ns");
		//		System.out.println("Inference: "+(endTime-parsingTime)/1000000.0+"ns");
		//		System.out.println("Total time: "+(endTime-startTime)/1000000.0+"ns");
		//		System.out.print("File writing: ");
		//		tripleStore.writeToFile("Inferred"+input+".out", dictionnary);
		//		System.out.println("ok");

		//		String[] headers = {"File","Size","i","Initial triples","Infered triples","Loops","Time","Correctness"};
		String[] datas = {input,""+i,""+beginNbTriples,""+(tripleStore.size()-beginNbTriples),""+steps,""+(endTime-parsingTime),""+size};
		writer.writeNext(datas);

	}

}
