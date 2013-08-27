package fr.ujm.tse.lt2c.satin.rules.naiveImpl;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;


import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.TripleImplNaive;

public class NaiveSCM_RNG2 implements Rule {

	private static Logger logger = Logger.getLogger(NaiveSCM_RNG2.class);
	private Dictionnary dictionnary;
	private TripleStore tripleStore;

	public NaiveSCM_RNG2(Dictionnary dictionnary, TripleStore tripleStore) {
		super();
		this.dictionnary = dictionnary;
		this.tripleStore = tripleStore;
	}

	@Override
	public void run() {

		/**
		 * 	INPUT
		 * p2 rdfs:range c
		 * p1 rdfs:subPropertyOf p2
		 *  OUPUT
		 * p1 rdfs:range c
		 */

		/*
		 * Get concepts codes in dictionnary
		 */
		long range = dictionnary.add("http://www.w3.org/2000/01/rdf-schema#range");
		long subPropertyOf = dictionnary.add("http://www.w3.org/2000/01/rdf-schema#subPropertyOf");

		/*
		 * Get triples matching input 
		 * Create
		 */
		Collection<Triple> range_Triples = tripleStore.getbyPredicate(range);
		Collection<Triple> subPropertyOf_Triples = tripleStore.getbyPredicate(subPropertyOf);
		Collection<Triple> outputTriples = new HashSet<>();

		for (Triple t1 : range_Triples) {
			long s1=t1.getSubject(), o1=t1.getObject();

			for (Triple t2 : subPropertyOf_Triples) {
				long s2=t2.getSubject(), o2=t2.getObject();

				if(s1==o2){
					Triple result = new TripleImplNaive(s2, range, o1);
					logger.trace("SCM_RNG2 "+dictionnary.printTriple(t1)+" & "+dictionnary.printTriple(t2)+" -> "+dictionnary.printTriple(result));
					outputTriples.add(result);
				}

			}

		}
		tripleStore.addAll(outputTriples);

	}

	@Override
	public Logger getLogger() {
		return logger;
	}

}
