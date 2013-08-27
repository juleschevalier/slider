package fr.ujm.tse.lt2c.satin.rules.naiveImpl;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;


import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.TripleImplNaive;

public class NaivePRP_DOM implements Rule {

	private static Logger logger = Logger.getLogger(NaivePRP_DOM.class);
	private Dictionnary dictionnary;
	private TripleStore tripleStore;

	public NaivePRP_DOM(Dictionnary dictionnary, TripleStore tripleStore) {
		super();
		this.dictionnary = dictionnary;
		this.tripleStore = tripleStore;
	}

	@Override
	public void run() {


		/**
		 * 	INPUT
		 * p rdfs:domain c
		 * x p y
		 *  OUPUT
		 * x rdf:type c
		 */

		/*
		 * Get concepts codes in dictionnary
		 */
		long domain = dictionnary.add("http://www.w3.org/2000/01/rdf-schema#domain");
		long type = dictionnary.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");

		/*
		 * Get triples matching input 
		 * Create
		 */
		Collection<Triple> domain_Triples = tripleStore.getbyPredicate(domain);
		Collection<Triple> outputTriples = new HashSet<>();

		for (Triple t1 : domain_Triples) {
			long s1=t1.getSubject(), o1=t1.getObject();

			for (Triple t2 : tripleStore.getbyPredicate(s1)) {
				long s2=t2.getSubject();
				Triple result = new TripleImplNaive(s2, type, o1);

				logger.trace("PRP_DOM "+dictionnary.printTriple(t1)+" & "+dictionnary.printTriple(t2)+" -> "+dictionnary.printTriple(result));
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
