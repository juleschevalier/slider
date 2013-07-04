package fr.ujm.tse.lt2c.satin.reasonner;

import java.util.ArrayList;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Parser;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.naiveImpl.DictionnaryImplNaive;
import fr.ujm.tse.lt2c.satin.naiveImpl.ParserImplNaive;
import fr.ujm.tse.lt2c.satin.naiveImpl.TripleStoreImplNaive;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.RulePRP_DOM;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.RulePRP_RNG;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.RulePRP_SPO1;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.RuleSCM_DOM1;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.RuleSCM_DOM2;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.RuleSCM_EQC2;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.RuleSCM_EQP2;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.RuleSCM_RNG1;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.RuleSCM_RNG2;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.RuleSCM_SCO;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.RuleSCM_SPO;

public class Reasonner {
	
	public static void main(String[] args) {
		
		TripleStore tripleStore = new TripleStoreImplNaive();
		Dictionnary dictionnary = new DictionnaryImplNaive();
		Parser parser = new ParserImplNaive(dictionnary, tripleStore);
		
		parser.parse("sample1.owl");
		
		long beginNbTriples = tripleStore.getAll().size();
		
		ArrayList<Rule> rules = new ArrayList<>();

		rules.add(new RulePRP_RNG());
		rules.add(new RulePRP_DOM());
		rules.add(new RuleSCM_SCO());
		rules.add(new RuleSCM_EQC2());
		rules.add(new RuleSCM_RNG1());
		rules.add(new RuleSCM_DOM1());
		rules.add(new RulePRP_SPO1());
		rules.add(new RuleSCM_SPO());
		rules.add(new RuleSCM_DOM2());
		rules.add(new RuleSCM_RNG2());
		rules.add(new RuleSCM_EQP2());

		for (Rule rule : rules) {
			rule.process(tripleStore, dictionnary);			
		}
		
//		for (Triple triple : tripleStore.getAll()) {
//			System.out.println(dictionnary.printTriple(triple));
//		}

		System.out.println("Triples avant : "+beginNbTriples);
		System.out.println("Triples après : "+tripleStore.getAll().size());
		System.out.println("Triples générés : "+(tripleStore.getAll().size()-beginNbTriples));
		
	}

}
