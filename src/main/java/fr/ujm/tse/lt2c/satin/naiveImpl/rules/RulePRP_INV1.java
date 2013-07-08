package fr.ujm.tse.lt2c.satin.naiveImpl.rules;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.naiveImpl.TripleImplNaive;

public class RulePRP_INV1 implements Rule {
	
	private static Logger logger = Logger.getLogger(RulePRP_INV1.class);

	@Override
	public void process(TripleStore tripleStore, Dictionnary dictionnary) {
		
		
		/**
		 * 	INPUT
		 * p1 owl:InverseOf p2
		 * x p1 y
		 *  OUPUT
		 * y p2 x
		 */
		
		/*
		 * Get concepts codes in dictionnary
		 */
		long inverseOf = dictionnary.add("http://www.w3.org/2002/07/owl#inverseOf");
		
		/*
		 * Get triples matching input 
		 * Create
		 */
		Collection<Triple> inverseOf_Triples = tripleStore.getbyPredicate(inverseOf);
		Collection<Triple> outputTriples = new HashSet<>();
		
		for (Triple t1 : inverseOf_Triples) {
			long s1=t1.getSubject(), o1=t1.getObject();
			
			for (Triple t2 : tripleStore.getAll()) {
				long s2=t2.getSubject(), p2=t2.getPredicate(), o2=t2.getObject();
				if(s1==p2){
					Triple result = new TripleImplNaive(o2, o1, s2);
					logger.trace("PRP_INV1 "+dictionnary.printTriple(t1)+" & "+dictionnary.printTriple(t2)+" -> "+dictionnary.printTriple(result));
					outputTriples.add(result);
				}
			}
		}
		tripleStore.addAll(outputTriples);
		
	}

}
