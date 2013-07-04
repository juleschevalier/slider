package fr.ujm.tse.lt2c.satin.reasonner;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Parser;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.naiveImpl.DictionnaryImplNaive;
import fr.ujm.tse.lt2c.satin.naiveImpl.ParserImplNaive;
import fr.ujm.tse.lt2c.satin.naiveImpl.TripleStoreImplNaive;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.RulePRP_DOM;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.RulePRP_RNG;
import fr.ujm.tse.lt2c.satin.naiveImpl.rules.RuleSCM_SCO;

public class Reasonner {
	
	public static void main(String[] args) {
		
		TripleStore tripleStore = new TripleStoreImplNaive();
		Dictionnary dictionnary = new DictionnaryImplNaive();
		Parser parser = new ParserImplNaive(dictionnary, tripleStore);
		
		parser.parse("sample1.owl");
		
		long beginNbTriples = tripleStore.getAll().size();
		
		RuleSCM_SCO rule1 = new RuleSCM_SCO();
		RulePRP_RNG rule2 = new RulePRP_RNG();
		RulePRP_DOM rule3 = new RulePRP_DOM();

		rule2.process(tripleStore, dictionnary);
		rule3.process(tripleStore, dictionnary);
		rule1.process(tripleStore, dictionnary);
		
		for (Triple triple : tripleStore.getAll()) {
			System.out.println(dictionnary.printTriple(triple));
		}

		System.out.println("Triples before: "+beginNbTriples);
		System.out.println("Triples after: "+tripleStore.getAll().size());
		
	}

}
