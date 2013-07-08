package fr.ujm.tse.lt2c.satin.naiveImpl.rules;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.naiveImpl.TripleImplNaive;

public class RuleSCM_EQP1 implements Rule {

	private static Logger logger = Logger.getLogger(RuleSCM_EQP1.class);

	@Override
	public void process(TripleStore tripleStore, Dictionnary dictionnary) {


		/**
		 * 	INPUT
		 * p1 owl:equivalentProperty p2
		 *  OUPUT
		 * p1 rdfs:subPropertyOf p2
		 * p2 rdfs:subPropertyOf p1
		 */

		/*
		 * Get concepts codes in dictionnary
		 */
		long subPropertyOf = dictionnary.add("http://www.w3.org/2000/01/rdf-schema#subPropertyOf");
		long equivalentProperty = dictionnary.add("http://www.w3.org/2002/07/owl#equivalentProperty");

		/*
		 * Get triples matching input 
		 * Create
		 */
		Collection<Triple> equivalentProperty_Triples = tripleStore.getbyPredicate(equivalentProperty);
		Collection<Triple> outputTriples = new HashSet<>();

		for (Triple t1 : equivalentProperty_Triples) {
			long s1=t1.getSubject(), o1=t1.getObject();
			Triple result1 = new TripleImplNaive(s1, subPropertyOf, o1);
			Triple result2 = new TripleImplNaive(o1, subPropertyOf, s1);
			logger.trace("SCM_EQP1 "+dictionnary.printTriple(t1)+" -> "+dictionnary.printTriple(result1)+" + "+dictionnary.printTriple(result2));
			outputTriples.add(result1);
			outputTriples.add(result2);

		}
		tripleStore.addAll(outputTriples);

	}

}
