package fr.ujm.tse.lt2c.satin.mark1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Parser;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.mark1.rules.Mark1EQ_REP_O;
import fr.ujm.tse.lt2c.satin.mark1.rules.Mark1EQ_REP_P;
import fr.ujm.tse.lt2c.satin.mark1.rules.Mark1EQ_REP_S;
import fr.ujm.tse.lt2c.satin.mark1.rules.Mark1SCM_SCO;
import fr.ujm.tse.lt2c.satin.naiveImpl.DictionnaryImplNaive;
import fr.ujm.tse.lt2c.satin.naiveImpl.ParserImplNaive;
import fr.ujm.tse.lt2c.satin.naiveImpl.TripleStoreImplNaive;

public class ReasonnerMark1 {
	
	private static Logger logger = Logger.getLogger(ReasonnerMark1.class);
	
	public static void main(String[] args) {
		
		TripleStore tripleStore = new TripleStoreImplNaive();
		Dictionnary dictionnary = new DictionnaryImplNaive();
		Parser parser = new ParserImplNaive(dictionnary, tripleStore);
		
		long startTime = new Date().getTime();
		
//		parser.parse("sample1.owl");
		parser.parse("subclassof.owl");
//		parser.parse("people+pets.rdf");
//		parser.parse("haters.rdf");
//		parser.parse("twopets.rdf");
//		parser.parse("geopolitical.owl");
		
		logger.debug("Parsing complet");
		logger.debug("Entrées dictionnaire : "+dictionnary.size());
		logger.debug("Triples : "+tripleStore.getAll().size());
		
		long parsingTime = new Date().getTime();
		
		long beginNbTriples = tripleStore.getAll().size();
		
		ArrayList<Rule> rules = new ArrayList<>();
		Collection<Triple> usableTriples = null;
		Collection<Triple> newTriples = new HashSet<>();
		
		/*Initialize rules used for inference*/
		//domain
//		rules.add(new RulePRP_DOM(dictionnary, usabletriples, tripleStore));
		//range
//		rules.add(new RulePRP_RNG(dictionnary, tripleStore));
		//subClass
		rules.add(new Mark1SCM_SCO(dictionnary, usableTriples, newTriples, tripleStore));
		//subPropertyOf
//		rules.add(new RuleSCM_SPO(dictionnary, tripleStore));
		rules.add(new Mark1EQ_REP_S(dictionnary, usableTriples, newTriples, tripleStore));
		rules.add(new Mark1EQ_REP_P(dictionnary, usableTriples, newTriples, tripleStore));
		rules.add(new Mark1EQ_REP_O(dictionnary, usableTriples, newTriples, tripleStore));
//		rules.add(new Mark1EQ_REF(dictionnary, usableTriples, newTriples, tripleStore));
		
		int old_size, new_size, steps=0;
		
		do{
			old_size = tripleStore.getAll().size();
			long stepTime = new Date().getTime();
			
			for (Rule rule : rules) {
				rule.run();
			}
			usableTriples = new HashSet<>();
			usableTriples.addAll(newTriples);
			newTriples.clear();
			
			logger.debug("Usable triples: "+usableTriples.size());
			
			new_size = tripleStore.getAll().size();
			long step2Time = new Date().getTime();
			logger.debug((step2Time-stepTime)+"ms for "+(new_size-old_size)+" triples");
			steps++;
		}while(old_size != new_size);
		
		long endTime = new Date().getTime();
		
		System.out.println("Dictionnary size: "+dictionnary.size());
		System.out.println("Initial triples: "+beginNbTriples);
		System.out.println("Triples after inference: "+tripleStore.getAll().size());
		System.out.println("Generated triples: "+(tripleStore.getAll().size()-beginNbTriples));
		System.out.println("Iterations: "+steps);
		System.out.println("Parsing: "+(parsingTime-startTime)+"ms");
		System.out.println("Inference: "+(endTime-parsingTime)+"ms");
		System.out.println("Total time: "+(endTime-startTime)+"ms");
		System.out.print("File writing: ");
		tripleStore.writeToFile("mark1.out", dictionnary);
		System.out.println("ok");
		
	}

}
