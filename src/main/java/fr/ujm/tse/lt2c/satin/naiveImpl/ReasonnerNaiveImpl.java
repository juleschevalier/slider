package fr.ujm.tse.lt2c.satin.naiveImpl;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Parser;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.NaiveCAX_SCO;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.NaiveEQ_DIFF1;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.NaiveEQ_REF;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.NaiveEQ_REP_O;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.NaiveEQ_REP_P;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.NaiveEQ_REP_S;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.NaiveEQ_SYM;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.NaiveEQ_TRANS;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.NaivePRP_DOM;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.NaivePRP_EQP1;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.NaivePRP_EQP2;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.NaivePRP_INV1;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.NaivePRP_INV2;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.NaivePRP_PDW;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.NaivePRP_RNG;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.NaivePRP_SPO1;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.NaiveSCM_DOM1;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.NaiveSCM_DOM2;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.NaiveSCM_EQC1;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.NaiveSCM_EQC2;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.NaiveSCM_EQP1;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.NaiveSCM_EQP2;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.NaiveSCM_RNG1;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.NaiveSCM_RNG2;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.NaiveSCM_SCO;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.NaiveSCM_SPO;

public class ReasonnerNaiveImpl {
	
	private static Logger logger = Logger.getLogger(ReasonnerNaiveImpl.class);
	
	public static void main(String[] args) {
		
		TripleStore tripleStore = new TripleStoreImplNaive();
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
		
		logger.debug("Parsing complet");
		
		long parsingTime = System.nanoTime();
		
		long beginNbTriples = tripleStore.getAll().size();
		
		ArrayList<Rule> rules = new ArrayList<>();
		
		/*Initialize rules used for inference on RhoDF*/
//		rules.add(new NaiveEQ_REF(dictionnary, tripleStore));
//		rules.add(new NaivePRP_DOM(dictionnary, tripleStore));
//		rules.add(new NaivePRP_RNG(dictionnary, tripleStore));
//		rules.add(new NaivePRP_SPO1(dictionnary, tripleStore));
//		rules.add(new NaiveCAX_SCO(dictionnary, tripleStore));
//		rules.add(new NaiveSCM_SCO(dictionnary, tripleStore));
//		rules.add(new NaiveSCM_EQC2(dictionnary, tripleStore));
//		rules.add(new NaiveSCM_SPO(dictionnary, tripleStore));
//		rules.add(new NaiveSCM_EQP2(dictionnary, tripleStore));
//		rules.add(new NaiveSCM_SPO(dictionnary, tripleStore));
//		rules.add(new NaiveSCM_EQP2(dictionnary, tripleStore));
//		rules.add(new NaiveSCM_DOM1(dictionnary, tripleStore));
//		rules.add(new NaiveSCM_DOM2(dictionnary, tripleStore));
//		rules.add(new NaiveSCM_RNG1(dictionnary, tripleStore));
//		rules.add(new NaiveSCM_RNG2(dictionnary, tripleStore));
		
		/*Others*/
		rules.add(new NaiveCAX_SCO(dictionnary, tripleStore));
		rules.add(new NaiveEQ_DIFF1(dictionnary, tripleStore));
		rules.add(new NaiveEQ_REF(dictionnary, tripleStore));
		rules.add(new NaiveEQ_REP_O(dictionnary, tripleStore));
		rules.add(new NaiveEQ_REP_P(dictionnary, tripleStore));
		rules.add(new NaiveEQ_REP_S(dictionnary, tripleStore));
		rules.add(new NaiveEQ_SYM(dictionnary, tripleStore));
		rules.add(new NaiveEQ_TRANS(dictionnary, tripleStore));
		rules.add(new NaivePRP_DOM(dictionnary, tripleStore));
		rules.add(new NaivePRP_EQP1(dictionnary, tripleStore));
		rules.add(new NaivePRP_EQP2(dictionnary, tripleStore));
		rules.add(new NaivePRP_INV1(dictionnary, tripleStore));
		rules.add(new NaivePRP_INV2(dictionnary, tripleStore));
		rules.add(new NaivePRP_PDW(dictionnary, tripleStore));
		rules.add(new NaivePRP_RNG(dictionnary, tripleStore));
		rules.add(new NaivePRP_SPO1(dictionnary, tripleStore));
		rules.add(new NaiveSCM_DOM1(dictionnary, tripleStore));
		rules.add(new NaiveSCM_DOM2(dictionnary, tripleStore));
		rules.add(new NaiveSCM_EQC1(dictionnary, tripleStore));
		rules.add(new NaiveSCM_EQC2(dictionnary, tripleStore));
		rules.add(new NaiveSCM_EQP1(dictionnary, tripleStore));
		rules.add(new NaiveSCM_EQP2(dictionnary, tripleStore));
		rules.add(new NaiveSCM_RNG1(dictionnary, tripleStore));
		rules.add(new NaiveSCM_RNG2(dictionnary, tripleStore));
		rules.add(new NaiveSCM_SCO(dictionnary, tripleStore));
		rules.add(new NaiveSCM_SPO(dictionnary, tripleStore));
		
		int old_size, new_size, steps=0;
		do{
			old_size = tripleStore.getAll().size();
			long stepTime = System.nanoTime();
			for (Rule rule : rules) {
				rule.run();
			}
			new_size = tripleStore.getAll().size();
			long step2Time = System.nanoTime();
			logger.debug((step2Time-stepTime)+"ns for "+(new_size-old_size)+" triples");
			steps++;
		}while(old_size != new_size);
		
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
		
		tripleStore.writeToFile("naive.out", dictionnary);
		System.out.println("ok");
		
	}

}
