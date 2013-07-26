package fr.ujm.tse.lt2c.satin.rules.useless;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;


import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.TripleImplNaive;

public class NaiveSCM_EQC1 implements Rule {

	private static Logger logger = Logger.getLogger(NaiveSCM_EQC1.class);
	private Dictionnary dictionnary;
	private TripleStore tripleStore;

	public NaiveSCM_EQC1(Dictionnary dictionnary, TripleStore tripleStore) {
		super();
		this.dictionnary = dictionnary;
		this.tripleStore = tripleStore;
	}

	@Override
	public void run() {


		/**
		 * 	INPUT
		 * c1 owl:equivalentClass c2
		 *  OUPUT
		 * c1 rdfs:subClassOf c2
		 * c2 rdfs:subClassOf c1
		 */

		/*
		 * Get concepts codes in dictionnary
		 */
		long subClassOf = dictionnary.add("http://www.w3.org/2000/01/rdf-schema#subClassOf");
		long equivalentClass = dictionnary.add("http://www.w3.org/2002/07/owl#equivalentClass");

		/*
		 * Get triples matching input 
		 * Create
		 */
		Collection<Triple> equivalentClass_Triples = tripleStore.getbyPredicate(equivalentClass);
		Collection<Triple> outputTriples = new HashSet<>();

		for (Triple t1 : equivalentClass_Triples) {
			long s1=t1.getSubject(), o1=t1.getObject();
			Triple result1 = new TripleImplNaive(s1, subClassOf, o1);
			Triple result2 = new TripleImplNaive(o1, subClassOf, s1);
			logger.trace("SCM_EQC1 "+dictionnary.printTriple(t1)+" -> "+dictionnary.printTriple(result1)+" + "+dictionnary.printTriple(result2));
			outputTriples.add(result1);
			outputTriples.add(result2);

		}
		tripleStore.addAll(outputTriples);

	}

}
