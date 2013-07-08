package fr.ujm.tse.lt2c.satin.naiveImpl.rules;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.naiveImpl.TripleImplNaive;

public class RuleEQ_REF implements Rule {

	private static Logger logger = Logger.getLogger(RuleEQ_REF.class);

	@Override
	public void process(TripleStore tripleStore, Dictionnary dictionnary) {


		/**
		 * 	INPUT
		 * s p o
		 *  OUPUT
		 * s owl:sameAs s
		 * p owl:sameAs p
		 * o owl:sameAs o
		 */

		/*
		 * Get concepts codes in dictionnary
		 */
		long sameAs = dictionnary.add("http://www.w3.org/2002/07/owl#sameAs");

		/*
		 * Get triples matching input 
		 * Create
		 */
		Collection<Triple> outputTriples = new HashSet<>();

		for (Triple t : tripleStore.getAll()) {
			long s=t.getSubject(), p=t.getPredicate(), o=t.getObject();
			Triple result1 = new TripleImplNaive(s, sameAs, s);
			Triple result2 = new TripleImplNaive(p, sameAs, p);
			Triple result3 = new TripleImplNaive(o, sameAs, o);
			logger.trace("EQ_REF "+dictionnary.printTriple(t)+" -> "+dictionnary.printTriple(result1)+" & "+dictionnary.printTriple(result2)+" & "+dictionnary.printTriple(result3));
			outputTriples.add(result1);
			outputTriples.add(result2);
			outputTriples.add(result3);
		}
		tripleStore.addAll(outputTriples);
	}

}
