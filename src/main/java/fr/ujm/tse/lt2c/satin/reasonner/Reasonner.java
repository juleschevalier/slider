package fr.ujm.tse.lt2c.satin.reasonner;

import java.util.ArrayList;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Parser;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.naiveImpl.DictionnaryImplNaive;
import fr.ujm.tse.lt2c.satin.naiveImpl.ParserImplNaive;
import fr.ujm.tse.lt2c.satin.naiveImpl.TripleStoreImplNaive;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.RuleEQ_DIFF1;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.RulePRP_DOM;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.RulePRP_EQP1;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.RulePRP_EQP2;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.RulePRP_INV1;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.RulePRP_INV2;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.RulePRP_PDW;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.RulePRP_RNG;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.RulePRP_SPO1;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.RuleSCM_DOM1;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.RuleSCM_DOM2;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.RuleSCM_EQC1;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.RuleSCM_EQC2;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.RuleSCM_EQP1;
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

		//range
		rules.add(new RulePRP_RNG());
		//domain
		rules.add(new RulePRP_DOM());
		//subClassOf
		rules.add(new RuleSCM_SCO());
		rules.add(new RuleSCM_EQC2());
		rules.add(new RuleSCM_RNG1());
		rules.add(new RuleSCM_DOM1());
		//subPropertyOf
		rules.add(new RulePRP_SPO1());
		rules.add(new RuleSCM_SPO());
		rules.add(new RuleSCM_DOM2());
		rules.add(new RuleSCM_RNG2());
		rules.add(new RuleSCM_EQP2());
		//equivalentProperty
		rules.add(new RulePRP_EQP1());
		rules.add(new RulePRP_EQP2());
		rules.add(new RuleSCM_EQP1());
		//inverseOf
		rules.add(new RulePRP_INV1());
		rules.add(new RulePRP_INV2());
		//sameAs
//		rules.add(new RuleEQ_REF());
//		rules.add(new RuleEQ_SYM());
//		rules.add(new RuleEQ_TRANS());
//		rules.add(new RuleEQ_REP_S());
//		rules.add(new RuleEQ_REP_P());
//		rules.add(new RuleEQ_REP_O());
		//equivalentClass
		rules.add(new RuleSCM_EQC1());
		//differentFrom
		rules.add(new RuleEQ_DIFF1());
		//propertyDisjointWith
		rules.add(new RulePRP_PDW());
		
		
		for (Rule rule : rules) {
			rule.process(tripleStore, dictionnary);
		}
		
//		for (Triple triple : tripleStore.getAll()) {
//			System.out.println(dictionnary.printTriple(triple));
//		}
		System.out.println("Entrées dictionnaire : "+dictionnary.size());
		System.out.println("Triples avant : "+beginNbTriples);
		System.out.println("Triples après : "+tripleStore.getAll().size());
		System.out.println("Triples générés : "+(tripleStore.getAll().size()-beginNbTriples));
		
	}

}
