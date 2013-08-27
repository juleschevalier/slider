package fr.ujm.tse.lt2c.satin.rules.naiveImpl;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;


import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.TripleImplNaive;

public class NaivePRP_SPO1 implements Rule {

	private static Logger logger = Logger.getLogger(NaivePRP_SPO1.class);
	private Dictionnary dictionnary;
	private TripleStore tripleStore;

	public NaivePRP_SPO1(Dictionnary dictionnary, TripleStore tripleStore) {
		super();
		this.dictionnary = dictionnary;
		this.tripleStore = tripleStore;
	}

	@Override
	public void run() {


		/**
		 * 	INPUT
		 * p1 rdfs:subPropertyOf p2
		 * x p1 y
		 *  OUPUT
		 * x p2 y
		 */

		/*
		 * Get concepts codes in dictionnary
		 */
		long subPropertyOf = dictionnary.add("http://www.w3.org/2000/01/rdf-schema#subPropertyOf");

		/*
		 * Get triples matching input 
		 * Create
		 */
		Collection<Triple> subProperty_Triples = tripleStore.getbyPredicate(subPropertyOf);
		Collection<Triple> outputTriples = new HashSet<>();

		for (Triple t1 : subProperty_Triples) {
			long s1=t1.getSubject(), o1=t1.getObject();

			for (Triple t2 : tripleStore.getbyPredicate(s1)) {
				long s2=t2.getSubject(), o2=t2.getObject();

				Triple result = new TripleImplNaive(s2, o1, o2);
				logger.trace("PRP_SPO1 "+dictionnary.printTriple(t1)+" & "+dictionnary.printTriple(t2)+" -> "+dictionnary.printTriple(result));
				outputTriples.add(result);
			}

		}
		tripleStore.addAll(outputTriples);

	}

	@Override
	public Logger getLogger() {
		return logger;
	}

}
