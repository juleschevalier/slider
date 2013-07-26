package fr.ujm.tse.lt2c.satin.rules.naiveImpl;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;


import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.triplestore.TripleImplNaive;

public class NaiveSCM_EQC2 implements Rule {

	private static Logger logger = Logger.getLogger(NaiveSCM_EQC2.class);
	private Dictionnary dictionnary;
	private TripleStore tripleStore;

	public NaiveSCM_EQC2(Dictionnary dictionnary, TripleStore tripleStore) {
		super();
		this.dictionnary = dictionnary;
		this.tripleStore = tripleStore;
	}

	@Override
	public void run() {


		/**
		 * 	INPUT
		 * c1 rdfs:subClassOf c2
		 * c2 rdfs:subClassOf c1
		 *  OUPUT
		 * c1 owl:equivalentClass c2
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
		Collection<Triple> subClassOf_Triples = tripleStore.getbyPredicate(subClassOf);
		Collection<Triple> outputTriples = new HashSet<>();

		for (Triple t1 : subClassOf_Triples) {
			long s1=t1.getSubject(), o1=t1.getObject();

			for (Triple t2 : subClassOf_Triples) {
				long s2=t2.getSubject(), o2=t2.getObject();

				if(s1!=o1&&o1==s2&&s1==o2){
					Triple result = new TripleImplNaive(s1, equivalentClass, o1);

					logger.trace("SCM_EQC2 "+dictionnary.printTriple(t1)+" & "+dictionnary.printTriple(t2)+" -> "+dictionnary.printTriple(result));
					outputTriples.add(result);
				}

			}

		}
		tripleStore.addAll(outputTriples);

	}

}
