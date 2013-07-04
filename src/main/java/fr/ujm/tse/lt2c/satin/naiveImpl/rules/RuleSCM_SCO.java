package fr.ujm.tse.lt2c.satin.naiveImpl.rules;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.naiveImpl.TripleImplNaive;

public class RuleSCM_SCO implements Rule {
	
	private static Logger logger = Logger.getLogger(RuleSCM_SCO.class);

	@Override
	public void process(TripleStore tripleStore, Dictionnary dictionnary) {
		
		
		/**
		 * 	INPUT
		 * c1 rdfs:subClassOf c2
		 * c2 rdfs:subClassOf c3
		 *  OUPUT
		 * c1 rdfs:subClassOf c3
		 */
		
		/*
		 * Get concepts codes in dictionnary
		 */
		long subClassOf = dictionnary.add("http://www.w3.org/2000/01/rdf-schema#subClassOf");
		
		/*
		 * Get triples matching input 
		 * Create
		 */
		Collection<Triple> subClassOf_Triples = tripleStore.getbyPredicate(subClassOf);
		Collection<Triple> outputTriples = new HashSet<>();
		
		for (Triple t1 : subClassOf_Triples) {
			long s1=t1.getSubject(), o1=t1.getObject();
			
			for (Triple t2 : subClassOf_Triples) {
				long s2=t2.getSubject(), o2=t2.getObject();
				
				if(o1==s2){
					Triple result = new TripleImplNaive(s1, subClassOf, o2);
					logger.trace("SCM_SCO "+dictionnary.printTriple(t1)+" & "+dictionnary.printTriple(t2)+" -> "+dictionnary.printTriple(result));
					outputTriples.add(result);
			}
				
			}
			
		}
		tripleStore.addAll(outputTriples);
		
	}

}
