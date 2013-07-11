package fr.ujm.tse.lt2c.satin.naiveImpl.rules;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.naiveImpl.TripleImplNaive;

public class NaiveSCM_DOM2 implements Rule {

	private static Logger logger = Logger.getLogger(NaiveSCM_DOM2.class);
	private Dictionnary dictionnary;
	private TripleStore tripleStore;

	public NaiveSCM_DOM2(Dictionnary dictionnary, TripleStore tripleStore) {
		super();
		this.dictionnary = dictionnary;
		this.tripleStore = tripleStore;
	}

	@Override
	public void run() {

		/**
		 * 	INPUT
		 * p2 rdfs:domain c
		 * p1 rdfs:subPropertyOf p2
		 *  OUPUT
		 * p1 rdfs:domain c
		 */

		/*
		 * Get concepts codes in dictionnary
		 */
		long domain = dictionnary.add("http://www.w3.org/2000/01/rdf-schema#domain");
		long subPropertyOf = dictionnary.add("http://www.w3.org/2000/01/rdf-schema#subPropertyOf");

		/*
		 * Get triples matching input 
		 * Create
		 */
		Collection<Triple> domain_Triples = tripleStore.getbyPredicate(domain);
		Collection<Triple> subPropertyOf_Triples = tripleStore.getbyPredicate(subPropertyOf);
		Collection<Triple> outputTriples = new HashSet<>();

		for (Triple t1 : domain_Triples) {
			long s1=t1.getSubject(), o1=t1.getObject();

			for (Triple t2 : subPropertyOf_Triples) {
				long s2=t2.getSubject(), o2=t2.getObject();

				if(s1==o2){
					Triple result = new TripleImplNaive(s2, domain, o1);
					logger.trace("SCM_DOM2 "+dictionnary.printTriple(t1)+" & "+dictionnary.printTriple(t2)+" -> "+dictionnary.printTriple(result));
					outputTriples.add(result);
				}

			}

		}
		tripleStore.addAll(outputTriples);

	}

}
