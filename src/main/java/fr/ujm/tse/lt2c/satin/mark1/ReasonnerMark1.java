package fr.ujm.tse.lt2c.satin.mark1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Parser;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.mark1.rules.Mark1CAX_SCO;
import fr.ujm.tse.lt2c.satin.mark1.rules.Mark1PRP_DOM;
import fr.ujm.tse.lt2c.satin.mark1.rules.Mark1PRP_RNG;
import fr.ujm.tse.lt2c.satin.mark1.rules.Mark1PRP_SPO1;
import fr.ujm.tse.lt2c.satin.mark1.rules.Mark1SCM_DOM1;
import fr.ujm.tse.lt2c.satin.mark1.rules.Mark1SCM_DOM2;
import fr.ujm.tse.lt2c.satin.mark1.rules.Mark1SCM_EQC2;
import fr.ujm.tse.lt2c.satin.mark1.rules.Mark1SCM_EQP2;
import fr.ujm.tse.lt2c.satin.mark1.rules.Mark1SCM_RNG1;
import fr.ujm.tse.lt2c.satin.mark1.rules.Mark1SCM_RNG2;
import fr.ujm.tse.lt2c.satin.mark1.rules.Mark1SCM_SCO;
import fr.ujm.tse.lt2c.satin.mark1.rules.Mark1SCM_SPO;
import fr.ujm.tse.lt2c.satin.naiveImpl.DictionnaryImplNaive;
import fr.ujm.tse.lt2c.satin.naiveImpl.ParserImplNaive;
import fr.ujm.tse.lt2c.satin.triplestore.impl.VerticalPartioningTripleStore;

public class ReasonnerMark1 {
	
	private static Logger logger = Logger.getLogger(ReasonnerMark1.class);
	
	public static void main(String[] args) {
		
		TripleStore tripleStore = new VerticalPartioningTripleStore();
		Dictionnary dictionnary = new DictionnaryImplNaive();
		Parser parser = new ParserImplNaive(dictionnary, tripleStore);
		
		long startTime = System.nanoTime();
		
//		parser.parse("subclassof.owl");
//		parser.parse("sample1.owl");
//		parser.parse("people+pets.rdf");
//		parser.parse("haters.rdf");
//		parser.parse("twopets.rdf");
//		parser.parse("geopolitical.owl");
//		parser.parse("http://www.w3.org/TR/owl-guide/wine.rdf");
		parser.parse("wine.rdf");
		
		logger.debug("Parsing completed");
		
		long parsingTime = System.nanoTime();
		
		long beginNbTriples = tripleStore.getAll().size();
		
		ArrayList<Rule> rules = new ArrayList<>();
	Collection<Triple> usableTriples = new HashSet<>();
		//Collection<Triple> usableTriples = null;
		Collection<Triple> newTriples = new HashSet<>();		

		/*Initialize rules used for inference on RhoDF*/
//		rules.add(new Mark1EQ_REF(dictionnary, usableTriples, newTriples, tripleStore));
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
//		usableTriples.add(null);
		
		do{
			old_size = tripleStore.getAll().size();
			long stepTime = System.nanoTime();
			logger.debug("--------------------STEP "+steps+"--------------------");
//			logger.debug("RS BF "+usableTriples);
			for (Rule rule : rules) {
				rule.run();
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
		System.out.println("Parsing: "+(parsingTime-startTime)+"ns");
		System.out.println("Inference: "+(endTime-parsingTime)+"ns");
		System.out.println("Total time: "+(endTime-startTime)+"ns");
		System.out.print("File writing: ");
		tripleStore.writeToFile("triplestore.out", dictionnary);
		System.out.println("ok");
		
	}

}
