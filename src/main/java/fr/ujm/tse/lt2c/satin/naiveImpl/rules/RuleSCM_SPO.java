package fr.ujm.tse.lt2c.satin.naiveImpl.rules;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.naiveImpl.TripleImplNaive;

public class RuleSCM_SPO implements Rule {

	private static Logger logger = Logger.getLogger(RuleSCM_SPO.class);

	@Override
	public void process(TripleStore tripleStore, Dictionnary dictionnary) {

		/**
		 * 	INPUT
		 * p1 rdfs:subPropertyOf p2
		 * p2 rdfs:subPropertyOf p3
		 *  OUPUT
		 * p1 rdfs:subPropertyOf p3
		 */

		/*
		 * Get concepts codes in dictionnary
		 */
		long subPropertyOf = dictionnary.add("http://www.w3.org/2000/01/rdf-schema#subPropertyOf");

		/*
		 * Get triples matching input 
		 * Create
		 */
		Collection<Triple> subPropertyOf_Triples = tripleStore.getbyPredicate(subPropertyOf);
		Collection<Triple> outputTriples = new HashSet<>();

		for (Triple t1 : subPropertyOf_Triples) {
			long s1=t1.getSubject(), o1=t1.getObject();

			for (Triple t2 : subPropertyOf_Triples) {
				long s2=t2.getSubject(), o2=t2.getObject();

				if(o1==s2){
					Triple result = new TripleImplNaive(s1, subPropertyOf, o2);
					logger.trace("SCM_SPO "+dictionnary.printTriple(t1)+" & "+dictionnary.printTriple(t2)+" -> "+dictionnary.printTriple(result));
					outputTriples.add(result);
				}

			}

		}
		tripleStore.addAll(outputTriples);

	}

}
