package fr.ujm.tse.lt2c.satin.naiveImpl.rules;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.naiveImpl.TripleImplNaive;

public class NaiveEQ_REP_O implements Rule {

	private static Logger logger = Logger.getLogger(NaiveEQ_REP_O.class);
	private Dictionnary dictionnary;
	private TripleStore tripleStore;

	public NaiveEQ_REP_O(Dictionnary dictionnary, TripleStore tripleStore) {
		super();
		this.dictionnary = dictionnary;
		this.tripleStore = tripleStore;
	}

	@Override
	public void run() {


		/**
		 * 	INPUT
		 * o owl:sameAs o'
		 * s p o
		 *  OUPUT
		 * s p o'
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

//			for (Triple t2 : tripleStore.getAll()) {
			for (Triple t2 : tripleStore.getbyObject(s1)) {
				long s2=t2.getSubject(), p2=t2.getPredicate();
					Triple result = new TripleImplNaive(s2,p2,o1);
					logger.trace("EQ_REP_O "+dictionnary.printTriple(t1)+" + "+dictionnary.printTriple(t2)+" -> "+dictionnary.printTriple(result));
					outputTriples.add(result);
			}
		}
		tripleStore.addAll(outputTriples);
	}

}
