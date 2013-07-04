package fr.ujm.tse.lt2c.satin.naiveImpl.rules;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.naiveImpl.TripleImplNaive;

public class RulePRP_RNG implements Rule {
	
	private static Logger logger = Logger.getLogger(RulePRP_RNG.class);

	@Override
	public void process(TripleStore tripleStore, Dictionnary dictionnary) {
		
		
		/**
		 * 	INPUT
		 * p rdfs:range c
		 * x p y
		 *  OUPUT
		 * y rdf:type c
		 */
		
		/*
		 * Get concepts codes in dictionnary
		 */
		long range = dictionnary.add("http://www.w3.org/2000/01/rdf-schema#range");
		long type = dictionnary.add("http://www.w3.org/2000/01/rdf-schema#type");
		
		/*
		 * Get triples matching input 
		 * Create
		 */
		Collection<Triple> subClassOf_Triples = tripleStore.getbyPredicate(range);
		Collection<Triple> outputTriples = new HashSet<>();
		
		for (Triple t1 : subClassOf_Triples) {
			long s1=t1.getSubject(), o1=t1.getObject();
			for (Triple t2 : tripleStore.getAll()) {
				long p2=t2.getPredicate(), o2=t2.getObject();
				if(s1==p2){
					Triple result = new TripleImplNaive(o2, type, o1);
					logger.trace("PRP_RNG "+dictionnary.printTriple(t1)+" & "+dictionnary.printTriple(t2)+" -> "+dictionnary.printTriple(result));
					outputTriples.add(result);
				}
			}
		}
		tripleStore.addAll(outputTriples);
		
	}

}
