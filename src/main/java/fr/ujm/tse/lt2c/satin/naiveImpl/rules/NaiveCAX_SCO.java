package fr.ujm.tse.lt2c.satin.naiveImpl.rules;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;

import fr.ujm.tse.lt2c.satin.interfaces.Dictionnary;
import fr.ujm.tse.lt2c.satin.interfaces.Rule;
import fr.ujm.tse.lt2c.satin.interfaces.Triple;
import fr.ujm.tse.lt2c.satin.interfaces.TripleStore;
import fr.ujm.tse.lt2c.satin.naiveImpl.TripleImplNaive;

public class NaiveCAX_SCO implements Rule {

	private static Logger logger = Logger.getLogger(NaiveCAX_SCO.class);
	private Dictionnary dictionnary;
	private TripleStore tripleStore;

	public NaiveCAX_SCO(Dictionnary dictionnary, TripleStore tripleStore) {
		super();
		this.dictionnary = dictionnary;
		this.tripleStore = tripleStore;
	}

	@Override
	public void run() {


		/**
		 * 	INPUT
		 * c1 rdfs:subClassOf c2
		 * x rdf:type c1
		 *  OUPUT
		 * x rdf:type c2
		 */

		/*
		 * Get concepts codes in dictionnary
		 */
		long subClassOf = dictionnary.add("http://www.w3.org/2000/01/rdf-schema#subClassOf");
		long type = dictionnary.add("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");

		/*
		 * Get triples matching input 
		 * Create
		 */
		Collection<Triple> subClassOf_Triples = tripleStore.getbyPredicate(subClassOf);
		Collection<Triple> type_Triples = tripleStore.getbyPredicate(type);
		Collection<Triple> outputTriples = new HashSet<>();

		for (Triple t1 : subClassOf_Triples) {
			long s1=t1.getSubject(), o1=t1.getObject();

			for (Triple t2 : type_Triples) {
				long s2=t2.getSubject(), o2=t2.getObject();

				if(s1==o2){
					Triple result = new TripleImplNaive(s2, type, o1);
					logger.trace("CAX_SCO "+dictionnary.printTriple(t1)+" & "+dictionnary.printTriple(t2)+" -> "+dictionnary.printTriple(result));
					outputTriples.add(result);
				}

			}

		}
		tripleStore.addAll(outputTriples);

	}

}
