package fr.ujm.tse.lt2c.satin.naiveImpl;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.rulesys.Node_RuleVariable;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Parser;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.NaiveCAX_SCO;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.NaivePRP_DOM;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.NaivePRP_RNG;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.NaivePRP_SPO1;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.NaiveSCM_DOM1;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.NaiveSCM_DOM2;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.NaiveSCM_EQC2;
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
		parser.parse("http://www.w3.org/TR/owl-guide/wine.rdf");
		
		logger.debug("Parsing complet");
		logger.debug("Entrées dictionnaire : "+dictionnary.size());
		logger.debug("Triples : "+tripleStore.getAll().size());
		
		long parsingTime = System.nanoTime();
		
		long beginNbTriples = tripleStore.getAll().size();
		
		ArrayList<Rule> rules = new ArrayList<>();
		
		/*Initialize rules used for inference on RhoDF*/
//		rules.add(new NaiveEQ_REF(dictionnary, tripleStore));
		rules.add(new NaivePRP_DOM(dictionnary, tripleStore));
		rules.add(new NaivePRP_RNG(dictionnary, tripleStore));
		rules.add(new NaivePRP_SPO1(dictionnary, tripleStore));
		rules.add(new NaiveCAX_SCO(dictionnary, tripleStore));
		rules.add(new NaiveSCM_SCO(dictionnary, tripleStore));
		rules.add(new NaiveSCM_EQC2(dictionnary, tripleStore));
		rules.add(new NaiveSCM_SPO(dictionnary, tripleStore));
		rules.add(new NaiveSCM_EQP2(dictionnary, tripleStore));
		rules.add(new NaiveSCM_SPO(dictionnary, tripleStore));
		rules.add(new NaiveSCM_EQP2(dictionnary, tripleStore));
		rules.add(new NaiveSCM_DOM1(dictionnary, tripleStore));
		rules.add(new NaiveSCM_DOM2(dictionnary, tripleStore));
		rules.add(new NaiveSCM_RNG1(dictionnary, tripleStore));
		rules.add(new NaiveSCM_RNG2(dictionnary, tripleStore));
		
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
		

		Model model = ModelFactory.createDefaultModel();
		
		for (Triple triple : tripleStore.getAll()) {
			Node s = new Node_RuleVariable(dictionnary.get(triple.getSubject()), (int) triple.getSubject());
			Node p = new Node_RuleVariable(dictionnary.get(triple.getPredicate()), (int) triple.getPredicate());
			Node o = new Node_RuleVariable(dictionnary.get(triple.getObject()), (int) triple.getObject());
			model.getGraph().add(new com.hp.hpl.jena.graph.Triple(s,p,o));
		}
		OutputStream os;
		try {
			os = new FileOutputStream("out.rdf");
			model.write(os);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("rdf writing ok");
		
	}

}
