package fr.ujm.tse.lt2c.satin.naiveImpl.rules;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.naiveImpl.TripleImplNaive;

public class NaiveEQ_TRANS implements Rule {

	private static Logger logger = Logger.getLogger(NaiveEQ_TRANS.class);
	private Dictionnary dictionnary;
	private TripleStore tripleStore;

	public NaiveEQ_TRANS(Dictionnary dictionnary, TripleStore tripleStore) {
		super();
		this.dictionnary = dictionnary;
		this.tripleStore = tripleStore;
	}

	@Override
	public void run() {


		/**
		 * 	INPUT
		 * x owl:sameAs y
		 * y owl:sameAs z
		 *  OUPUT
		 * x owl:sameAs z
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

			for (Triple t2 : sameAs_Triples) {
				long s2=t2.getSubject(), o2=t2.getObject();

				if(o1==s2){
					Triple result = new TripleImplNaive(s1, sameAs, o2);
					logger.trace("EQ_SYM "+dictionnary.printTriple(t1)+" + "+dictionnary.printTriple(t2)+" -> "+dictionnary.printTriple(result));
					outputTriples.add(result);
				}
			}
		}
		tripleStore.addAll(outputTriples);
	}

}
