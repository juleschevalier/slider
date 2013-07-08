package fr.ujm.tse.lt2c.satin.naiveImpl.rules;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.naiveImpl.TripleImplNaive;

public class RuleEQ_SYM implements Rule {

	private static Logger logger = Logger.getLogger(RuleEQ_SYM.class);

	@Override
	public void process(TripleStore tripleStore, Dictionnary dictionnary) {


		/**
		 * 	INPUT
		 * x owl:sameAs y
		 *  OUPUT
		 * y owl:sameAs x
		 */

		/*
		 * Get concepts codes in dictionnary
		 */
		long sameAs = dictionnary.add("http://www.w3.org/2002/07/owl#sameAs");

		/*
		 * Get triples matching input 
		 * Create
		 */
		Collection<Triple> sameAs_Triples = tripleStore.getbyPredicate(sameAs);
		Collection<Triple> outputTriples = new HashSet<>();

		for (Triple t : sameAs_Triples) {
			long s=t.getSubject(), o=t.getObject();
			Triple result = new TripleImplNaive(o, sameAs, s);
			logger.trace("EQ_SYM "+dictionnary.printTriple(t)+" -> "+dictionnary.printTriple(result));
			outputTriples.add(result);
		}
		tripleStore.addAll(outputTriples);
	}

}
