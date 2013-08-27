package fr.ujm.tse.lt2c.satin.rules.naiveImpl;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;


import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.TripleImplNaive;

public class NaiveSCM_DOM1 implements Rule {

	private static Logger logger = Logger.getLogger(NaiveSCM_DOM1.class);
	private Dictionnary dictionnary;
	private TripleStore tripleStore;

	public NaiveSCM_DOM1(Dictionnary dictionnary, TripleStore tripleStore) {
		super();
		this.dictionnary = dictionnary;
		this.tripleStore = tripleStore;
	}

	@Override
	public void run() {


		/**
		 * 	INPUT
		 * p rdfs:domain c1
		 * c1 rdfs:subClassOf c2
		 *  OUPUT
		 * p rdfs:domain c2
		 */

		/*
		 * Get concepts codes in dictionnary
		 */
		long subClassOf = dictionnary.add("http://www.w3.org/2000/01/rdf-schema#subClassOf");
		long domain = dictionnary.add("http://www.w3.org/2000/01/rdf-schema#domain");

		/*
		 * Get triples matching input 
		 * Create
		 */
		Collection<Triple> domain_Triples = tripleStore.getbyPredicate(domain);
		Collection<Triple> subClassOf_Triples = tripleStore.getbyPredicate(subClassOf);
		Collection<Triple> outputTriples = new HashSet<>();

		for (Triple t1 : domain_Triples) {
			long s1=t1.getSubject(), o1=t1.getObject();

			for (Triple t2 : subClassOf_Triples) {
				long s2=t2.getSubject(), o2=t2.getObject();

				if(o1==s2){
					Triple result = new TripleImplNaive(s1, domain, o2);
					logger.trace("SCM_DOM1 "+dictionnary.printTriple(t1)+" & "+dictionnary.printTriple(t2)+" -> "+dictionnary.printTriple(result));
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
