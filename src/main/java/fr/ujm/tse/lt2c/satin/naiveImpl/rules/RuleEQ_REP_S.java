package fr.ujm.tse.lt2c.satin.naiveImpl.rules;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.naiveImpl.TripleImplNaive;

public class RuleEQ_REP_S implements Rule {

	private static Logger logger = Logger.getLogger(RuleEQ_REP_S.class);

	@Override
	public void process(TripleStore tripleStore, Dictionnary dictionnary) {


		/**
		 * 	INPUT
		 * s owl:sameAs s'
		 * s p o
		 *  OUPUT
		 * s' p o
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

		for (Triple t1 : sameAs_Triples) {
			long s1=t1.getSubject(), o1=t1.getObject();

			for (Triple t2 : tripleStore.getAll()) {
				long s2=t2.getSubject(), p2=t2.getPredicate(), o2=t2.getObject();

				if(s1==s2){
					Triple result = new TripleImplNaive(o1,p2,o2);
					logger.trace("EQ_REP_S "+dictionnary.printTriple(t1)+" + "+dictionnary.printTriple(t2)+" -> "+dictionnary.printTriple(result));
					outputTriples.add(result);
				}
			}
		}
		tripleStore.addAll(outputTriples);
	}

}
