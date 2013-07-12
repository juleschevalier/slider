package fr.ujm.tse.lt2c.satin.naiveImpl;

import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Parser;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.NaiveEQ_REP_O;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.NaiveEQ_REP_P;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.NaiveEQ_REP_S;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.NaiveSCM_SCO;

public class ReasonnerNaiveImpl {
	
	private static Logger logger = Logger.getLogger(ReasonnerNaiveImpl.class);
	
	public static void main(String[] args) {
		
		TripleStore tripleStore = new TripleStoreImplNaive();
		Dictionnary dictionnary = new DictionnaryImplNaive();
		Parser parser = new ParserImplNaive(dictionnary, tripleStore);
		
		long startTime = new Date().getTime();
		
		parser.parse("subclassof.owl");
//		parser.parse("sample1.owl");
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
		
		/*Initialize rules used for inference*/
//		//range
//		rules.add(new NaivePRP_RNG(dictionnary, tripleStore));
//		//domain
//		rules.add(new NaivePRP_DOM(dictionnary, tripleStore));
//		//subClassOf
		rules.add(new NaiveSCM_SCO(dictionnary, tripleStore));
//		rules.add(new NaiveSCM_EQC2(dictionnary, tripleStore));
//		rules.add(new NaiveSCM_RNG1(dictionnary, tripleStore));
//		rules.add(new NaiveSCM_DOM1(dictionnary, tripleStore));
//		//subPropertyOf
//		rules.add(new NaivePRP_SPO1(dictionnary, tripleStore));
//		rules.add(new NaiveSCM_SPO(dictionnary, tripleStore));
//		rules.add(new NaiveSCM_DOM2(dictionnary, tripleStore));
//		rules.add(new NaiveSCM_RNG2(dictionnary, tripleStore));
//		rules.add(new NaiveSCM_EQP2(dictionnary, tripleStore));
//		//equivalentProperty
//		rules.add(new NaivePRP_EQP1(dictionnary, tripleStore));
//		rules.add(new NaivePRP_EQP2(dictionnary, tripleStore));
//		rules.add(new NaiveSCM_EQP1(dictionnary, tripleStore));
//		//inverseOf
//		rules.add(new NaivePRP_INV1(dictionnary, tripleStore));
//		rules.add(new NaivePRP_INV2(dictionnary, tripleStore));
//		//sameAs
//		rules.add(new NaiveEQ_REF(dictionnary, tripleStore));
//		rules.add(new NaiveEQ_SYM(dictionnary, tripleStore));
//		rules.add(new NaiveEQ_TRANS(dictionnary, tripleStore));
		rules.add(new NaiveEQ_REP_S(dictionnary, tripleStore));
		rules.add(new NaiveEQ_REP_P(dictionnary, tripleStore));
		rules.add(new NaiveEQ_REP_O(dictionnary, tripleStore));
//		//equivalentClass
//		rules.add(new NaiveSCM_EQC1(dictionnary, tripleStore));
//		//differentFrom
//		rules.add(new NaiveEQ_DIFF1(dictionnary, tripleStore));
//		//propertyDisjointWith
//		rules.add(new NaivePRP_PDW(dictionnary, tripleStore));
		
		int old_size, new_size, steps=0;
		do{
			old_size = tripleStore.getAll().size();
			long stepTime = new Date().getTime();
			for (Rule rule : rules) {
				rule.run();
			}
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
